/*
 * APITable <https://github.com/apitable/apitable>
 * Copyright (C) 2022 APITable Ltd. <https://apitable.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.apitable.interfaces.social.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.json.JSONObject;
import cn.vika.core.utils.StringUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.integration.dto.AppConfig;
import com.apitable.integration.service.IAppConfigService;
import com.apitable.integration.service.IDingTalkService;
import com.apitable.interfaces.social.event.NotificationEvent;
import com.apitable.interfaces.social.event.SocialEvent;
import com.apitable.interfaces.social.model.CustomSocialConnectInfo;
import com.apitable.interfaces.social.model.SocialConnectInfo;
import com.apitable.interfaces.social.model.SocialUserBind;
import com.apitable.organization.service.IUnitService;
import com.apitable.player.enums.NotificationException;
import com.apitable.player.ro.NotificationCreateRo;
import com.apitable.shared.component.notification.INotificationFactory;
import com.apitable.shared.component.notification.NotificationTemplateId;
import com.apitable.shared.component.notification.NotificationToTag;
import com.apitable.shared.config.properties.ConstProperties;
import com.apitable.shared.constants.NotificationConstants;
import com.apitable.shared.sysconfig.i18n.I18nStringsUtil;
import com.apitable.shared.sysconfig.notification.NotificationTemplate;
import com.apitable.shared.sysconfig.notification.SocialTemplate;
import com.apitable.space.entity.SpaceEntity;
import com.apitable.space.enums.SpaceUpdateOperate;
import com.apitable.space.service.ISpaceMemberRoleRelService;
import com.apitable.space.service.ISpaceRoleService;
import com.apitable.space.service.ISpaceService;
import com.apitable.user.entity.UserEntity;
import com.apitable.user.service.IUserService;
import com.apitable.workspace.service.INodeService;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationAsyncsendV2Request;
import com.dingtalk.api.response.OapiMessageCorpconversationAsyncsendV2Response;
import com.taobao.api.ApiException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * custom social service facade implements.
 */
@Component
@Slf4j
public class CustomSocialServiceFacade implements SocialServiceFacade {

    @Resource
    private IAppConfigService iAppConfigService;

    @Resource
    private IDingTalkService iDingTalkService;

    @Resource
    private ISpaceService iSpaceService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private INotificationFactory notificationFactory;

    @Resource
    private ISpaceRoleService spaceRoleService;

    @Resource
    private ISpaceMemberRoleRelService spaceMemberRoleRelService;

    @Resource
    private IUnitService iUnitService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ConstProperties constProperties;

    @Value("${DINGTALK_SEND_MSG}")
    private String dingtalkSendMsgUrl;

    @Override
    public void createSocialUser(SocialUserBind socialUser) {

    }

    @Override
    public Long getUserIdByUnionId(String unionId) {
        return null;
    }

    @Override
    public String getSpaceIdByDomainName(String domainName) {
        return null;
    }

    @Override
    public String getDomainNameBySpaceId(String spaceId, boolean appendHttpsPrefix) {
        return null;
    }

    @Override
    public Map<String, String> getDomainNameMap(List<String> spaceIds) {
        return Collections.emptyMap();
    }

    @Override
    public void removeDomainBySpaceIds(List<String> spaceIds) {

    }

    @Override
    public SocialConnectInfo getConnectInfo(String spaceId) {
        SpaceEntity space = iSpaceService.getBySpaceId(spaceId);
        if (StringUtils.isBlank(space.getAppKey())) {
            return null;
        }
        String appKey = space.getAppKey();
        AppConfig appConfig = iAppConfigService.getAppConfigByAppKey(appKey);
        if (null == appConfig || appConfig.getId() == 0L) {
            return null;
        }
        CustomSocialConnectInfo socialConnectInfo = new CustomSocialConnectInfo();
        socialConnectInfo.setAppId(appKey);
        socialConnectInfo.setEnabled(true);
        socialConnectInfo.setSpaceId(spaceId);
        socialConnectInfo.setPlatform(2);
        return socialConnectInfo;
    }

    @Override
    public boolean checkSocialBind(String spaceId) {
        return false;
    }

    @Override
    public void checkCanOperateSpaceUpdate(String spaceId, SpaceUpdateOperate spaceUpdateOperate) {

    }

    @Override
    public void checkWhetherSpaceCanChangeMainAdmin(String spaceId, Long opMemberId,
                                                    Long acceptMemberId,
                                                    List<SpaceUpdateOperate> spaceUpdateOperates) {

    }

    @Override
    public void deleteUser(Long userId) {

    }

    @Override
    public String getSuiteKeyByDingtalkSuiteId(String suiteId) {
        return null;
    }

    @Override
    public List<String> fuzzySearchIfSatisfyCondition(String spaceId, String word) {
        return null;
    }

