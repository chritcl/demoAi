package com.oa.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleKey;

    /** 显示顺序 */
    private Integer sort;

    /** 状态 0正常 1停用 */
    private Integer status;

    /** 数据范围 1全部 2自定义 3本部门 4本部门及以下 5仅本人 */
    private Integer dataScope;

    /** 菜单ID集合（用于角色授权，非持久化） */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.util.List<Long> menuIds;
}
