package com.apitable.integration.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DingTalkEventEnum.
 */
@Getter
@AllArgsConstructor
public enum DingTalkEventEnum {

    CHECK_URL("check_url"),
    
    USER_ADD_ORG("user_add_org"),

    USER_MODIFY_ORG("user_modify_org"),

    USER_LEAVE_ORG("user_leave_org"),

    USER_ADMIN_ADD("org_admin_add"),

    USER_ADMIN_REMOVE("org_admin_remove"),

    ORG_DEPT_CREATE("org_dept_create"),

    ORG_DEPT_MODIFY("org_dept_modify"),

    ORG_DEPT_REMOVE("org_dept_remove");

    private final String type;

    /**
     * toEnum.
     *
     * @param type type
     * @return DingTalkEventEnum
     */
    public static DingTalkEventEnum toEnum(String type) {
        for (DingTalkEventEnum e : DingTalkEventEnum.values()) {
            if (e.getType().equals(type)) {
                return e;
            }
        }
        return DingTalkEventEnum.CHECK_URL;
    }
}

