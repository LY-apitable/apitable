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

package com.apitable.workspace.controller;

import static com.apitable.shared.constants.PageConstants.PAGE_DESC;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.apitable.control.infrastructure.ControlIdBuilder;
import com.apitable.control.infrastructure.ControlIdBuilder.ControlId;
import com.apitable.control.infrastructure.ControlTemplate;
import com.apitable.control.infrastructure.permission.NodePermission;
import com.apitable.core.support.ResponseData;
import com.apitable.core.util.ExceptionUtil;
import com.apitable.organization.service.IOrganizationService;
import com.apitable.organization.service.IUnitService;
import com.apitable.organization.vo.UnitMemberVo;
import com.apitable.shared.component.scanner.annotation.ApiResource;
import com.apitable.shared.component.scanner.annotation.GetResource;
import com.apitable.shared.component.scanner.annotation.PostResource;
import com.apitable.shared.context.LoginContext;
import com.apitable.shared.context.SessionContext;
import com.apitable.shared.util.page.PageInfo;
import com.apitable.shared.util.page.PageObjectParam;
import com.apitable.space.service.ISpaceRoleService;
import com.apitable.space.service.ISpaceService;
import com.apitable.workspace.enums.PermissionException;
import com.apitable.workspace.ro.AddNodeRoleRo;
import com.apitable.workspace.ro.BatchDeleteNodeRoleRo;
import com.apitable.workspace.ro.BatchModifyNodeRoleRo;
import com.apitable.workspace.ro.DeleteNodeRoleRo;
import com.apitable.workspace.ro.ModifyNodeRoleRo;
import com.apitable.workspace.ro.RoleControlOpenRo;
import com.apitable.workspace.service.INodeRoleService;
import com.apitable.workspace.service.INodeService;
import com.apitable.workspace.vo.NodeCollaboratorsVo;
import com.apitable.workspace.vo.NodeRoleMemberVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Node Role API.
*/
@RestController
@Tag(name = "Workbench - Node Role API")
@ApiResource(path = "/node")
@Validated
public class NodeRoleController {

    @Resource
    private INodeRoleService iNodeRoleService;

    @Resource
    private ControlTemplate controlTemplate;

    @Resource
    private IUnitService iUnitService;

    @Resource
    private INodeService iNodeService;

    @Resource
    private IOrganizationService iOrganizationService;

    @Resource
    private ISpaceRoleService iSpaceRoleService;

    @Resource
    private ISpaceService iSpaceService;

    /**
     * Gets the role infos of datasheet.
    */
    @GetResource(path = "/listRole", requiredPermission = false)
    @Operation(summary = "Gets the role infos of datasheet.")
    public ResponseData<NodeCollaboratorsVo> listRole(
        @RequestParam("nodeId") String nodeId) {
        String spaceId = iNodeService.getSpaceIdByNodeId(nodeId);

        String extendNodeId = iNodeRoleService.getNodeExtendNodeId(nodeId);
        String extendNodeName = "";
        Boolean extend = true;
        if (extendNodeId == null) {
            extendNodeName = iSpaceService.getNameBySpaceId(spaceId);
        } else {
            extendNodeName = iNodeService.getNodeNameByNodeId(extendNodeId);
            extend = !nodeId.equals(extendNodeId);
        }

        List<Long> adminIds = iSpaceRoleService.getSpaceAdminsWithWorkbenchManage(spaceId);
        List<UnitMemberVo> admins = iOrganizationService.findAdminsVo(adminIds, spaceId);

        Boolean isBelongRootFolder = iNodeService.isNodeBelongRootFolder(spaceId, nodeId);
        NodeCollaboratorsVo nodeCollaboratorsVo = new NodeCollaboratorsVo();
        nodeCollaboratorsVo.setMembers(iNodeRoleService.getNodeRoleMembers(spaceId, nodeId));
        nodeCollaboratorsVo.setAdmins(admins);
        nodeCollaboratorsVo.setRoleUnits(iNodeRoleService.getNodeRoleUnitList(nodeId));
        nodeCollaboratorsVo.setSelf(iOrganizationService.finUnitMemberVo(LoginContext.me().getMemberId()));
        nodeCollaboratorsVo.setOwner(iNodeRoleService.getNodeOwner(nodeId));
        nodeCollaboratorsVo.setExtend(extend);
        nodeCollaboratorsVo.setBelongRootFolder(isBelongRootFolder);
        nodeCollaboratorsVo.setExtendNodeName(extendNodeName);

        return ResponseData.success(nodeCollaboratorsVo);
    }

