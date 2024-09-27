package com.apitable.integration.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * AppConfig.
 */
@Data
public class AppConfig implements Serializable {

    private Long id;

    private String corpId;

    private String clientId;

    private String clientSecret;

    private String aesKey;

    private String token;

    private Long agentId;
}