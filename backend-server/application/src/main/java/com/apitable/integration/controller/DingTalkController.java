package com.apitable.integration.controller;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.auth.vo.LoginResultVO;
import com.apitable.core.support.ResponseData;
import com.apitable.integration.dto.AppConfig;
import com.apitable.integration.enums.DingTalkEventEnum;
import com.apitable.integration.ro.DingTalkLoginRo;
import com.apitable.integration.service.IAppConfigService;
import com.apitable.integration.service.IDingTalkService;
import com.apitable.organization.entity.TeamEntity;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.ITeamMemberRelService;
import com.apitable.organization.service.ITeamService;
import com.apitable.shared.component.redis.RedisLockHelper;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.DingCallbackCrypto;
import com.apitable.shared.util.DingCallbackCrypto.DingTalkEncryptException;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.service.IUserService;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse.DeptGetResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse.UserGetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * DingTalk integrated interface.
 */
@RestController
@Tag(name = "DingTalk integrated interface")
@ApiResource(path = "/dingtalk")
@Slf4j
public class DingTalkController  {

    @Resource
    private IAppConfigService appConfigService;

    @Resource
    private IDingTalkService dingTalkService;

    @Resource
    private ITeamService iTeamService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Resource
    private RedisLockHelper redisLockHelper;

    /**
     * 获取CorpId.
     */
    @GetResource(path = "/corpid", requiredLogin = false)
    @Operation(summary = "获取CorpId", description = "通过传入的AppKey获取当前访问企业的CorpId")

    public ResponseData<String> getCorpId(@RequestParam(name = "appKey", required = true) String appKey) {
        log.info("传入AppKey:" + appKey);
        String corpId = appConfigService.getCorpIdByAppKey(appKey);
        
        return ResponseData.success(corpId);
    }

    /**
     * 根据免登码，获取用户信息.
     */
    @PostResource(name = "Login", path = "/login", requiredLogin = false)
    @Operation(summary = "登录获取用户信息", description = "通过传入的AppKey和免登码获取access_token及用户信息")
    public ResponseData<LoginResultVO> login(@RequestBody @Valid final DingTalkLoginRo dingTalkLoginRo, 
                                        final HttpServletRequest request) {
        Long userId = dingTalkService.getUserInfo(dingTalkLoginRo);
        String spaceId = iSpaceService.getSpaceIdByAppKey(dingTalkLoginRo.getAppKey());
        SessionContext.setUserId(userId);
        return ResponseData.success(LoginResultVO.builder().userId(userId).spaceId(spaceId).build());
    }

    /**
     * 同步钉钉组织架构至ApiTable.
     */
    @PostResource(name = "SyncDingTalk", path = "/sync/{spaceId}", requiredLogin = true)
    @Operation(summary = "同步钉钉组织架构", description = "同步钉钉的组织架构（部门及用户）至ApiTable")
    public ResponseData<Long> sync(@PathVariable("spaceId") String spaceId) {
        Long rootTeamId = dingTalkService.sync(spaceId);
        return ResponseData.success(rootTeamId);
    }

