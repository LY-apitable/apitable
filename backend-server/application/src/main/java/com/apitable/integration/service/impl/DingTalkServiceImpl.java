package com.apitable.integration.service.impl;

import static java.util.stream.Collectors.toList;

import cn.hutool.json.JSONArray;
import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.teaopenapi.models.Config;
import com.apitable.auth.service.IAuthService;
import com.apitable.core.exception.BusinessException;
import com.apitable.integration.dto.AppConfig;
import com.apitable.integration.ro.DingTalkLoginRo;
import com.apitable.integration.service.IAppConfigService;
import com.apitable.integration.service.IDingTalkService;
import com.apitable.organization.entity.TeamEntity;
import com.apitable.organization.service.IMemberService;
import com.apitable.organization.service.ITeamMemberRelService;
import com.apitable.organization.service.ITeamService;
import com.apitable.organization.vo.MemberInfoVo;
import com.apitable.organization.vo.MemberPageVo;
import com.apitable.organization.vo.MemberTeamPathInfo;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiV2DepartmentGetRequest;
import com.dingtalk.api.request.OapiV2DepartmentListsubRequest;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.request.OapiV2UserGetuserinfoRequest;
import com.dingtalk.api.request.OapiV2UserListRequest;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse.DeptGetResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse;
import com.dingtalk.api.response.OapiV2DepartmentListsubResponse.DeptBaseResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse.UserGetResponse;
import com.dingtalk.api.response.OapiV2UserGetuserinfoResponse;
import com.dingtalk.api.response.OapiV2UserGetuserinfoResponse.UserGetByCodeResponse;
import com.dingtalk.api.response.OapiV2UserListResponse;
import com.dingtalk.api.response.OapiV2UserListResponse.ListUserResponse;
import com.dingtalk.api.response.OapiV2UserListResponse.PageResult;
import com.taobao.api.ApiException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DingTalkServiceImpl.
 */
@Service
@Slf4j
public class DingTalkServiceImpl implements IDingTalkService {

    @Resource
    private IAppConfigService iAppConfigService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private IAuthService iAuthService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private ITeamService iTeamService;

    @Resource
    private IMemberService iMemberService;

    @Resource
    private ITeamMemberRelService iTeamMemberRelService;

    @Value("${DINGTALK_USER_INFO}")
    private String getUserInfoUrl;

    @Value("${DINGTALK_USER_DETAIL}")
    private String getUserDetailUrl;

    @Value("${DINGTALK_DEPARTMENT_LIST_SUB}")
    private String getDepartmentListSubUrl;

    @Value("${DINGTALK_USER_LIST_BY_DEPT}")
    private String getUserListByDeptUrl;

    @Value("${DINGTALK_DEPARTMENT_DETAIL}")
    private String getDepartmentDetailUrl;

    private static final String REDIS_KEY = "INTEGRATION:DINGTALK:ACCESS_TOKEN:";

