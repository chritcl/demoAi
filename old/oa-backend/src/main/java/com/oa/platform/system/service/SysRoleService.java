package com.oa.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.platform.common.api.PageResult;
import com.oa.platform.common.api.ResultCode;
import com.oa.platform.common.constant.Constants;
import com.oa.platform.common.exception.BusinessException;
import com.oa.platform.common.page.PageQuery;
import com.oa.platform.system.entity.SysRole;
import com.oa.platform.system.entity.SysRoleMenu;
import com.oa.platform.system.entity.SysUserRole;
import com.oa.platform.system.mapper.SysRoleMapper;
import com.oa.platform.system.mapper.SysRoleMenuMapper;
import com.oa.platform.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色服务。
 */
@Service
public class SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;

    public SysRoleService(SysRoleMapper roleMapper, SysRoleMenuMapper roleMenuMapper,
                          SysUserRoleMapper userRoleMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.userRoleMapper = userRoleMapper;
    }

    public PageResult<SysRole> page(PageQuery pageQuery, String roleName, String roleKey, Integer status) {
        Page<SysRole> page = pageQuery.toPage();
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isBlank()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        if (roleKey != null && !roleKey.isBlank()) {
            wrapper.like(SysRole::getRoleKey, roleKey);
        }
        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }
        wrapper.orderByAsc(SysRole::getSort);
        return PageResult.of(roleMapper.selectPage(page, wrapper));
    }

    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 0).orderByAsc(SysRole::getSort));
    }

    public SysRole detail(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXISTS);
        }
        role.setMenuIds(roleMenuMapper.selectMenuIdsByRoleId(id));
        return role;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(SysRole role) {
        checkKeyUnique(role);
        roleMapper.insert(role);
        saveRoleMenus(role.getId(), role.getMenuIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(SysRole role) {
        checkKeyUnique(role);
        roleMapper.updateById(role);
        saveRoleMenus(role.getId(), role.getMenuIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (Constants.ROLE_ADMIN.equals(roleMapper.selectById(id).getRoleKey())) {
            throw new BusinessException("不允许删除超级管理员角色");
        }
        Long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException("该角色已分配给用户，不允许删除");
        }
        roleMapper.deleteById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
    }

    private void checkKeyUnique(SysRole role) {
        LambdaQueryWrapper<SysRole> w = new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleKey, role.getRoleKey());
        if (role.getId() != null) {
            w.ne(SysRole::getId, role.getId());
        }
        if (roleMapper.selectCount(w) > 0) {
            throw new BusinessException(ResultCode.DATA_EXISTS, "角色编码已存在");
        }
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        List<SysRoleMenu> list = new ArrayList<>();
        for (Long menuId : menuIds) {
            list.add(new SysRoleMenu(roleId, menuId));
        }
        for (SysRoleMenu rm : list) {
            roleMenuMapper.insert(rm);
        }
    }
}
