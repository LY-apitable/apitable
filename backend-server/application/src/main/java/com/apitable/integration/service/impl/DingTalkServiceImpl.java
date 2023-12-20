package com.apitable.integration.service.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.apitable.auth.service.IAuthService;
import com.apitable.core.exception.BusinessException;
import com.apitable.integration.dto.AppConfig;
import com.apitable.integration.ro.DingTalkLoginRo;
import com.apitable.integration.service.IAppConfigService;
import com.apitable.integration.service.IDingTalkService;
import com.apitable.user.service.IUserService;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.request.OapiV2UserGetuserinfoRequest;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.dingtalk.api.response.OapiV2UserGetuserinfoResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse.UserGetResponse;
import com.dingtalk.api.response.OapiV2UserGetuserinfoResponse.UserGetByCodeResponse;
import com.taobao.api.ApiException;
import com.aliyun.dingtalkoauth2_1_0.Client;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.teaopenapi.models.Config;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DingTalkServiceImpl implements IDingTalkService {

    @Resource
    private IAppConfigService appConfigService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private IAuthService authService;

    @Resource
    private IUserService userService;

    @Value("${DINGTALK_GET_USER_INFO}")
    private String getUserInfoUrl;

    @Value("${DINGTALK_GET_USER_DETAIL}")
    private String getUserDetailUrl;

    private static final String REDIS_KEY = "INTEGRATION:DINGTALK:ACCESS_TOKEN:";

    public Long getUserInfo(DingTalkLoginRo dingTalkLoginRo) {
        AppConfig appConfig = appConfigService.getAppConfigByAppKey(dingTalkLoginRo.getAppKey());
        String accessToken = "";
        try {
            //获取AccessToken
            accessToken = getAccessToken(appConfig.getClientId(), appConfig.getClientSecret());
        } catch (Exception e) {
            log.error("DingTalk获取AccessToken失败", e);
            throw new BusinessException("DingTalk获取AccessToken失败", e);
        }

        //获取用户ID
        UserGetByCodeResponse userResponse = getUserId(dingTalkLoginRo.getAuthCode(), accessToken);
        Long userId = userService.getUserIdByDingUnionId(userResponse.getUnionid());
        if (null == userId || 0L == userId) {//未注册用户
            //根据用户ID获取用户详情
            UserGetResponse userGetResponse = getUserDetail(userResponse.getUserid(), accessToken);
            log.info("xxxxxxxxxx email:" + userGetResponse.getEmail() + "   " + userGetResponse.getOrgEmail());
            //注册用户
            userId = authService.registerUserByDingTalk(userResponse.getUnionid(), "+86", userGetResponse.getMobile(), 
                            userGetResponse.getEmail(), userGetResponse.getName(), null, null);
        }
        return userId;
    }

    private String getAccessToken(String clientId, String clientSecret) throws Exception {
        String key = REDIS_KEY + clientId;
        String accessToken = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(accessToken)) {
            log.info("Dingtalk应用AccessToken缓存已存在：" + accessToken);
            return accessToken;
        }

        Config config = new Config();
        config.protocol = "https";
        config.regionId = "central";
        Client client = new Client(config);
        GetAccessTokenRequest request = new GetAccessTokenRequest();
        request.setAppKey(clientId);
        request.setAppSecret(clientSecret);
        GetAccessTokenResponse response = client.getAccessToken(request);
        if (response.getStatusCode() == 200) {
            accessToken = response.getBody().getAccessToken();
            Long expireIn = response.getBody().getExpireIn();
            redisTemplate.opsForValue().set(key, accessToken, expireIn - 60, TimeUnit.SECONDS);
            log.info("Dingtalk应用AccessToken获取：" + accessToken);
            return accessToken;
        } else {
            throw new BusinessException("DingTalk获取AccessToken失败 状态码" + response.getStatusCode());
        }
    }

    /**
     * 根据authCode获取用户ID
     *
     * @param authCode
     * @param accessToken
     * @return
     * @throws ApiException
     */
    private UserGetByCodeResponse getUserId(String authCode, String accessToken) {
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
     * 根据用户ID获取用户详情
     *
     * @param userId
     * @param accessToken
     * @return
     */
    private UserGetResponse getUserDetail(String userId, String accessToken) {
        DingTalkClient client = new DefaultDingTalkClient(getUserDetailUrl);
        OapiV2UserGetRequest request = new OapiV2UserGetRequest();
        request.setUserid(userId);
        request.setLanguage("zh_CN");

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
}