    /**
     * 钉钉事件订阅.
     */
    @PostResource(name = "DingTalkSubscribe", path = "/callback", requiredLogin = false)
    @Operation(summary = "钉钉事件订阅", description = "钉钉事件订阅|通讯录相关")
    public Map<String, String> subscribe(@RequestParam(value = "appKey", required = true) String appKey,
                                        @RequestParam(value = "signature", required = true) String signature,
                                        @RequestParam(value = "timestamp", required = true) String timeStamp,
                                        @RequestParam(value = "nonce", required = true) String nonce,
                                        @RequestBody(required = true) JSONObject json) throws DingTalkEncryptException {
        AppConfig appConfig = appConfigService.getAppConfigByAppKey(appKey);
        DingCallbackCrypto callbackCrypto = new DingCallbackCrypto(appConfig.getToken(), appConfig.getAesKey(), appConfig.getClientId());
        String encryptMsg = json.getStr("encrypt");
        String decryptMsg = callbackCrypto.getDecryptMsg(signature, timeStamp, nonce, encryptMsg);

        JSONObject eventJson = JSONUtil.parseObj(decryptMsg);
        String eventType = eventJson.getStr("EventType");
        DingTalkEventEnum event = DingTalkEventEnum.toEnum(eventType);
        if (event == DingTalkEventEnum.CHECK_URL) {
            Map<String, String> successMap = callbackCrypto.getEncryptedMap("success");
            return successMap;
        }
        String spaceId = iSpaceService.getSpaceIdByAppKey(appKey);
        switch (event) {
            case USER_ADD_ORG: {
                JSONArray userIdArr = eventJson.getJSONArray("UserId");
                for (int i = 0; i < userIdArr.size(); i++) {
                    UserGetResponse user = dingTalkService.getUserDetail(userIdArr.getStr(i), appConfig);
                    List<Long> deptIdList = user.getDeptIdList();
                    List<Long> teamIdList = iTeamService.getTeamIdsByDeptIds(spaceId, deptIdList);
                    dingTalkService.createUserWithTeam(spaceId, appKey, user.getUserid(), user.getMobile(), user.getEmail(), user.getName(), user.getAdmin(), teamIdList);
                }
                break;
            }
            case USER_MODIFY_ORG: {
                JSONArray userIdArr = eventJson.getJSONArray("UserId");
                for (int i = 0; i < userIdArr.size(); i++) {
                    String userIdStr = userIdArr.getStr(i);
                    try {
                        redisLockHelper.tryLock(userIdStr);
                        dingTalkService.userModifyOrg(appKey, spaceId, userIdStr, appConfig);
                    } finally {
                        redisLockHelper.releaseLock(userIdStr);
                    }
                }
                break;
            }
            case USER_LEAVE_ORG: {
                JSONArray userIdArr = eventJson.getJSONArray("UserId");
                dingTalkService.userLeaveOrg(appKey, spaceId, userIdArr, appConfig);
                break;
            }
            case USER_ADMIN_ADD: {
                JSONArray userIdArr = eventJson.getJSONArray("UserId");
                for (int i = 0; i < userIdArr.size(); i++) {
                    Long userId = iUserService.getUserIdByAppKeyAndDingUnionId(appKey, userIdArr.getStr(i));
                    Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
                    iMemberService.setMemberMainAdmin(memberId);
                }
                break;
            }
            case USER_ADMIN_REMOVE: {
                JSONArray userIdArr = eventJson.getJSONArray("UserId");
                for (int i = 0; i < userIdArr.size(); i++) {
                    Long userId = iUserService.getUserIdByAppKeyAndDingUnionId(appKey, userIdArr.getStr(i));
                    Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
                    iMemberService.cancelMemberMainAdmin(memberId);
                }
                break;
            }
            case ORG_DEPT_CREATE: {
                JSONArray deptIdArr = eventJson.getJSONArray("DeptId");
                for (int i = 0; i < deptIdArr.size(); i++) {
                    Long deptId = deptIdArr.getLong(i);
                    DeptGetResponse dept = dingTalkService.getDepartmentDetail(deptId, appConfig);
                    TeamEntity superTeam = iTeamService.getTeamByDeptId(spaceId, dept.getParentId());
                    iTeamService.createSubTeam(spaceId, dept.getName(), superTeam.getId(), dept.getDeptId(), dept.getParentId(), null);
                }
                break;
            }
            case ORG_DEPT_MODIFY: {
                JSONArray deptIdArr = eventJson.getJSONArray("DeptId");
                for (int i = 0; i < deptIdArr.size(); i++) {
                    Long deptId = deptIdArr.getLong(i);
                    DeptGetResponse dept = dingTalkService.getDepartmentDetail(deptId, appConfig);
                    TeamEntity teamEntity = iTeamService.getTeamByDeptId(spaceId, deptId);
                    TeamEntity parentTeam = iTeamService.getTeamByDeptId(spaceId, dept.getParentId());
                    iTeamService.updateTeamParent(teamEntity.getId(), dept.getName(), parentTeam.getId(), dept.getParentId());
                }
                break;
            }
            case ORG_DEPT_REMOVE: {
                JSONArray deptIdArr = eventJson.getJSONArray("DeptId");
                for (int i = 0; i < deptIdArr.size(); i++) {
                    Long deptId = deptIdArr.getLong(i);
                    TeamEntity teamEntity = iTeamService.getTeamByDeptId(spaceId, deptId);
                    iTeamService.deleteTeam(teamEntity.getId());
                }
                break;
            }
            default:
                break;
        }

        Map<String, String> successMap = callbackCrypto.getEncryptedMap("success");
        return successMap;
    }
}
