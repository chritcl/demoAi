package com.oa.platform.security.vo;

import lombok.Data;

import java.util.Set;

/**
 * 当前登录用户信息。
 */
@Data
public class UserInfoVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Long deptId;
    private String deptName;

    private Set<String> roles;
    private Set<String> permissions;
}
