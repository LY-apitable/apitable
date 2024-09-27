package com.apitable.integration.service;

import com.apitable.integration.dto.AppConfig;

/**
 * IAppConfigService.
 */
public interface IAppConfigService {
  
    public String getCorpIdByAppKey(String appKey);

    public AppConfig getAppConfigByAppKey(String appKey);
}