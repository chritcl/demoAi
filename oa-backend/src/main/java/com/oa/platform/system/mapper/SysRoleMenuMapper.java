package com.oa.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.system.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /** 查询角色拥有的菜单ID */
    @Select("SELECT menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<Long> selectMenuIdsByRoleId(Long roleId);
}
