package com.apitable.integration.service;

import cn.hutool.json.JSONArray;
import com.apitable.integration.dto.AppConfig;
import com.apitable.integration.ro.DingTalkLoginRo;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse.DeptGetResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse.UserGetResponse;
import java.util.List;

/**
 * IDingTalkService.
 */
public interface IDingTalkService {
  
    /**
     * get user info.
     *
     * @param dingTalkLoginRo  dingTalkLoginRo
     */
    Long getUserInfo(DingTalkLoginRo dingTalkLoginRo);

    /**
     * sync DingTalk org.
     *
     * @param spaceId spaceId
     * @return root team Id
     */
    Long sync(String spaceId);

    /**
     * get user detail.
     *
     * @param userId user Id
     * @param appConfig appConfig
     */
    UserGetResponse getUserDetail(String userId, AppConfig appConfig);

    /**
     * get department detail.
     *
     * @param deptId dept Id
     * @param appConfig appConfig
     */
    DeptGetResponse getDepartmentDetail(Long deptId, AppConfig appConfig);

    /**
     * create user with team.
     *
     * @param spaceId spaceId
     * @param appKey appKey
     * @param dingUnionId dingUnionId
     * @param mobile mobile
     * @param email email
     * @param name name
     * @param admin admin
     * @param teamIdList teamIdList
     */
    void createUserWithTeam(String spaceId, String appKey, String dingUnionId, String mobile, String email, String name, boolean admin, List<Long> teamIdList);


    /**
     * userModifyOrg.
     *
     * @param appKey appKey
     * @param spaceId spaceId
     * @param userId userId
     * @param appConfig appConfig
     */
    void userModifyOrg(String appKey, String spaceId, String userId, AppConfig appConfig);

    /**
     * userLeaveOrg.
     *
     * @param appKey appKey
     * @param spaceId spaceId
     * @param userIdArr userIdArr
     * @param appConfig appConfig
     */
    void userLeaveOrg(String appKey, String spaceId, JSONArray userIdArr, AppConfig appConfig);

    /**
     * getAccessToken.
     *
     * @param appConfig appConfig
     * @return accessToken
     */
    String getAccessToken(AppConfig appConfig);

    /**
     * loadProcessList.
     *
     * @return processList
     */
    JSONArray loadProcessList();

    /**
     * loadProcessComponents.
     *
     * @return processComponents
     */
    JSONArray loadProcessComponents(String processCode);
}
