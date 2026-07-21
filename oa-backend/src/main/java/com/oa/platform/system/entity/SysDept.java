package com.oa.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门/组织架构。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class SysDept extends BaseEntity {

    /** 父部门ID */
    private Long parentId;

    /** 祖级列表（逗号分隔） */
    private String ancestors;

    /** 部门名称 */
    private String deptName;

    /** 显示顺序 */
    private Integer sort;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态 0正常 1停用 */
    private Integer status;

    /** 子部门（非持久化） */
    @TableField(exist = false)
    private List<SysDept> children = new ArrayList<>();
}
