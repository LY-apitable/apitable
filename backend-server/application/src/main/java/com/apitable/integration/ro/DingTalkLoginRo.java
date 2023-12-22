package com.apitable.integration.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Data;

@Data
@Schema(description = "钉钉免登参数")
public class DingTalkLoginRo {

    @Schema(description = "AppKey", requiredMode = RequiredMode.REQUIRED)
    private String appKey;

    @Schema(description = "AuthCode|钉钉免登授权码", requiredMode = RequiredMode.REQUIRED)
    private String authCode;
}
