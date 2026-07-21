package com.oa.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.common.util.SecurityUtils;
import com.oa.platform.system.dto.SysUserDTO;
import com.oa.platform.system.entity.SysUser;
import com.oa.platform.system.entity.SysUserRole;
import com.oa.platform.system.mapper.SysUserMapper;
import com.oa.platform.system.mapper.SysUserRoleMapper;
import com.oa.platform.common.util.PinyinUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务。
 */
@Service
public class SysUserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserService(SysUserMapper userMapper, SysUserRoleMapper userRoleMapper,
                          PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<SysUser> page(PageQuery pageQuery, String username, String phone, Integer status, Long deptId) {
        Page<SysUser> page = pageQuery.toPage();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isBlank()) {
            wrapper.like(SysUser::getUsername, username);
        }
        if (phone != null && !phone.isBlank()) {
            wrapper.like(SysUser::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        if (deptId != null) {
            wrapper.eq(SysUser::getDeptId, deptId);
        }
        wrapper.orderByDesc(SysUser::getId);
        IPage<SysUser> result = userMapper.selectPage(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(result);
    }

    public SysUser detail(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        user.setPassword(null);
        user.setPinyin(null);
        return user;
    }

    /** 获取用户角色ID */
    public List<Long> getRoleIds(Long userId) {
        return userRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(SysUserDTO dto) {
        // 唯一性校验
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.DATA_EXISTS, "用户名已存在");
        }
        String pwd = (dto.getPassword() == null || dto.getPassword().isBlank()) ? "123456" : dto.getPassword();
        dto.setPassword(passwordEncoder.encode(pwd));
        dto.setPinyin(PinyinUtil.toPinyin(dto.getNickname()));
        userMapper.insert(dto);
        saveUserRoles(dto.getId(), dto.getRoleIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SysUserDTO dto) {
        dto.setPassword(null); // 不在此处更新密码
        if (dto.getNickname() != null) {
            dto.setPinyin(PinyinUtil.toPinyin(dto.getNickname()));
        }
        userMapper.updateById(dto);
        saveUserRoles(dto.getId(), dto.getRoleIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (Constants.SUPER_ADMIN_ID.equals(id)) {
            throw new BusinessException("不允许删除超级管理员");
        }
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
    }

    public void resetPwd(Long id, String password) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    public void changeStatus(Long id, Integer status) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setStatus(status);
        userMapper.updateById(user);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<SysUserRole> list = new ArrayList<>();
        for (Long roleId : roleIds) {
            list.add(new SysUserRole(userId, roleId));
        }
        // 批量插入
        for (SysUserRole ur : list) {
            userRoleMapper.insert(ur);
        }
    }

    /** 个人信息 */
    public SysUser profile() {
        return detail(SecurityUtils.getCurrentUserId());
    }

    /** 修改个人信息 */
    public void updateProfile(SysUser dto) {
        dto.setId(SecurityUtils.getCurrentUserId());
        dto.setPassword(null);
        if (dto.getNickname() != null) {
            dto.setPinyin(PinyinUtil.toPinyin(dto.getNickname()));
        }
        userMapper.updateById(dto);
    }

    /** 修改密码 */
    public void updatePassword(String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(SecurityUtils.getCurrentUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(update);
    }
}
