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

package com.apitable.interfaces.social.model;

import com.apitable.space.enums.SpaceResourceGroupCode;
import java.util.List;
import lombok.Data;

/**
 * social connect info.
 */
@Data
public class CustomSocialConnectInfo implements SocialConnectInfo {

    private String spaceId;

    private Integer platform;

    private Integer appType;

    private String appId;

    private String tenantId;

    private Integer authMode;

    private Boolean enabled;

    private Boolean contactSyncing = false;

    private String remindObserver;

    private List<SpaceResourceGroupCode> disableResourceGroupCodes;

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public boolean contactSyncing() {
        return this.contactSyncing;
    }
}
