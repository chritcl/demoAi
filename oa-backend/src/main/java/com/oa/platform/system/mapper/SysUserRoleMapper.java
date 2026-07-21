package com.oa.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.platform.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /** 查询用户拥有的角色ID */
    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);
}