    /**
     * Page Query the Node' Collaborator.
    */
    @GetResource(path = "/collaborator/page")
    @Operation(summary = "Page Query the Node' Collaborator", description = PAGE_DESC)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ResponseData<PageInfo<NodeRoleMemberVo>> getCollaboratorPage(
        @PageObjectParam Page page, @RequestParam("nodeId") String nodeId
    ) {
        Long memberId = LoginContext.me().getMemberId();
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.READ_NODE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_ACCESS_DENIED));
        PageInfo pageInfo = iNodeRoleService.getNodeRoleMembersPageInfo(page, nodeId);
        return ResponseData.success(pageInfo);
    }

    /**
     * Disable Node' Role Extend.
    */
    @PostResource(path = "/disableRoleExtend")
    @Operation(summary = "Disable Node' Role Extend")
    public ResponseData<Void> disableRoleExtend(
        @RequestParam("nodeId") String nodeId, @RequestBody(required = false) RoleControlOpenRo roleControlOpenRo
    ) {
        Long memberId = LoginContext.me().getMemberId();
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.READ_NODE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_ACCESS_DENIED));

        String spaceId = iNodeService.getSpaceIdByNodeId(nodeId);
        boolean includeExtend = ObjectUtil.isNotNull(roleControlOpenRo)
            && BooleanUtil.isTrue(roleControlOpenRo.getIncludeExtend());
        iNodeRoleService.enableNodeRole(LoginContext.me().getLoginUser().getUserId(), spaceId, nodeId, includeExtend);
        return ResponseData.success();
    }

    /**
     * Enable Node' Role Extend.
    */
    @PostResource(path = "/enableRoleExtend")
    @Operation(summary = "Enable Node' Role Extend")
    public ResponseData<Void> enableRoleExtend(@RequestParam("nodeId") String nodeId) {
        Long memberId = LoginContext.me().getMemberId();
        controlTemplate.checkNodePermission(memberId, nodeId, NodePermission.READ_NODE,
            status -> ExceptionUtil.isTrue(status, PermissionException.NODE_ACCESS_DENIED));

        iNodeRoleService.disableNodeRole(LoginContext.me().getLoginUser().getUserId(), nodeId);
        return ResponseData.success();
    }

    /**
     * Add node role.
    */
    @PostResource(path = "/addRole", requiredPermission = false)
    @Operation(summary = "Add node role")
    public ResponseData<Void> addRole(@Valid @RequestBody AddNodeRoleRo data) {
        // whether the field is already closed to prevent repeated operations
        ControlId controlId = ControlIdBuilder.nodeId(data.getNodeId());
        // check whether the added organizational unit id has the current space
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        iUnitService.checkInSpace(spaceId, data.getUnitIds());
        // add node role
        iNodeRoleService.addNodeRole(SessionContext.getUserId(), controlId.toString(), data.getRole(), data.getUnitIds());
        return ResponseData.success();
    }

    /**
     * Edit node role.
    */
    @PostResource(path = "/editRole", requiredPermission = false)
    @Operation(summary = "Edit node role")
    @Deprecated
    public ResponseData<Void> editRole(@RequestBody @Valid ModifyNodeRoleRo data) {
        BatchModifyNodeRoleRo ro = new BatchModifyNodeRoleRo();
        ro.setUnitIds(CollUtil.newArrayList(data.getUnitId()));
        ro.setRole(data.getRole());
        ro.setNodeId(data.getNodeId());
        return batchEditRole(ro);
    }

    /**
     * Batch edit node role.
    */
    @PostResource(path = "/batchEditRole", requiredPermission = false)
    @Operation(summary = "Batch edit node role")
    public ResponseData<Void> batchEditRole(@RequestBody @Valid BatchModifyNodeRoleRo data) {
        // Check whether the added organizational unit id has the current space
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        iUnitService.checkInSpace(spaceId, data.getUnitIds());
        // edit role
        iNodeRoleService.updateNodeRole(SessionContext.getUserId(), data.getNodeId(), data.getRole(), data.getUnitIds());
        return ResponseData.success();
    }

    /**
     * Delete node role.
    */
    @PostResource(path = "/deleteRole",
        method = RequestMethod.DELETE, requiredPermission = false)
    @Operation(summary = "Delete node role")
    public ResponseData<Void> deleteRole(@RequestBody @Valid DeleteNodeRoleRo data) {
        // check whether the added organizational unit id has the current space
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        iUnitService.checkInSpace(spaceId, Collections.singletonList(data.getUnitId()));
        // deletes the role of the specified unit
        iNodeRoleService.deleteNodeRole(SessionContext.getUserId(), data.getNodeId(), data.getUnitId());
        return ResponseData.success();
    }

    /**
     * Batch delete role.
    */
    @PostResource(path = "/batchDeleteRole",
        method = RequestMethod.DELETE, requiredPermission = false)
    @Operation(summary = "Batch delete role")
    public ResponseData<Void> batchDeleteRole(@RequestBody @Valid BatchDeleteNodeRoleRo data) {
        // check whether the added organizational unit id has the current space
        String spaceId = iNodeService.getSpaceIdByNodeId(data.getNodeId());
        iUnitService.checkInSpace(spaceId, data.getUnitIds());
        iNodeRoleService.deleteNodeRoles(data.getNodeId(), data.getUnitIds());
        return ResponseData.success();
    }
}