package com.oa.platform.common.constant;

/**
 * 系统常量。
 */
public final class Constants {

    private Constants() {
    }

    /** 超级管理员角色编码 */
    public static final String ROLE_ADMIN = "admin";

    /** 超级管理员用户ID */
    public static final Long SUPER_ADMIN_ID = 1L;

    /** 缓存 - 用户登录信息前缀 */
    public static final String CACHE_LOGIN_USER = "oa:login:user:";

    /** 缓存 - 验证码前缀 */
    public static final String CACHE_CAPTCHA = "oa:captcha:";

    /** 缓存 - 菜单/权限前缀 */
    public static final String CACHE_USER_PERMS = "oa:user:perms:";

    /** Token 黑名单前缀 */
    public static final String CACHE_TOKEN_BLACKLIST = "oa:token:blacklist:";

    /** 流程状态 - 运行中 */
    public static final String FLOW_RUNNING = "running";
    /** 流程状态 - 已完成 */
    public static final String FLOW_DONE = "done";
    /** 流程状态 - 已终止/驳回 */
    public static final String FLOW_TERMINATED = "terminated";

    /** 任务状态 - 待办 */
    public static final String TASK_PENDING = "pending";
    /** 任务状态 - 已办(通过) */
    public static final String TASK_DONE = "done";
    /** 任务状态 - 驳回 */
    public static final String TASK_REJECTED = "rejected";
    /** 任务状态 - 转办 */
    public static final String TASK_TRANSFERRED = "transferred";

    /** 菜单类型 - 目录 */
    public static final String MENU_DIR = "M";
    /** 菜单类型 - 菜单 */
    public static final String MENU_MENU = "C";
    /** 菜单类型 - 按钮 */
    public static final String MENU_BTN = "F";
}
