package com.oa.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.system.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper。
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /** 根据用户ID查询角色编码 */
    @Select("SELECT r.role_key FROM sys_role r "
            + "JOIN sys_user_role ur ON r.id = ur.role_id "
            + "WHERE ur.user_id = #{userId} AND r.status = 0 AND r.deleted = 0")
    List<String> selectRoleKeysByUserId(Long userId);
}