    @Override
    public <T extends SocialEvent> void eventCall(T event) {
        switch (event.getEventType()) {
            case NOTIFICATION:
                NotificationCreateRo ro = ((NotificationEvent) event).getNotificationMeta();
                if (ro.getType() == 0) {
                    NotificationTemplate template = notificationFactory.getTemplateById(ro.getTemplateId());
                    if (null == template) {
                        log.error("SocialNotificationTemplateError {}", ro.getTemplateId());
                        return;
                    }
                    NotificationToTag toTag = NotificationToTag.getValue(template.getToTag());
                    if (toTag == null) {
                        log.error("fail to get tag:{}", template.getToTag());
                        return;
                    }
                    ExceptionUtil.isNotNull(toTag, NotificationException.TMPL_TO_TAG_ERROR);
                    if (NotificationToTag.toUserTag(toTag)) {
                        createUserNotify(template, ro);
                        return;
                    }
                    if (NotificationToTag.toMemberTag(toTag)) {
                        createMemberNotify(template, ro, toTag);
                        return;
                    }
                } else if (ro.getType() == 1) {
                    List<Long> userIds = getUserIdListByRo(ro, NotificationToTag.MEMBERS);
                    List<UserEntity> userList = iUserService.listByIds(userIds);
                    String dingUnionIds = userList.stream().map(UserEntity::getDingUnionId).collect(Collectors.joining(","));
                    sendCustomDingtalkActionCard(ro, dingUnionIds);
                }
                break;
            case TEMPLATE_QUOTE:
                break;
            default:
                break;
        }
    }

    private void createUserNotify(NotificationTemplate template, NotificationCreateRo ro) {
        List<String> toUserIds = CollUtil.removeBlank(CollUtil.distinct(ro.getToUserId()));
        if (CollUtil.isEmpty(toUserIds)) {
            throw new BusinessException(NotificationException.USER_EMPTY_ERROR);
        }
        // createNotifyWithoutVerify(ListUtil.toList(Convert.toLongArray(toUserIds)), template, ro);
    }

    private void createMemberNotify(NotificationTemplate template, NotificationCreateRo ro,
                                   NotificationToTag toTag) {
        List<Long> userIds = getUserIdListByRo(ro, toTag);
        
        SocialTemplate socialTemplate = notificationFactory.getSocialTemplateById("dingtalk", ro.getTemplateId());
        if (null == socialTemplate) {
            return;
        }

        List<UserEntity> userList = iUserService.listByIds(userIds);
        String dingUnionIds = userList.stream().map(UserEntity::getDingUnionId).collect(Collectors.joining(","));

        switch (socialTemplate.getMessageType()) {
            case "action_card":
                sendDingtalkActionCard(socialTemplate, ro, dingUnionIds);
                break;
            default:
                break;
        }
    }

    private List<Long> getUserIdListByRo(NotificationCreateRo ro, NotificationToTag toTag) {
        List<Long> userIds;
        if (NotificationTemplateId.spaceDeleteNotify(
            NotificationTemplateId.getValue(ro.getTemplateId()))) {
            userIds = CollUtil.toList(Convert.toLongArray(ro.getToUserId()));
        } else {
            List<Long> toMemberIds = CollUtil.newArrayList();
            if (CollUtil.isNotEmpty(ro.getToMemberId())) {
                toMemberIds.addAll(
                    CollUtil.removeBlank(CollUtil.distinct(ro.getToMemberId())).stream()
                        .map(Long::valueOf).toList());
            }
            if (CollUtil.isNotEmpty(ro.getToUnitId())) {
                List<Long> unitIds =
                    CollUtil.removeBlank(CollUtil.distinct(ro.getToUnitId())).stream()
                        .map(Long::valueOf).collect(Collectors.toList());
                toMemberIds.addAll(iUnitService.getMembersIdByUnitIds(unitIds));
            }
            userIds = getSpaceUserIdByMemberIdAndToTag(ro.getSpaceId(), toMemberIds, toTag);
        }
        if (CollUtil.isEmpty(userIds)) {
            throw new BusinessException(NotificationException.USER_EMPTY_ERROR);
        }
        return userIds;
    }

    private List<Long> getSpaceUserIdByMemberIdAndToTag(String spaceId, List<Long> memberIds, NotificationToTag toTag) {
        if (toTag.equals(NotificationToTag.MEMBERS)) {
            ExceptionUtil.isNotEmpty(memberIds, NotificationException.MEMBER_EMPTY_ERROR);
            return notificationFactory.getMemberUserId(memberIds, spaceId);
        }
        if (toTag.equals(NotificationToTag.ALL_MEMBERS)) {
            return notificationFactory.getSpaceAllUserId(spaceId);
        }
        if (toTag.equals(NotificationToTag.SPACE_ADMINS)) {
            return notificationFactory.getMemberUserId(
                spaceRoleService.getSpaceAdminsWithWorkbenchManage(spaceId), spaceId);
        }
        if (toTag.equals(NotificationToTag.SPACE_MEMBER_ADMINS)) {
            List<Long> memberAdminIds =
                spaceMemberRoleRelService.getMemberIdListByResourceGroupCodes(spaceId,
                    ListUtil.toList(NotificationConstants.TO_MANAGE_MEMBER_RESOURCE_CODE));
            memberAdminIds.add(notificationFactory.getSpaceSuperAdmin(spaceId));
            if (CollUtil.isNotEmpty(memberAdminIds)) {
                return notificationFactory.getMemberUserIdExcludeDeleted(memberAdminIds);
            }
        }
        if (toTag.equals(NotificationToTag.SPACE_MAIN_ADMIN)) {
            Long mainMemberId = notificationFactory.getSpaceSuperAdmin(spaceId);
            return notificationFactory.getMemberUserId(Collections.singletonList(mainMemberId),
                spaceId);
        }
        return null;
    }

