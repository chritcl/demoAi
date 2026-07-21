package com.oa.platform.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.security.LoginUser;
import com.oa.platform.system.entity.SysDept;
import com.oa.platform.system.entity.SysUser;
import com.oa.platform.system.mapper.SysDeptMapper;
import com.oa.platform.system.mapper.SysMenuMapper;
import com.oa.platform.system.mapper.SysRoleMapper;
import com.oa.platform.system.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户认证信息加载。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMapper roleMapper;
    private final SysDeptMapper deptMapper;

    public UserDetailsServiceImpl(SysUserMapper userMapper, SysMenuMapper menuMapper,
                                  SysRoleMapper roleMapper, SysDeptMapper deptMapper) {
        this.userMapper = userMapper;
        this.menuMapper = menuMapper;
        this.roleMapper = roleMapper;
        this.deptMapper = deptMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException(ResultCode.LOGIN_ERROR);
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setPassword(user.getPassword());
        loginUser.setStatus(user.getStatus());

        // 部门名称
        if (user.getDeptId() != null) {
            SysDept dept = deptMapper.selectById(user.getDeptId());
            if (dept != null) {
                loginUser.setDeptName(dept.getDeptName());
            }
        }

        // 角色
        List<String> roleKeys = roleMapper.selectRoleKeysByUserId(user.getId());
        loginUser.setRoles(new HashSet<>(roleKeys));

        // 权限：超级管理员拥有全部权限（用 *:*:* 表示）
        if (Constants.SUPER_ADMIN_ID.equals(user.getId())) {
            HashSet<String> all = new HashSet<>();
            all.add("*:*:*");
            loginUser.setPermissions(all);
        } else {
            List<String> perms = menuMapper.selectPermsByUserId(user.getId());
            loginUser.setPermissions(perms.stream().filter(p -> p != null && !p.isBlank())
                    .collect(Collectors.toCollection(HashSet::new)));
        }
        return loginUser;
    }
}
