package com.oa.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper。
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 根据用户ID查询权限标识集合 */
    @Select("SELECT DISTINCT m.perms FROM sys_menu m "
            + "JOIN sys_role_menu rm ON m.id = rm.menu_id "
            + "JOIN sys_user_role ur ON rm.role_id = ur.role_id "
            + "JOIN sys_role r ON r.id = ur.role_id "
            + "WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms <> '' "
            + "AND m.status = 0 AND r.status = 0 AND m.deleted = 0")
    List<String> selectPermsByUserId(Long userId);

    /** 根据用户ID查询其可见菜单 */
    @Select("SELECT DISTINCT m.* FROM sys_menu m "
            + "JOIN sys_role_menu rm ON m.id = rm.menu_id "
            + "JOIN sys_user_role ur ON rm.role_id = ur.role_id "
            + "JOIN sys_role r ON r.id = ur.role_id "
            + "WHERE ur.user_id = #{userId} AND m.menu_type <> 'F' AND m.status = 0 AND r.status = 0 "
            + "AND m.deleted = 0 ORDER BY m.sort")
    List<SysMenu> selectMenusByUserId(Long userId);
}