    private void sendDingtalkActionCard(SocialTemplate socialTemplate, NotificationCreateRo ro, String dingUnionIds) {
        SpaceEntity space = iSpaceService.getBySpaceId(ro.getSpaceId());
        AppConfig appConfig = iAppConfigService.getAppConfigByAppKey(space.getAppKey());
        Long agentId = appConfig.getAgentId();

        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(agentId);
        request.setUseridList(dingUnionIds);
        request.setToAllUser(false);
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        JSONObject extra = ro.getBody().getJSONObject("extras");
        msg.setActionCard(new OapiMessageCorpconversationAsyncsendV2Request.ActionCard());
        msg.getActionCard().setTitle(I18nStringsUtil.t(socialTemplate.getTitle(), new Locale("zh-CN")));
        msg.getActionCard().setMarkdown(StringUtil.format(I18nStringsUtil.t(socialTemplate.getTemplateString(), new Locale("zh-CN")), extra));
        msg.getActionCard().setSingleTitle(I18nStringsUtil.t(socialTemplate.getUrlTitle(), new Locale("zh-CN")));
        Map<String, String> params = new HashMap<>();
        params.put("appKey", space.getAppKey());
        params.put("nodeId", ro.getNodeId());
        params.put("viewId", extra.getStr("viewId"));
        params.put("recordId", extra.getJSONArray("recordIds").getStr(0));
        String redirectUrl = constProperties.getServerDomain() + StringUtil.format(socialTemplate.getUrl(), params);
        String url = "dingtalk://dingtalkclient/action/openapp?corpid=" + appConfig.getCorpId() + "&container_type=work_platform&app_id=0_" + agentId + "&redirect_type=jump&redirect_url=";
        try {
            url += URLEncoder.encode(redirectUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("redirectUrl Encode Error", e);
        }
        log.info("发送工作通知url" + url);
        msg.getActionCard().setSingleUrl(url);
        msg.setMsgtype(socialTemplate.getMessageType());
        request.setMsg(msg);
        try {
            DingTalkClient client = new DefaultDingTalkClient(dingtalkSendMsgUrl);
            String accessToken = iDingTalkService.getAccessToken(appConfig);
            OapiMessageCorpconversationAsyncsendV2Response rsp = client.execute(request, accessToken);
            log.info("发送工作通知结果：" + rsp.getBody());
        } catch (ApiException e) {
            log.error("发送工作通知失败", e);
        }
    }

    private void sendCustomDingtalkActionCard(NotificationCreateRo ro, String dingUnionIds) {
        SpaceEntity space = iSpaceService.getBySpaceId(ro.getSpaceId());
        AppConfig appConfig = iAppConfigService.getAppConfigByAppKey(space.getAppKey());
        Long agentId = appConfig.getAgentId();

        OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
        request.setAgentId(agentId);
        request.setUseridList(dingUnionIds);
        request.setToAllUser(false);
        OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
        msg.setActionCard(new OapiMessageCorpconversationAsyncsendV2Request.ActionCard());
        msg.getActionCard().setTitle(ro.getTitle());
        msg.getActionCard().setMarkdown(ro.getContent());
        msg.getActionCard().setSingleTitle(I18nStringsUtil.t("social_notification_url_title", new Locale("zh-CN")));
        String redirectUrl = constProperties.getServerDomain() + "/user/dingtalk/social_login?appkey=" + space.getAppKey();;
        String url = "dingtalk://dingtalkclient/action/openapp?corpid=" + appConfig.getCorpId() + "&container_type=work_platform&app_id=0_" + agentId + "&redirect_type=jump&redirect_url=";
        try {
            url += URLEncoder.encode(redirectUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("redirectUrl Encode Error", e);
        }
        msg.getActionCard().setSingleUrl(url);
        msg.setMsgtype("action_card");
        request.setMsg(msg);
        try {
            DingTalkClient client = new DefaultDingTalkClient(dingtalkSendMsgUrl);
            String accessToken = iDingTalkService.getAccessToken(appConfig);
            OapiMessageCorpconversationAsyncsendV2Response rsp = client.execute(request, accessToken);
            log.info("发送工作通知结果：" + rsp.getBody());
        } catch (ApiException e) {
            log.error("发送工作通知失败", e);
        }
    }
}
