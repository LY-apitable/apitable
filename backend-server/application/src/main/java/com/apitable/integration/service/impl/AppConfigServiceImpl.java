package com.apitable.integration.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.core.exception.BusinessException;
import com.apitable.integration.dto.AppConfig;
import com.apitable.integration.service.IAppConfigService;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * AppConfigServiceImpl.
 */
@Service
@Slf4j
public class AppConfigServiceImpl implements IAppConfigService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${LMKJ_AUTH_DESKTOP}")
    private String lmkjAuthUrl; 

    private static final String REDIS_KEY = "INTEGRATION:AUTH:APPKEY:";

    @Override
    public String getCorpIdByAppKey(String appKey) {
        AppConfig appConfig = getAppConfigByAppKey(appKey);
        return appConfig.getCorpId();
    }

    @Override
    public AppConfig getAppConfigByAppKey(String appKey) {
        String key = REDIS_KEY + appKey;
        AppConfig appConfig = (AppConfig) redisTemplate.opsForValue().get(key);
        if (appConfig != null && appConfig.getId() != 0L) {
            return appConfig;
        }

        OkHttpClient client = new OkHttpClient();
        MultipartBody body = new MultipartBody.Builder()
                    .setType(MediaType.parse("multipart/form-data"))
                    .addFormDataPart("urlToken", appKey)
                    .build();
        Request request = new Request.Builder()
                    .post(body)
                    .url(lmkjAuthUrl)
                    .build();
        try {
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                JSONObject responseObj = JSONUtil.parseObj(response.body().string());
                if (responseObj.getInt("code") == 404) {
                    throw new BusinessException("未授权组织！");
                }

                JSONObject data = responseObj.getJSONObject("data");
                
                appConfig = new AppConfig();
                appConfig.setId(data.getLong("id"));
                appConfig.setCorpId(data.getStr("corpId"));
                appConfig.setClientId(data.getStr("clientId"));
                appConfig.setClientSecret(data.getStr("clientSecret"));
                appConfig.setAesKey(data.getStr("aesKey"));
                appConfig.setToken(data.getStr("token"));
                appConfig.setAgentId(data.getLong("agentId"));
                redisTemplate.opsForValue().set(key, appConfig, 1800, TimeUnit.SECONDS);

                return appConfig;
            } else {
                throw new BusinessException("Dingtalk权限校验失败,code:" + response.code() + " Msg:" + response.message());
            }
        } catch (IOException e) {
            log.error("Dingtalk权限校验失败", e);
            throw new BusinessException("Dingtalk权限校验失败", e);
        }
    }
}
