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

package com.apitable.integration.ro;

import cn.hutool.json.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * <p>
 * Integration queue list parameters.
 * </p>
 */
@Data
@Schema(description = "Integration queue list parameters")
public class IntegrationCreateRo {

    @Schema(description = "Space ID (optional)", example = "spcHKrd0liUcl")
    private String spaceId = null;

    @Schema(description = "Integration Message Type (optional)", example = "0")
    private Integer type = 0;

    @Schema(description = "Integration Message Process Code (optional)", example = "PROC-EF6YJL35P2-SCKICSB7P750S0YISYKV3-xxxx-1")
    private String processCode = null;

    @Schema(description = "Integration Message Process component (optional)")
    private JSONArray component;

    @Schema(description = "Start the process user ID (optional)", example = "1261273764218")
    private String originator;

    @Schema(description = "Todo Creator (optional)", example = "1261273764218")
    private String creator;

    @Schema(description = "Todo Subject(optional)", example = "todo subject")
    private String subject = null;

    @Schema(description = "Todo Description", example = "todo description")
    private String description;

    @Schema(description = "Todo ExecutorIds (optional)")
    private List<String> executorIds;

    @Schema(description = "Todo ParticipantIds")
    private List<String> participantIds;
}