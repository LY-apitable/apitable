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

package com.apitable.starter.databus.autoconfigure;

import com.apitable.starter.databus.client.api.AutomationDaoApiApi;
import com.apitable.starter.databus.client.api.FusionApiApi;
import com.apitable.starter.databus.client.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DatabusClientConfig.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DatabusClientProperties.class)
public class DatabusClientAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabusClientAutoConfiguration.class);

    private final DatabusClientProperties databusClientProperties;

    @Value("${SERVER_DOMAIN}")
    private String serverDomain;
    
    public DatabusClientAutoConfiguration(DatabusClientProperties databusClientProperties) {
        this.databusClientProperties = databusClientProperties;
    }

    /**
     * Common databus client.
     *
     * @return {@link ApiClient}
     */
    @Bean
    public ApiClient apiClient() {
        LOGGER.info("Register Databus Client Bean");
        ApiClient client = new ApiClient();
        client.setBasePath(databusClientProperties.getHost());
        return client;
    }

    /**
     * Api client for fusion api v1.
     *
     * @return {@link ApiClient}
     */
    @Bean
    public ApiClient apiClientV1() {
        LOGGER.info("Register Databus Client Bean");
        ApiClient client = new ApiClient();
        client.setBasePath(serverDomain);
        return client;
    }

    /**
     * Common databus automation client.
     *
     * @return {@link AutomationDaoApiApi}
     */
    @Bean
    public AutomationDaoApiApi automationDaoApiApi(@Qualifier("apiClient")ApiClient apiClient) {
        LOGGER.info("Register Databus AutomationDaoApiApi Bean");
        return new AutomationDaoApiApi(apiClient);
    }

    /**
     * Fusion api client.
     *
     * @return {@link FusionApiApi}
     */
    @Bean
    public FusionApiApi fusionApiApi(@Qualifier("apiClientV1")ApiClient apiClient) {
        LOGGER.info("Register Databus FusionApiApi Bean");
        return new FusionApiApi(apiClient);
    }
}
