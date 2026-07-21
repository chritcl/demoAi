package com.oa.platform.security;

import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.util.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * 权限校验服务，注册为 Bean 名称 "ss"。
 * 用法：@PreAuthorize("@ss.hasPerm('system:user:list')")
 */
@Service("ss")
public class PermissionService {

    /**
     * 判断当前用户是否拥有指定权限。超级管理员直接放行。
     */
    public boolean hasPerm(String perm) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        if (perm == null || perm.isBlank()) {
            return true;
        }
        return user.getPermissions() != null && user.getPermissions().contains("*:*:*")
                || (user.getPermissions() != null && user.getPermissions().contains(perm));
    }

    /**
     * 判断是否为指定角色（roleKey）。
     */
    public boolean hasRole(String roleKey) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        return user.getRoles() != null && user.getRoles().contains(roleKey);
    }

    /**
     * 任意一个角色。
     */
    public boolean hasAnyRole(String... roleKeys) {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) {
            return false;
        }
        if (user.isAdmin()) {
            return true;
        }
        if (user.getRoles() == null) {
            return false;
        }
        for (String k : roleKeys) {
            if (user.getRoles().contains(k)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return Constants.SUPER_ADMIN_ID.equals(SecurityUtils.getCurrentUserId());
    }
}