    @Override
    public Long getUserInfo(DingTalkLoginRo dingTalkLoginRo) {
        AppConfig appConfig = iAppConfigService.getAppConfigByAppKey(dingTalkLoginRo.getAppKey());
        String accessToken;
        try {
            accessToken = getAccessToken(appConfig);
        } catch (Exception e) {
            log.error("DingTalk获取AccessToken失败", e);
            throw new BusinessException("DingTalk获取AccessToken失败", e);
        }

        String spaceId = iSpaceService.getSpaceIdByAppKey(dingTalkLoginRo.getAppKey());
        UserGetByCodeResponse userResponse = getUser(dingTalkLoginRo.getAuthCode(), accessToken);
        Long userId;
        if (StringUtils.isBlank(spaceId)) {
            UserGetResponse userGetResponse = getUserDetail(userResponse.getUserid(), appConfig);
            userId = iAuthService.registerUserByDingTalk(dingTalkLoginRo.getAppKey(), userGetResponse.getAdmin(), "+86", 
                                    userGetResponse.getMobile(), userGetResponse.getEmail(), userGetResponse.getUserid(), userGetResponse.getName());
        } else {
            userId = iUserService.getUserIdByAppKeyAndDingUnionId(dingTalkLoginRo.getAppKey(), userResponse.getUserid());
            if (null == userId || 0L == userId) {
                UserGetResponse userGetResponse = getUserDetail(userResponse.getUserid(), appConfig);
                userId = iAuthService.registerUserByDingTalk(dingTalkLoginRo.getAppKey(), userGetResponse.getAdmin(), "+86", 
                                        userGetResponse.getMobile(), userGetResponse.getEmail(), userGetResponse.getUserid(), userGetResponse.getName());
            }
        }
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sync(String spaceId) {
        SpaceEntity space = iSpaceService.getBySpaceId(spaceId);
        String appKey = space.getAppKey();
        AppConfig appConfig = iAppConfigService.getAppConfigByAppKey(appKey);
        String accessToken;
        try {
            accessToken = getAccessToken(appConfig);
        } catch (Exception e) {
            log.error("DingTalk获取AccessToken失败", e);
            throw new BusinessException("DingTalk获取AccessToken失败", e);
        }
        List<TeamEntity> teamList = iTeamService.selectAllTeamBySpaceId(spaceId);
        Long rootTeamId = iTeamService.getRootTeamId(spaceId);
        Map<Long, TeamEntity> deptIdTeamMap = teamList.stream()
                                                    .collect(Collectors.toMap(TeamEntity::getDeptId, Function.identity()));

        List<TeamEntity> newTeamList = syncTeam(accessToken, spaceId, deptIdTeamMap, rootTeamId, 1L);
        newTeamList.add(0, teamList.get(0));

        List<Long> newTeamDeptIdList = newTeamList.stream().map(TeamEntity::getDeptId).collect(Collectors.toList());
        
        List<Long> deleteTeamIdList = teamList.stream()
                                                .filter(a -> !newTeamDeptIdList.contains(a.getDeptId()))
                                                .map(TeamEntity::getId)
                                                .collect(Collectors.toList());

        if (deleteTeamIdList.size() > 0) {
            iTeamService.deleteTeam(deleteTeamIdList);
        }

        List<MemberPageVo> memberList = iMemberService.selectMembersByRootTeamId(spaceId);
        Map<String, MemberPageVo> unionIdMemberMap = memberList.stream().collect(Collectors.toMap(MemberPageVo::getDingUnionId, Function.identity()));
        for (TeamEntity teamEntity : newTeamList) {
            syncUser(unionIdMemberMap, appKey, spaceId, accessToken, teamEntity, 0L);
        }

        for (MemberPageVo member : unionIdMemberMap.values()) {
            Long memberId = member.getMemberId();
            String[] teamIdArr = member.getTeamIds().split(",");
            List<Long> teamIdList = Arrays.stream(teamIdArr).map(teamId -> Long.parseLong(teamId)).collect(Collectors.toList());
            iTeamMemberRelService.removeByTeamIdsAndMemberId(memberId, teamIdList);
            if (iTeamMemberRelService.getTeamByMemberId(memberId).size() == 0) {
                iMemberService.removeByMemberIds(Collections.singletonList(memberId));
                iUserService.closeAccount(iUserService.getById(member.getUserId()));
            }
        }

        return rootTeamId;
    }

    private void syncUser(Map<String, MemberPageVo> unionIdMemberMap, String appKey, String spaceId, String accessToken, TeamEntity teamEntity, Long cursor) {
        DingTalkClient client = new DefaultDingTalkClient(getUserListByDeptUrl);
        OapiV2UserListRequest req = new OapiV2UserListRequest();
        req.setDeptId(teamEntity.getDeptId());
        req.setCursor(cursor);
        req.setSize(100L);
        try {
            OapiV2UserListResponse response = client.execute(req, accessToken);
            PageResult result = response.getResult();
            List<ListUserResponse> userList = result.getList();
            for (ListUserResponse user : userList) {
                String userId = user.getUserid();
                if (unionIdMemberMap.containsKey(userId)) {
                    MemberPageVo member = unionIdMemberMap.get(userId);
                    List<String> tmpList = Arrays.asList(member.getTeamIds().split(","));
                    List<String> teamIdList = new ArrayList<>();
                    teamIdList.addAll(tmpList);
                    if (teamIdList.contains(Long.toString(teamEntity.getId()))) {
                        teamIdList.remove(Long.toString(teamEntity.getId()));
                        if (teamIdList.size() > 0) {
                            member.setTeamIds(String.join(",", teamIdList));
                        } else {
                            unionIdMemberMap.remove(userId);
                        }
                    } else {
                        iTeamMemberRelService.addMemberTeams(Collections.singletonList(member.getMemberId()), Collections.singletonList(teamEntity.getId()));
                    }
                    if (user.getAdmin()) {
                        iMemberService.setMemberMainAdmin(member.getMemberId());
                    } else {
                        iMemberService.cancelMemberMainAdmin(member.getMemberId());
                    }
                } else {
                    List<Long> teamIdList = new ArrayList<>();
                    teamIdList.add(teamEntity.getId());
                    createUserWithTeam(spaceId, appKey, userId, user.getMobile(), user.getEmail(), user.getName(), user.getAdmin(), teamIdList);
                }
            }
            if (result.getHasMore()) {
                Long nextCursor = result.getNextCursor();
                syncUser(unionIdMemberMap, appKey, spaceId, accessToken, teamEntity, nextCursor);
            }
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getErrMsg(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUserWithTeam(String spaceId, String appKey, String unionId, String mobile, String email, String name, boolean admin, List<Long> teamIdList) {
        Long userId = iUserService.getUserIdByAppKeyAndDingUnionId(appKey, unionId);
        if (null == userId || 0L == userId) {
            UserEntity userEntity = iUserService.createUserByDingTalk("+86", mobile, email, appKey, unionId, name);
            userId = userEntity.getId();
        }
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        if (memberId == null || memberId == 0L) {
            Long firstTeamId = teamIdList.get(0);
            teamIdList.remove(0);
            memberId = iMemberService.createMember(userId, spaceId, firstTeamId);
        }
        iTeamMemberRelService.addMemberTeams(Collections.singletonList(memberId), teamIdList);
        if (admin) {
            iMemberService.setMemberMainAdmin(memberId);
        }
    }

    private List<TeamEntity> syncTeam(String accessToken, String spaceId, Map<Long, TeamEntity> deptIdTeamMap, Long rootTeamId, Long queryDeptId) {
        DingTalkClient client = new DefaultDingTalkClient(getDepartmentListSubUrl);
        OapiV2DepartmentListsubRequest req = new OapiV2DepartmentListsubRequest();
        req.setDeptId(queryDeptId);
        OapiV2DepartmentListsubResponse response;
        List<TeamEntity> teamList = new ArrayList<>();
        try {
            response = client.execute(req, accessToken);
            List<DeptBaseResponse> deptList = response.getResult();
            int seq = 1;
            for (DeptBaseResponse deptInfo : deptList) {
                long deptId = deptInfo.getDeptId();
                String name = deptInfo.getName();
                long parentDeptId = deptInfo.getParentId();

                TeamEntity team;
                if (deptIdTeamMap.containsKey(deptId)) {
                    team = deptIdTeamMap.get(deptId);
                    team.setSpaceId(spaceId);
                    team.setParentId(deptIdTeamMap.get(parentDeptId).getId());
                    team.setTeamName(name);
                    team.setDeptId(deptId);
                    team.setParentDeptId(parentDeptId);
                    team.setSequence(seq);

                    iTeamService.updateById(team);
                } else {
                    long superId = 0L;
                    if (parentDeptId == 1L) {
                        superId = rootTeamId;
                    } else {
                        superId = deptIdTeamMap.get(parentDeptId).getId();
                    }
                    team = iTeamService.createSubTeam(spaceId, name, superId, deptId, parentDeptId, ++seq);
                    deptIdTeamMap.put(deptId, team);
                }
                teamList.add(team);
                teamList.addAll(syncTeam(accessToken, spaceId, deptIdTeamMap, rootTeamId, deptId));
            }
            return teamList;
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getErrMsg(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String getAccessToken(AppConfig appConfig) {
        String clientId = appConfig.getClientId();
        String key = REDIS_KEY + clientId;
        String accessToken = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(accessToken)) {
            log.info("Dingtalk应用AccessToken缓存已存在：" + accessToken);
            return accessToken;
        }

        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        try {
            Client client = new Client(config);
            GetAccessTokenRequest request = new GetAccessTokenRequest();
            request.setAppKey(clientId);
            request.setAppSecret(appConfig.getClientSecret());
            GetAccessTokenResponse response = client.getAccessToken(request);
            if (response.getStatusCode() == 200) {
                accessToken = response.getBody().getAccessToken();
                Long expireIn = response.getBody().getExpireIn();
                redisTemplate.opsForValue().set(key, accessToken, expireIn - 120, TimeUnit.SECONDS);
                log.info("Dingtalk应用AccessToken获取：" + accessToken);
                return accessToken;
            } else {
                throw new BusinessException("DingTalk获取AccessToken失败 状态码" + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getMessage(), e);
        }
    }

    /**
     * 根据authCode获取用户ID.
     *
     * @param authCode authCode
     * @param accessToken accessToken
     */
    private UserGetByCodeResponse getUser(String authCode, String accessToken) {
        DingTalkClient client = new DefaultDingTalkClient(getUserInfoUrl);
        OapiV2UserGetuserinfoRequest request = new OapiV2UserGetuserinfoRequest();
        request.setCode(authCode);
        OapiV2UserGetuserinfoResponse response;
        try {
            response = client.execute(request, accessToken);
            if (response.isSuccess()) {
                return response.getResult();
            } else {
                String errMsg = "DingTalk获取UserId失败 errorCode:" + response.getErrorCode() + " errMsg:" + response.getErrmsg();
                log.error(errMsg);
                throw new BusinessException(errMsg);
            }

        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getErrMsg(), e);
        }
    }

    /**
     * 根据用户ID获取用户详情.
     *
     * @param userId userId
     * @param appConfig appConfig
     */
    @Override
    public UserGetResponse getUserDetail(String userId, AppConfig appConfig) {
        String accessToken;
        try {
            accessToken = getAccessToken(appConfig);
        } catch (Exception e) {
            log.error("DingTalk获取AccessToken失败", e);
            throw new BusinessException("DingTalk获取AccessToken失败", e);
        }

        DingTalkClient client = new DefaultDingTalkClient(getUserDetailUrl);
        OapiV2UserGetRequest request = new OapiV2UserGetRequest();
        request.setUserid(userId);
        try {
            OapiV2UserGetResponse response = client.execute(request, accessToken);
            if (response.isSuccess()) {
                return response.getResult();
            } else {
                String errMsg = "DingTalk获取User失败 errorCode:" + response.getErrorCode() + " errMsg:" + response.getErrmsg();
                log.error(errMsg);
                throw new BusinessException(errMsg);
            }
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getErrMsg(), e);
        }
    }

    /**
     * 根据部门ID获取部门详情.
     *
     * @param deptId deptId
     * @param appConfig appConfig
     */
    @Override
    public DeptGetResponse getDepartmentDetail(Long deptId, AppConfig appConfig) {
        String accessToken;
        try {
            accessToken = getAccessToken(appConfig);
        } catch (Exception e) {
            log.error("DingTalk获取AccessToken失败", e);
            throw new BusinessException("DingTalk获取AccessToken失败", e);
        }

        DingTalkClient client = new DefaultDingTalkClient(getDepartmentDetailUrl);
        OapiV2DepartmentGetRequest request = new OapiV2DepartmentGetRequest();
        request.setDeptId(deptId);
        try {
            OapiV2DepartmentGetResponse response = client.execute(request, accessToken);
            if (response.isSuccess()) {
                return response.getResult();
            } else {
                String errMsg = "DingTalk获取Department失败 errorCode:" + response.getErrorCode() + " errMsg:" + response.getErrmsg();
                log.error(errMsg);
                throw new BusinessException(errMsg);
            }
        } catch (ApiException e) {
            log.error(e.getMessage(), e);
            throw new BusinessException(e.getErrMsg(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userModifyOrg(String appKey, String spaceId, String userIdStr, AppConfig appConfig) {
        UserGetResponse user = getUserDetail(userIdStr, appConfig);
        Long userId = iUserService.getUserIdByAppKeyAndDingUnionId(appKey, userIdStr);
        Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userId, spaceId);
        MemberInfoVo memberInfo = iMemberService.getMemberInfoVo(memberId);
        List<MemberTeamPathInfo> teamInfoList = memberInfo.getTeamData();
        List<Long> nowTeamIdList = teamInfoList.stream().map(MemberTeamPathInfo::getTeamId).collect(toList());
        List<Long> newTeamIdList = iTeamService.getTeamIdsByDeptIds(spaceId, user.getDeptIdList());

        List<Long> addTeamIdList = newTeamIdList.stream().filter(t -> !nowTeamIdList.contains(t)).collect(toList());
        List<Long> removeTeamIdList = nowTeamIdList.stream().filter(t -> !newTeamIdList.contains(t)).collect(toList());
        if (removeTeamIdList.size() > 0) {
            iTeamMemberRelService.removeByTeamIdsAndMemberId(memberId, removeTeamIdList);
        }
        if (addTeamIdList.size() > 0) {
            iTeamMemberRelService.addMemberTeams(Collections.singletonList(memberId), addTeamIdList);
        }
        iMemberService.updateMemberNameByUserId(userId, user.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userLeaveOrg(String appKey, String spaceId, JSONArray userIdArr, AppConfig appConfig) {
        for (int i = 0; i < userIdArr.size(); i++) {
            Long userId = iUserService.getUserIdByAppKeyAndDingUnionId(appKey, userIdArr.getStr(i));
            UserEntity userEntity = iUserService.getById(userId);
            Long memberId = iMemberService.getMemberIdByUserIdAndSpaceId(userEntity.getId(), spaceId);
            iMemberService.removeByMemberIds(Collections.singletonList(memberId));
            iUserService.closeAccount(userEntity);
        }
    }
}
