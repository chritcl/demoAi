package com.oa.platform.common.util;

import com.oa.platform.common.constant.Constants;
import com.oa.platform.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具：获取当前登录用户。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static LoginUser getLoginUser() {
        Authentication auth = getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    public static Long getCurrentUserId() {
        LoginUser user = getLoginUser();
        return user == null ? null : user.getUserId();
    }

    public static Long getCurrentDeptId() {
        LoginUser user = getLoginUser();
        return user == null ? null : user.getDeptId();
    }

    public static String getCurrentUsername() {
        LoginUser user = getLoginUser();
        return user == null ? "system" : user.getUsername();
    }

    public static boolean isAdmin() {
        Long id = getCurrentUserId();
        return id != null && Constants.SUPER_ADMIN_ID.equals(id);
    }
}
