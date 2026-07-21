package com.oa.platform.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.oa.platform.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统菜单/权限。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 菜单类型 M目录 C菜单 F按钮 */
    private String menuType;

    /** 前端路由地址 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 权限标识 */
    private String perms;

    /** 路由参数 */
    private String query;

    /** 跳转地址 */
    private String redirect;

    /** 图标 */
    private String icon;

    /** 是否外链 0是 1否 */
    private Integer isFrame;

    /** 是否缓存 0是 1否 */
    private Integer isCache;

    /** 是否隐藏 0显示 1隐藏 */
    private Integer visible;

    /** 显示顺序 */
    private Integer sort;

    /** 状态 0正常 1停用 */
    private Integer status;

    /** 子菜单（非持久化） */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}
