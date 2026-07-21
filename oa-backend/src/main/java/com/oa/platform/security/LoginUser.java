package com.oa.platform.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.oa.platform.common.constant.Constants;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录用户主体（实现 Spring Security UserDetails）。
 */
@Data
public class LoginUser implements UserDetails {

    private Long userId;
    private Long deptId;
    private String deptName;
    private String username;
    private String nickname;
    private String avatar;
    private String password;
    private Integer status;

    /** 权限标识集合 */
    private Set<String> permissions;

    /** 角色编码集合 */
    private Set<String> roles;

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (permissions != null) {
            permissions.forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }
        if (roles != null) {
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return status == null || status == 0;
    }

    @JsonIgnore
    public boolean isAdmin() {
        return Constants.SUPER_ADMIN_ID.equals(userId);
    }
}
