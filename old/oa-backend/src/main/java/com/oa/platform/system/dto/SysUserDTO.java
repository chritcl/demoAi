package com.oa.platform.system.dto;

import com.oa.platform.system.entity.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户新增/修改 DTO（含角色）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserDTO extends SysUser {

    /** 角色 ID 列表 */
    private List<Long> roleIds;

    /** 新增时密码 */
    private String password;
}
