package com.apitable.shared.client;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.apitable.shared.config.properties.XxlJobClientConfigProperties;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * XxlJobClient.
 */
@Component
@Slf4j
public class XxlJobClient {

    private String cookie = "";

    private static final String POST_FORM_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=UTF-8";

    @Autowired
    private XxlJobClientConfigProperties clientConfigProperties;

    @PostConstruct
    public void init() throws IOException {
        log.info("xxl JOB 初始化配置：{}", clientConfigProperties.toString());
    }

    /**
     * 根据执行器名称查找执行器.
     */
    public JSONObject loadByAppName(String appName) {
        log.info("loadByAppName: {}", appName);
        return doGet(String.format(clientConfigProperties.getJobGroupLoadByAppNameUrl(), appName));
    }

    /**
     * 创建任务.
     */
    public JSONObject createJob(JSONObject params) {
        return doPost(clientConfigProperties.getJobInfoAddUrl(), params);
    }

    /**
     * 更新任务.
     */
    public JSONObject updateJob(JSONObject params) {
        return doPost(clientConfigProperties.getJobInfoUpdateUrl(), params);
    }

    /**
     * 根据任务 ID 加载.
     */
    public JSONObject loadById(int id) {
        log.info("loadById: {}", id);
        return doGet(String.format(clientConfigProperties.getJobInfoLoadByIdUrl(), id));
    }

    /**
     * 删除任务.
     */
    public JSONObject deleteJob(int id) {
        log.info("deleteJob: {}", id);
        return doGet(String.format(clientConfigProperties.getJobInfoDeleteUrl(), id));
    }

    /**
     * 开启任务.
     */
    public JSONObject startJob(int id) {
        log.info("startJob: {}", id);
        return doGet(String.format(clientConfigProperties.getJobInfoStartJobUrl(), id));
    }

    /**
     * 停止任务.
     */
    public JSONObject stopJob(int id) {
        log.info("stopJob: {}", id);
        return doGet(String.format(clientConfigProperties.getJobInfoStopJobUrl(), id));
    }

    /**
     * 创建执行器.
     */
    public JSONObject createJobGroup(JSONObject params) throws IOException {
        return doPost(clientConfigProperties.getJobGroupSaveUrl(), params);
    }

    /**
     * 执行器列表.
     */
    public JSONObject jobGroupPageList(JSONObject params) throws IOException {
        params.set("start", Optional.ofNullable(params.getInt("start")).orElse(0));
        params.set("length", Optional.ofNullable(params.getInt("length")).orElse(10));
        return doPost(clientConfigProperties.getJobGroupPageListUrl(), params);
    }

    /**
     * 任务列表.
     */
    public JSONObject jobInfoPageList(JSONObject params) throws IOException {
        params.set("start", Optional.ofNullable(params.getInt("start")).orElse(0));
        params.set("length", Optional.ofNullable(params.getInt("length")).orElse(10));
        return doPost(clientConfigProperties.getJobInfoPageListUrl(), params);
    }

    /**
     * 发起 GET 请求.
     */
    private JSONObject doGet(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .get()
            .url(url)
            .header("cookie", cookie)
            .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                log.info("xxlJob url:{} doGet:{}", url, body);
                return JSONUtil.parseObj(body);
            } else {
                log.error("xxlJob doGet:{}", response.code());
                return null;
            }
        } catch (IOException e) {
            log.error("xxlJob 登录失败123", e);
            return null;
        }
    }

    private JSONObject doPost(String url, JSONObject params) {
        OkHttpClient client = new OkHttpClient();
        Builder builder = new FormBody.Builder();
        params.forEach((k, v) -> builder.addEncoded(k, v.toString()));
        Request request = new Request.Builder()
            .post(builder.build())
            // .get()
            .url(url)
            .header("Content-Type", POST_FORM_CONTENT_TYPE)
            // .header("cookie", cookie)
            .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String body = response.body().string();
                log.info("xxlJob url:{} params:{} doPost:{}", url, params.toString(), body);
                return JSONUtil.parseObj(body);
            } else {
                log.error("xxlJob doPost:{}", response.code());
                return null;
            }
        } catch (IOException e) {
            log.error("xxlJob doPost", e);
            return null;
        }
    }
}
