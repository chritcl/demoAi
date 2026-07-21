package com.oa.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.system.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户 Mapper。
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 根据角色编码查询用户ID（取启用用户） */
    @Select("SELECT u.id FROM sys_user u "
            + "JOIN sys_user_role ur ON u.id = ur.user_id "
            + "JOIN sys_role r ON r.id = ur.role_id "
            + "WHERE r.role_key = #{roleKey} AND u.status = 0 AND u.deleted = 0 "
            + "ORDER BY u.id LIMIT 1")
    Long selectUserIdByRoleKey(String roleKey);
}

