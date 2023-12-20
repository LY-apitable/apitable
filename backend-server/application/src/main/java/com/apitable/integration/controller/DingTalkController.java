package com.apitable.integration.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apitable.auth.ro.LoginRo;
import com.apitable.auth.vo.LoginResultVO;
import com.apitable.core.support.ResponseData;
import com.apitable.integration.ro.DingTalkLoginRo;
import com.apitable.integration.service.IAppConfigService;
import com.apitable.integration.service.IDingTalkService;
import com.apitable.integration.service.impl.DingTalkServiceImpl;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.SessionContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

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

    /**
     * 获取CorpId
     */
    @GetResource(path = "/corpid", requiredLogin = false)
    @Operation(summary = "获取CorpId", description = "通过传入的AppKey获取当前访问企业的CorpId")

    public ResponseData<String> getCorpId(@RequestParam(name = "appKey", required = true) String appKey) {
        log.info("传入AppKey:" + appKey);
        String corpId = appConfigService.getCorpIdByAppKey(appKey);
        
        return ResponseData.success(corpId);
    }

    /**
     * 根据免登码，获取用户信息
     */
    @PostResource(name = "Login", path = "/login", requiredLogin = false)
    @Operation(summary = "登录获取用户信息", description = "通过传入的AppKey和免登码获取access_token及用户信息")
    public ResponseData<LoginResultVO> login(@RequestBody @Valid final DingTalkLoginRo dingTalkLoginRo, 
                                        final HttpServletRequest request) {
        Long userId = dingTalkService.getUserInfo(dingTalkLoginRo);
        SessionContext.setUserId(userId);
        return null;
    }
}