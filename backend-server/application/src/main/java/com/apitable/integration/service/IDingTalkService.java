package com.apitable.integration.service;

import java.util.List;

import com.apitable.integration.dto.AppConfig;
import com.apitable.integration.ro.DingTalkLoginRo;
import com.dingtalk.api.response.OapiV2DepartmentGetResponse.DeptGetResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse.UserGetResponse;

import cn.hutool.json.JSONArray;

public interface IDingTalkService {
  
    /**
     * get user info
     *
     * @param dingTalkLoginRo  dingTalkLoginRo
     */
    Long getUserInfo(DingTalkLoginRo dingTalkLoginRo);

    /**
     * "sync DingTalk org
     * 
     * @param spaceId spaceId
     * @return root team Id
     */
    Long sync(String spaceId);

    /**
     * get user detail
     * 
     * @param userId
     * @param appConfig
     */
    UserGetResponse getUserDetail(String userId, AppConfig appConfig);

    /**
     * get department detail
     * 
     * @param deptId
     * @param appConfig
     */
    DeptGetResponse getDepartmentDetail(Long deptId, AppConfig appConfig);

    /**
     * create user with team
     * 
     * @param spaceId
     * @param appKey
     * @param dingUnionId
     * @param mobile
     * @param email
     * @param name
     * @param admin
     * @param teamIdList
     */
    void createUserWithTeam(String spaceId, String appKey, String dingUnionId, String mobile, String email, String name, boolean admin, List<Long> teamIdList);


    /**
     * @param appKey
     * @param spaceId
     * @param userId
     * @param appConfig
     */
    void userModifyOrg(String appKey, String spaceId, String userId, AppConfig appConfig);

    /**
     * @param appKey
     * @param spaceId
     * @param userIdArr
     * @param appConfig
     */
    void userLeaveOrg(String appKey, String spaceId, JSONArray userIdArr, AppConfig appConfig);
}
