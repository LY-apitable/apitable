package com.apitable.integration.service;

import com.apitable.integration.ro.DingTalkLoginRo;

public interface IDingTalkService {
  
    /**
     * 登录获取用户信息.
     *
     * @param dingTalkLoginRo  dingTalkLoginRo
     */
    Long getUserInfo(DingTalkLoginRo dingTalkLoginRo);
}
