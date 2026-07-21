-- =================================================================
-- 该文件仅用于字段、表关系和历史业务参考，
-- 不得直接用于新系统建库。
-- 新数据库结构必须通过 Flyway 迁移脚本重新设计。
-- 参见：docs/ARCHITECTURE.md
-- =================================================================
-- ============================================================================
-- 协同办公平台 (OA Platform) 完整建库脚本
-- 数据库: oa_platform  |  MySQL 8.x  |  字符集: utf8mb4
--
-- 说明：
--   1. 本脚本依据《OA报价20270714.xlsx》全功能清单设计，覆盖 10 大模块全部二级功能。
--   2. 全部表使用 CREATE TABLE IF NOT EXISTS，幂等可重复执行；与工程现有
--      oa-backend/src/main/resources/schema.sql 完全兼容（已建表会跳过）。
--   3. 表分两类标记：
--      [基础] = 工程已在 schema.sql 中存在（此处保留以保证脚本自包含、可直接建新库）
--      [扩展] = 报价清单新增/补全能力（安全策略、公文交换、委托授权、考勤规则等）
--   4. 公共审计字段约定：create_time / create_by / update_time / update_by / deleted / remark
--      软删除 deleted：0 未删 1 已删；status：0 正常 1 停用（特殊业务字段在 COMMENT 注明）
--   5. 2026-07 复核修订（依据《功能开发清单.xlsx》涉及表列 + docs/oa 流程文档）：
--      - 补 portal_read_record（信息发布阅读记录，流程 01 规则 6）
--      - 补 office_seal_record（用印台账，流程 06 规则 4；office_seal_registry 回归印章主数据）
--      - sys_user 补 address/fax/duty（工作台-个人信息：地址/电话传真/职责描述）
-- ============================================================================

CREATE DATABASE IF NOT EXISTS oa_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE oa_platform;

SET NAMES utf8mb4;

-- ============================================================================
-- 一、应用支撑平台 · 组织 / 用户 / 权限 [基础]
-- ============================================================================

-- [基础] 系统用户
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    dept_id         BIGINT          DEFAULT NULL COMMENT '部门ID',
    username        VARCHAR(50)     NOT NULL COMMENT '用户名',
    nickname        VARCHAR(50)     DEFAULT NULL COMMENT '昵称',
    password        VARCHAR(100)    DEFAULT NULL COMMENT '密码(BCrypt)',
    email           VARCHAR(100)    DEFAULT NULL COMMENT '邮箱',
    phone           VARCHAR(20)     DEFAULT NULL COMMENT '手机',
    address         VARCHAR(200)    DEFAULT NULL COMMENT '个人地址(工作台-个人信息)',
    fax             VARCHAR(30)     DEFAULT NULL COMMENT '电话传真(工作台-个人信息)',
    duty            VARCHAR(200)    DEFAULT NULL COMMENT '职责描述(工作台-个人信息)',
    avatar          VARCHAR(255)    DEFAULT NULL COMMENT '头像',
    gender          TINYINT         DEFAULT 2 COMMENT '性别 0男1女2未知',
    status          TINYINT         DEFAULT 0 COMMENT '状态 0正常1停用',
    login_ip        VARCHAR(50)     DEFAULT NULL COMMENT '最后登录IP',
    login_date      DATETIME        DEFAULT NULL COMMENT '最后登录时间',
    pinyin          VARCHAR(100)    DEFAULT NULL COMMENT '姓名拼音(通讯录检索)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT         DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500)    DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_dept (dept_id),
    KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- [基础] 角色
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_name       VARCHAR(50)     NOT NULL COMMENT '角色名',
    role_key        VARCHAR(50)     NOT NULL COMMENT '角色编码',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT         DEFAULT 0 COMMENT '状态(0正常 1停用)',
    data_scope      TINYINT         DEFAULT 1 COMMENT '数据范围 1全部2自定义3本部门4本部门及以下5仅本人',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT         DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500)    DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- [基础] 菜单/权限
CREATE TABLE IF NOT EXISTS sys_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父级ID',
    menu_name       VARCHAR(50)     NOT NULL COMMENT '菜单名称',
    menu_type       CHAR(1)         DEFAULT 'C' COMMENT 'M目录 C菜单 F按钮',
    path            VARCHAR(200)    DEFAULT NULL COMMENT '路由地址',
    component       VARCHAR(255)    DEFAULT NULL COMMENT '组件路径',
    perms           VARCHAR(100)    DEFAULT NULL COMMENT '权限标识',
    query           VARCHAR(255)    DEFAULT NULL COMMENT '路由参数',
    redirect        VARCHAR(200)    DEFAULT NULL COMMENT '重定向地址',
    icon            VARCHAR(50)     DEFAULT NULL COMMENT '图标',
    is_frame        TINYINT         DEFAULT 1 COMMENT '0外链1否',
    is_cache        TINYINT         DEFAULT 0 COMMENT '0缓存1否',
    visible         TINYINT         DEFAULT 0 COMMENT '0显示1隐藏',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT         DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT         DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500)    DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限';

-- [基础] 部门
CREATE TABLE IF NOT EXISTS sys_dept (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父级ID',
    ancestors       VARCHAR(200)    DEFAULT NULL COMMENT '祖级列表',
    dept_name       VARCHAR(50)     NOT NULL COMMENT '部门名称',
    sort            INT             DEFAULT 0 COMMENT '排序',
    leader          VARCHAR(50)     DEFAULT NULL COMMENT '负责人',
    phone           VARCHAR(20)     DEFAULT NULL COMMENT '联系电话',
    email           VARCHAR(100)    DEFAULT NULL COMMENT '邮箱',
    status          TINYINT         DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT          DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT          DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT         DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500)    DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';

-- [基础] 用户-角色
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色';

-- [基础] 角色-菜单
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单';

-- [扩展] 角色-部门(数据范围：data_scope=2 自定义时使用)
CREATE TABLE IF NOT EXISTS sys_role_dept (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    PRIMARY KEY (role_id, dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-部门(数据权限)';

-- [扩展] 组织节点可见性(通讯录组织节点保护)：角色对部门的可见性规则
CREATE TABLE IF NOT EXISTS sys_dept_visibility (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id         BIGINT DEFAULT NULL COMMENT '角色ID(为空表示全局规则)',
    dept_id         BIGINT NOT NULL COMMENT '部门节点',
    visible         TINYINT DEFAULT 0 COMMENT '0可见1不可见',
    scope_mode      VARCHAR(20) DEFAULT 'hide' COMMENT 'hide隐藏 mask脱敏',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id),
    KEY idx_dept (dept_id),
    KEY idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织节点可见性规则';

-- [基础] 字典类型 / 字典数据
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    dict_name   VARCHAR(50) NOT NULL COMMENT '字典名称',
    dict_type   VARCHAR(50) NOT NULL COMMENT '字典类型',
    status      TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by   BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by   BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted     TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    dict_type   VARCHAR(50) NOT NULL COMMENT '字典类型',
    dict_label  VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value  VARCHAR(100) NOT NULL COMMENT '字典键值',
    list_class  VARCHAR(20) DEFAULT NULL COMMENT '样式类型',
    sort        INT DEFAULT 0 COMMENT '排序',
    status      TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    is_default  TINYINT DEFAULT 1 COMMENT '是否默认(0否 1是)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by   BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by   BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted     TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据';

-- [基础] 操作日志
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    title           VARCHAR(50) DEFAULT NULL COMMENT '标题',
    business_type   INT DEFAULT 0 COMMENT '0其它1新增2修改3删除…',
    method          VARCHAR(200) DEFAULT NULL COMMENT '方法名',
    request_method  VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
    oper_name       VARCHAR(50) DEFAULT NULL COMMENT '操作人员',
    oper_id         BIGINT DEFAULT NULL COMMENT '操作人ID',
    oper_url        VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    oper_param      TEXT DEFAULT NULL COMMENT '请求参数',
    json_result     TEXT DEFAULT NULL COMMENT '返回结果',
    status          TINYINT DEFAULT 0 COMMENT '0正常1异常',
    error_msg       VARCHAR(2000) DEFAULT NULL COMMENT '错误消息',
    oper_ip         VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    oper_time       DATETIME DEFAULT NULL COMMENT '操作时间',
    cost_time       BIGINT DEFAULT NULL COMMENT '耗时(毫秒)',
    PRIMARY KEY (id),
    KEY idx_oper_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- [扩展] 登录日志(登录/退出审计，与操作日志分离)
CREATE TABLE IF NOT EXISTS sys_login_log (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    user_id         BIGINT DEFAULT NULL COMMENT '用户ID',
    login_type      VARCHAR(20) DEFAULT 'login' COMMENT 'login登录 logout退出',
    ipaddr          VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    login_location  VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    browser         VARCHAR(50) DEFAULT NULL COMMENT '浏览器',
    os              VARCHAR(50) DEFAULT NULL COMMENT '操作系统',
    platform        VARCHAR(20) DEFAULT 'pc' COMMENT 'pc/mobile/android/ios/harmony',
    status          TINYINT DEFAULT 0 COMMENT '0成功1失败',
    msg             VARCHAR(500) DEFAULT NULL COMMENT '提示消息',
    login_time      DATETIME DEFAULT NULL COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志';

-- [扩展] 安全审计日志(登录/消息/文件/群组/个人配置/管理员操作审计)
CREATE TABLE IF NOT EXISTS sys_audit_log (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    audit_type      VARCHAR(30) NOT NULL COMMENT 'login/message/file/group/profile/admin',
    user_id         BIGINT DEFAULT NULL COMMENT '用户ID',
    user_name       VARCHAR(50) DEFAULT NULL COMMENT '用户名/姓名',
    action          VARCHAR(100) DEFAULT NULL COMMENT '操作动作',
    target_type     VARCHAR(50) DEFAULT NULL COMMENT '目标对象类型',
    target_id       VARCHAR(64) DEFAULT NULL COMMENT '目标对象ID',
    detail          TEXT DEFAULT NULL COMMENT '审计详情',
    ipaddr          VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    audit_time      DATETIME DEFAULT NULL COMMENT '审计时间',
    PRIMARY KEY (id),
    KEY idx_type_time (audit_type, audit_time),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全审计日志';

-- [基础] 站内消息
CREATE TABLE IF NOT EXISTS sys_message (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT NOT NULL COMMENT '接收人',
    title           VARCHAR(200) DEFAULT NULL COMMENT '标题',
    content         TEXT DEFAULT NULL COMMENT '内容',
    type            VARCHAR(20) DEFAULT 'system' COMMENT 'system/todo/notice/news',
    business_type   VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    business_id     BIGINT DEFAULT NULL COMMENT '业务ID',
    is_read         TINYINT DEFAULT 0 COMMENT '是否已读(0否 1是)',
    send_user_id    BIGINT DEFAULT NULL COMMENT '发送人ID',
    send_user_name  VARCHAR(50) DEFAULT NULL COMMENT '发送人姓名',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- [基础] 文件
CREATE TABLE IF NOT EXISTS sys_file (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    original_name   VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    file_name       VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    file_path       VARCHAR(500) DEFAULT NULL COMMENT '文件路径',
    url             VARCHAR(500) DEFAULT NULL COMMENT '访问URL',
    size            BIGINT DEFAULT NULL COMMENT '文件大小(字节)',
    content_type    VARCHAR(100) DEFAULT NULL COMMENT '文件类型',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    upload_day      DATE DEFAULT NULL COMMENT '上传日期',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件';

-- [扩展] 文件服务配置(存储位置/存储服务/大小/格式)
CREATE TABLE IF NOT EXISTS sys_file_config (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    config_name     VARCHAR(50) NOT NULL COMMENT '配置项名',
    config_key      VARCHAR(50) NOT NULL COMMENT '配置键',
    config_value    VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    value_type      VARCHAR(20) DEFAULT 'string' COMMENT 'string/number/boolean/json',
    max_size        BIGINT DEFAULT 104857600 COMMENT '允许上传大小(字节)',
    allow_types     VARCHAR(1000) DEFAULT NULL COMMENT '允许格式(逗号分隔)',
    storage_type    VARCHAR(20) DEFAULT 'local' COMMENT 'local/oss/minio',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件服务配置';

-- ============================================================================
-- 二、应用支撑平台 · 安全策略 [扩展]
-- ============================================================================

-- [扩展] 安全策略统一配置(水印/截屏/密码策略 等键值化)
CREATE TABLE IF NOT EXISTS sys_security_policy (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    policy_key      VARCHAR(50) NOT NULL COMMENT '策略键 water_mark/screenshot/password/blacklist...',
    policy_value    VARCHAR(2000) DEFAULT NULL COMMENT '策略值',
    scope           VARCHAR(20) DEFAULT 'global' COMMENT 'global/user/role/dept',
    scope_value     VARCHAR(200) DEFAULT NULL COMMENT '范围目标ID列表',
    enabled         TINYINT DEFAULT 0 COMMENT '0启用1禁用',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_policy_key_scope (policy_key, scope, scope_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全策略配置';

-- [扩展] 登录黑白名单(账号/IP；清单中 sys_login_blacklist/sys_login_whitelist
--         两表结构完全一致，合并为本表，用 list_type 区分 black/white，减少一套重复代码)
CREATE TABLE IF NOT EXISTS sys_login_blacklist (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    list_type       VARCHAR(10) NOT NULL COMMENT 'black黑名单 white白名单',
    target_type     VARCHAR(10) NOT NULL COMMENT 'account账号 ip地址',
    target_value    VARCHAR(100) NOT NULL COMMENT '账号或IP/CIDR',
    expire_time     DATETIME DEFAULT NULL COMMENT '失效时间(空=永久)',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_type_target (list_type, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录黑白名单';

-- [扩展] 截屏日志(禁止截屏策略的后台留痕)
CREATE TABLE IF NOT EXISTS sys_screenshot_log (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT DEFAULT NULL COMMENT '用户ID',
    user_name       VARCHAR(50) DEFAULT NULL COMMENT '用户名/姓名',
    page_url        VARCHAR(500) DEFAULT NULL COMMENT '页面URL',
    device_id       VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
    platform        VARCHAR(20) DEFAULT NULL COMMENT '客户端平台',
    ipaddr          VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    screenshot_time DATETIME DEFAULT NULL COMMENT '截屏时间',
    PRIMARY KEY (id),
    KEY idx_user_time (user_id, screenshot_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='截屏日志';

-- [扩展] 用户安全设置(手势/指纹密码、绑定设备)
CREATE TABLE IF NOT EXISTS sys_user_security (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    gesture_pwd    VARCHAR(200) DEFAULT NULL COMMENT '手势密码(加密)',
    biometric_en    TINYINT DEFAULT 0 COMMENT '是否启用指纹/面容 0否1是',
    device_id       VARCHAR(100) DEFAULT NULL COMMENT '设备ID',
    device_name     VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    platform        VARCHAR(20) DEFAULT NULL COMMENT '客户端平台',
    last_verify     DATETIME DEFAULT NULL COMMENT '最后验证时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_device (user_id, device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户安全设置(手势/指纹)';

-- [扩展] 应用中心·应用注册(统一门户应用中心入口)
CREATE TABLE IF NOT EXISTS sys_app (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    app_name        VARCHAR(100) NOT NULL COMMENT '应用名称',
    app_code        VARCHAR(50) DEFAULT NULL COMMENT '应用编码',
    app_url         VARCHAR(500) DEFAULT NULL COMMENT '应用地址',
    category        VARCHAR(50) DEFAULT NULL COMMENT '分类',
    icon            VARCHAR(255) DEFAULT NULL COMMENT '图标',
    app_type        VARCHAR(20) DEFAULT 'inner' COMMENT 'inner内置 outer外链',
    level           TINYINT DEFAULT 0 COMMENT '分级 0普通1重要',
    visible_roles   VARCHAR(500) DEFAULT NULL COMMENT '可见角色ID列表',
    sort            INT DEFAULT 0 COMMENT '排序',
    status          TINYINT DEFAULT 0 COMMENT '0上架1下线',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_code (app_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用中心-应用注册';

-- [扩展] APP 客户端版本管理(灰度/差分/断点续传)
CREATE TABLE IF NOT EXISTS sys_app_version (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    platform        VARCHAR(20) NOT NULL COMMENT 'android/ios/harmony',
    version_code    INT NOT NULL COMMENT '版本号(数字)',
    version_name    VARCHAR(20) NOT NULL COMMENT '版本名',
    package_url     VARCHAR(500) DEFAULT NULL COMMENT '安装包地址',
    patch_url       VARCHAR(500) DEFAULT NULL COMMENT '差分包地址',
    upgrade_desc    VARCHAR(1000) DEFAULT NULL COMMENT '升级说明',
    force_upgrade   TINYINT DEFAULT 0 COMMENT '0否1强制',
    gray_release    TINYINT DEFAULT 0 COMMENT '0全量1灰度',
    gray_percent    INT DEFAULT 100 COMMENT '灰度比例',
    gray_scope      VARCHAR(500) DEFAULT NULL COMMENT '灰度范围(用户/角色/部门)',
    status          TINYINT DEFAULT 0 COMMENT '0已发布1已下架',
    publish_time    DATETIME DEFAULT NULL COMMENT '发布时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id),
    KEY idx_platform_code (platform, version_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP客户端版本';

-- [扩展] APP 升级日志
CREATE TABLE IF NOT EXISTS sys_app_upgrade_log (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT DEFAULT NULL COMMENT '用户ID',
    platform        VARCHAR(20) DEFAULT NULL COMMENT '客户端平台',
    from_version    VARCHAR(20) DEFAULT NULL COMMENT '升级前版本',
    to_version      VARCHAR(20) DEFAULT NULL COMMENT '升级后版本',
    upgrade_type    VARCHAR(20) DEFAULT 'full' COMMENT 'full全量 patch差分',
    status          TINYINT DEFAULT 0 COMMENT '0成功1失败',
    upgrade_time    DATETIME DEFAULT NULL COMMENT '升级时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='APP升级日志';

-- [扩展] 意见常用语(公文/审批批示用语库)
CREATE TABLE IF NOT EXISTS sys_opinion_phrase (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    content         VARCHAR(500) NOT NULL COMMENT '常用语内容',
    scope           VARCHAR(20) DEFAULT 'public' COMMENT 'public公共 private个人',
    user_id         BIGINT DEFAULT NULL COMMENT '个人时归属用户',
    sort            INT DEFAULT 0 COMMENT '排序',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id),
    KEY idx_scope_user (scope, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见常用语';

-- ============================================================================
-- 三、统一门户 · 通知公告 / 信息发布 [基础+扩展]
-- ============================================================================

-- [基础] 通知公告
CREATE TABLE IF NOT EXISTS portal_notice (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    summary         VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    content         LONGTEXT DEFAULT NULL COMMENT '公告正文',
    category        VARCHAR(50) DEFAULT NULL COMMENT '分类',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1已发布2撤回',
    top             TINYINT DEFAULT 0 COMMENT '是否置顶(0否 1是)',
    cover           VARCHAR(255) DEFAULT NULL COMMENT '封面',
    read_count      INT DEFAULT 0 COMMENT '阅读次数',
    publish_user_id BIGINT DEFAULT NULL COMMENT '发布人ID',
    publish_user_name VARCHAR(50) DEFAULT NULL COMMENT '发布人姓名',
    publish_time    DATETIME DEFAULT NULL COMMENT '发布时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告';

-- [扩展] 公告阅读记录(阅读统计按人去重)
CREATE TABLE IF NOT EXISTS portal_notice_read (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    notice_id       BIGINT NOT NULL COMMENT '公告ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    read_time       DATETIME DEFAULT NULL COMMENT '阅读时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_notice_user (notice_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告阅读记录';

-- [扩展] 信息发布栏目
CREATE TABLE IF NOT EXISTS portal_column (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_id       BIGINT DEFAULT 0 COMMENT '父级ID',
    column_name     VARCHAR(100) NOT NULL COMMENT '栏目名称',
    column_code     VARCHAR(50) DEFAULT NULL COMMENT '栏目编码',
    sort            INT DEFAULT 0 COMMENT '排序',
    visible_roles   VARCHAR(500) DEFAULT NULL COMMENT '查看权限',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信息发布栏目';

-- [基础] 信息发布文章
CREATE TABLE IF NOT EXISTS portal_article (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    summary         VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    content         LONGTEXT DEFAULT NULL COMMENT '文章正文',
    column_id       BIGINT DEFAULT NULL COMMENT '栏目ID',
    category        VARCHAR(50) DEFAULT NULL COMMENT '分类',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1待审2已发布3驳回',
    top             TINYINT DEFAULT 0 COMMENT '是否置顶(0否 1是)',
    author          VARCHAR(50) DEFAULT NULL COMMENT '作者',
    cover           VARCHAR(255) DEFAULT NULL COMMENT '封面',
    view_count      INT DEFAULT 0 COMMENT '查看次数',
    download_count  INT DEFAULT 0 COMMENT '下载次数',
    publish_user_id BIGINT DEFAULT NULL COMMENT '发布人ID',
    audit_user_id   BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_user_name VARCHAR(50) DEFAULT NULL COMMENT '审核人姓名',
    audit_comment   VARCHAR(500) DEFAULT NULL COMMENT '审核意见(驳回原因)',
    read_expire     DATETIME DEFAULT NULL COMMENT '阅读时效(过期不可见)',
    visible_scope   VARCHAR(500) DEFAULT NULL COMMENT '查看权限范围',
    publish_time    DATETIME DEFAULT NULL COMMENT '发布时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_column (column_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信息发布文章';

-- [扩展] 文章附件(下载次数/下载人员统计)
CREATE TABLE IF NOT EXISTS portal_article_file (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id      BIGINT NOT NULL COMMENT '文章ID',
    file_id         BIGINT NOT NULL COMMENT '文件ID',
    file_name       VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    download_count  INT DEFAULT 0 COMMENT '下载次数',
    sort            INT DEFAULT 0 COMMENT '排序',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_article (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信息发布文章附件';

-- [扩展] 文章下载人员记录
CREATE TABLE IF NOT EXISTS portal_article_download (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id      BIGINT NOT NULL COMMENT '文章ID',
    file_id         BIGINT DEFAULT NULL COMMENT '文件ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    user_name       VARCHAR(50) DEFAULT NULL COMMENT '用户名/姓名',
    download_time   DATETIME DEFAULT NULL COMMENT '下载时间',
    PRIMARY KEY (id),
    KEY idx_article_user (article_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章下载人员记录';

-- [扩展] 文章阅读记录(阅读统计按人去重，见流程设计/01 规则6)
CREATE TABLE IF NOT EXISTS portal_read_record (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    article_id      BIGINT NOT NULL COMMENT '文章ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    read_time       DATETIME DEFAULT NULL COMMENT '最近阅读时间',
    read_count      INT DEFAULT 1 COMMENT '阅读次数',
    PRIMARY KEY (id),
    UNIQUE KEY uk_article_user (article_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章阅读记录';

-- ============================================================================
-- 四、公文管理 / 公文交换 [基础+扩展]
-- ============================================================================

-- [基础] 公文(发文/收文)
CREATE TABLE IF NOT EXISTS doc_official (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_type        VARCHAR(10) DEFAULT 'send' COMMENT 'send发文 receive收文 sign签报(二期公文统计口径扩展)',
    doc_no          VARCHAR(50) DEFAULT NULL COMMENT '文号',
    doc_category    VARCHAR(50) DEFAULT NULL COMMENT '文种',
    seq_no          INT DEFAULT NULL COMMENT '流水号',
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    urgency         TINYINT DEFAULT 0 COMMENT '0普通1紧急2特急',
    secrecy         TINYINT DEFAULT 0 COMMENT '0公开1秘密2机密',
    content         LONGTEXT DEFAULT NULL COMMENT '公文正文',
    attachments     VARCHAR(1000) DEFAULT NULL COMMENT '附件',
    drafter_user_id BIGINT DEFAULT NULL COMMENT '拟稿人ID',
    drafter_name    VARCHAR(50) DEFAULT NULL COMMENT '拟稿人姓名',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    from_unit       VARCHAR(100) DEFAULT NULL COMMENT '来文单位(收文)',
    recipient_scope VARCHAR(255) DEFAULT NULL COMMENT '传阅范围',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2已发布3驳回(收文:1已登记2办理中3归档)',
    publish_date    DATE DEFAULT NULL COMMENT '发布日期',
    archived        TINYINT DEFAULT 0 COMMENT '0未归档1已归档',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_doctype_status (doc_type, status),
    KEY idx_doc_no (doc_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文';

-- [扩展] 文号规则(自动/手动生成文号及文种)
CREATE TABLE IF NOT EXISTS doc_no_rule (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    rule_name       VARCHAR(100) NOT NULL COMMENT '规则名称',
    doc_category    VARCHAR(50) NOT NULL COMMENT '文种',
    prefix_format   VARCHAR(100) DEFAULT NULL COMMENT '前缀格式(含{year}{dept})',
    year_seq        INT DEFAULT 0 COMMENT '当前年度流水',
    seq_width       INT DEFAULT 4 COMMENT '流水位数',
    auto_gen        TINYINT DEFAULT 1 COMMENT '0手动1自动',
    dept_id         BIGINT DEFAULT NULL COMMENT '适用部门(空=全部)',
    current_year    INT DEFAULT NULL COMMENT '当前年度',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id),
    KEY idx_category (doc_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文号规则';

-- [扩展] 发文模板
CREATE TABLE IF NOT EXISTS doc_send_template (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    template_name   VARCHAR(100) NOT NULL COMMENT '模板名称',
    doc_category    VARCHAR(50) DEFAULT NULL COMMENT '文种',
    content         LONGTEXT DEFAULT NULL COMMENT '正文模板',
    flow_key        VARCHAR(50) DEFAULT NULL COMMENT '绑定流程',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发文模板';

-- [扩展] 公文办理意见(每环节批示意见留痕)
CREATE TABLE IF NOT EXISTS doc_opinion (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id          BIGINT NOT NULL COMMENT '公文ID',
    node_name       VARCHAR(100) DEFAULT NULL COMMENT '环节名',
    opinion_type    VARCHAR(20) DEFAULT 'approve' COMMENT 'approve同意 reject驳回 countersign会签 comment批示',
    user_id         BIGINT DEFAULT NULL COMMENT '用户ID',
    user_name       VARCHAR(50) DEFAULT NULL COMMENT '用户名/姓名',
    content         VARCHAR(1000) DEFAULT NULL COMMENT '意见内容',
    opinion_time    DATETIME DEFAULT NULL COMMENT '意见时间',
    PRIMARY KEY (id),
    KEY idx_doc (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文办理意见';

-- [扩展] 收文登记(来文登记/传递/催办/归档)
CREATE TABLE IF NOT EXISTS doc_receive_register (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id          BIGINT DEFAULT NULL COMMENT '关联公文(收文)',
    receive_no      VARCHAR(50) DEFAULT NULL COMMENT '收文编号',
    from_unit       VARCHAR(100) DEFAULT NULL COMMENT '来文单位',
    from_doc_no     VARCHAR(50) DEFAULT NULL COMMENT '来文文号',
    receive_date    DATE DEFAULT NULL COMMENT '收文日期',
    receive_user_id BIGINT DEFAULT NULL COMMENT '登记人',
    receive_user_name VARCHAR(50) DEFAULT NULL COMMENT '登记人姓名',
    transfer_to     VARCHAR(500) DEFAULT NULL COMMENT '传递去向',
    urge_count      INT DEFAULT 0 COMMENT '催办次数',
    last_urge_time  DATETIME DEFAULT NULL COMMENT '最近催办时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id),
    KEY idx_doc (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收文登记';

-- [扩展] 公文交换(单位间收发传输/对账/跟踪)
CREATE TABLE IF NOT EXISTS doc_exchange (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id          BIGINT DEFAULT NULL COMMENT '公文ID',
    doc_no          VARCHAR(50) DEFAULT NULL COMMENT '文号',
    exchange_type   VARCHAR(10) DEFAULT 'send' COMMENT 'send发出 receive接收',
    from_unit       VARCHAR(100) DEFAULT NULL COMMENT '发出单位',
    to_unit         VARCHAR(100) DEFAULT NULL COMMENT '接收单位',
    title           VARCHAR(200) DEFAULT NULL COMMENT '标题',
    send_time       DATETIME DEFAULT NULL COMMENT '发送时间',
    receive_time    DATETIME DEFAULT NULL COMMENT '收阅时间',
    receive_user    VARCHAR(50) DEFAULT NULL COMMENT '收阅人',
    status          VARCHAR(20) DEFAULT 'sent' COMMENT 'sent发送中 received已收阅 reconcile已对账',
    tracked         TINYINT DEFAULT 0 COMMENT '是否跟踪(0否 1是)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id),
    KEY idx_doc (doc_id),
    KEY idx_to_unit (to_unit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文交换';

-- [扩展] 公文交换日志(过程记录/对账)
CREATE TABLE IF NOT EXISTS doc_exchange_log (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    exchange_id     BIGINT NOT NULL COMMENT '交换ID',
    action          VARCHAR(50) DEFAULT NULL COMMENT 'send/receive/read/reconcile',
    operator        VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    unit            VARCHAR(100) DEFAULT NULL COMMENT '单位',
    log_time        DATETIME DEFAULT NULL COMMENT '日志时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_exchange (exchange_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文交换日志';

-- ============================================================================
-- 五、流程引擎 [基础+扩展]
-- ============================================================================

-- [基础] 流程定义
CREATE TABLE IF NOT EXISTS flow_definition (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    flow_key        VARCHAR(50) NOT NULL COMMENT '流程标识',
    flow_name       VARCHAR(100) NOT NULL COMMENT '流程名称',
    business_type   VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    version         INT DEFAULT 1 COMMENT '版本号',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_key (flow_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义';

-- [基础] 流程节点
CREATE TABLE IF NOT EXISTS flow_node (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    flow_id         BIGINT NOT NULL COMMENT '流程ID',
    node_name       VARCHAR(100) DEFAULT NULL COMMENT '节点名称',
    node_type       VARCHAR(20) DEFAULT 'approve' COMMENT 'approve审批 start发起 countersign会签 cc抄送 end结束',
    approver_type   VARCHAR(20) DEFAULT 'user' COMMENT 'user/role/initiator/dept_leader',
    approver_value  VARCHAR(50) DEFAULT NULL COMMENT '审批人值',
    approver_name   VARCHAR(100) DEFAULT NULL COMMENT '审批人姓名',
    sort            INT DEFAULT 1 COMMENT '排序',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_flow_id (flow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点';

-- [基础] 流程实例
CREATE TABLE IF NOT EXISTS flow_instance (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    flow_id         BIGINT DEFAULT NULL COMMENT '流程ID',
    flow_key        VARCHAR(50) DEFAULT NULL COMMENT '流程标识',
    business_type   VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    business_id     BIGINT DEFAULT NULL COMMENT '业务ID',
    title           VARCHAR(200) DEFAULT NULL COMMENT '标题',
    start_user_id   BIGINT DEFAULT NULL COMMENT '发起人ID',
    start_user_name VARCHAR(50) DEFAULT NULL COMMENT '发起人姓名',
    current_node_id BIGINT DEFAULT NULL COMMENT '当前节点ID',
    current_node_name VARCHAR(100) DEFAULT NULL COMMENT '当前节点名称',
    status          VARCHAR(20) DEFAULT 'running' COMMENT 'running/finished/terminated',
    finish_time     DATETIME DEFAULT NULL COMMENT '完成时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_business (business_type, business_id),
    KEY idx_start_user (start_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例';

-- [基础] 流程任务
CREATE TABLE IF NOT EXISTS flow_task (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    instance_id     BIGINT NOT NULL COMMENT '实例ID',
    flow_id         BIGINT DEFAULT NULL COMMENT '流程ID',
    flow_key        VARCHAR(50) DEFAULT NULL COMMENT '流程标识',
    business_type   VARCHAR(50) DEFAULT NULL COMMENT '业务类型',
    business_id     BIGINT DEFAULT NULL COMMENT '业务ID',
    title           VARCHAR(200) DEFAULT NULL COMMENT '标题',
    node_name       VARCHAR(100) DEFAULT NULL COMMENT '节点名称',
    node_sort       INT DEFAULT NULL COMMENT '节点序号',
    assignee        BIGINT DEFAULT NULL COMMENT '办理人ID',
    assignee_name   VARCHAR(50) DEFAULT NULL COMMENT '办理人姓名',
    start_user_id   BIGINT DEFAULT NULL COMMENT '发起人ID',
    status          VARCHAR(20) DEFAULT 'pending' COMMENT 'pending/done/terminated',
    action          VARCHAR(20) DEFAULT NULL COMMENT 'approve/reject/transfer/withdraw/urge',
    comment         VARCHAR(1000) DEFAULT NULL COMMENT '审批意见',
    action_user_id  BIGINT DEFAULT NULL COMMENT '操作人ID',
    action_user_name VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    action_time     DATETIME DEFAULT NULL COMMENT '操作时间',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_assignee (assignee, status),
    KEY idx_action_user (action_user_id),
    KEY idx_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程任务';

-- [扩展] 委托授权(转办基础)
CREATE TABLE IF NOT EXISTS flow_delegation (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    from_user_id    BIGINT NOT NULL COMMENT '授权人',
    from_user_name  VARCHAR(50) DEFAULT NULL COMMENT '授权人姓名',
    to_user_id      BIGINT NOT NULL COMMENT '被授权人',
    to_user_name    VARCHAR(50) DEFAULT NULL COMMENT '被授权人姓名',
    flow_keys       VARCHAR(500) DEFAULT NULL COMMENT '授权流程类型(空=全部)',
    start_time      DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time        DATETIME DEFAULT NULL COMMENT '结束时间',
    status          TINYINT DEFAULT 0 COMMENT '0生效1失效',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_from_user (from_user_id),
    KEY idx_to_user (to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='委托授权';

-- [扩展] 委托授权记录(每次授权留痕)
CREATE TABLE IF NOT EXISTS flow_delegation_log (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    delegation_id   BIGINT DEFAULT NULL COMMENT '委托授权ID',
    task_id         BIGINT DEFAULT NULL COMMENT '转办的任务',
    from_user_id    BIGINT DEFAULT NULL COMMENT '原使用人ID(调出方)',
    to_user_id      BIGINT DEFAULT NULL COMMENT '新使用人ID(调入方)',
    log_time        DATETIME DEFAULT NULL COMMENT '日志时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='委托授权记录';

-- ============================================================================
-- 六、综合办公 [基础+扩展]
-- ============================================================================

-- [基础] 请假申请
CREATE TABLE IF NOT EXISTS office_leave (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    applicant_id    BIGINT DEFAULT NULL COMMENT '申请人ID',
    applicant_name  VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    leave_type      VARCHAR(50) DEFAULT NULL COMMENT '假别(字典 oa_leave_type)',
    start_date      DATE DEFAULT NULL COMMENT '开始日期',
    end_date        DATE DEFAULT NULL COMMENT '结束日期',
    days            DECIMAL(6,1) DEFAULT NULL COMMENT '天数',
    reason          VARCHAR(500) DEFAULT NULL COMMENT '事由',
    proxy_user_id   BIGINT DEFAULT NULL COMMENT '工作代理人',
    proxy_user_name VARCHAR(50) DEFAULT NULL COMMENT '代理人姓名',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2通过3驳回',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请';

-- [基础] 用车申请
CREATE TABLE IF NOT EXISTS office_vehicle (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    applicant_id    BIGINT DEFAULT NULL COMMENT '申请人ID',
    applicant_name  VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    car_id          BIGINT DEFAULT NULL COMMENT '分配车辆(扩展)',
    use_time        DATETIME DEFAULT NULL COMMENT '用车时间',
    return_time     DATETIME DEFAULT NULL COMMENT '归还时间',
    origin_place    VARCHAR(100) DEFAULT NULL COMMENT '出发地',
    destination     VARCHAR(100) DEFAULT NULL COMMENT '目的地',
    passengers      INT DEFAULT NULL COMMENT '乘车人数',
    car_type        VARCHAR(50) DEFAULT NULL COMMENT '车型(字典 oa_vehicle_type)',
    driver_name     VARCHAR(50) DEFAULT NULL COMMENT '司机姓名',
    reason          VARCHAR(500) DEFAULT NULL COMMENT '事由',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2通过3驳回4已归还',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用车申请';

-- [扩展] 车辆主数据
CREATE TABLE IF NOT EXISTS office_car (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    plate_no        VARCHAR(20) NOT NULL COMMENT '车牌号',
    car_type        VARCHAR(50) DEFAULT NULL COMMENT '车型',
    brand           VARCHAR(50) DEFAULT NULL COMMENT '品牌',
    seats           INT DEFAULT NULL COMMENT '座位数',
    driver_id       BIGINT DEFAULT NULL COMMENT '司机ID',
    driver_name     VARCHAR(50) DEFAULT NULL COMMENT '司机姓名',
    status          TINYINT DEFAULT 0 COMMENT '0空闲1使用中2维修3报废',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_plate (plate_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆主数据';

-- [扩展] 车辆使用记录(状态汇总分析)
CREATE TABLE IF NOT EXISTS office_car_record (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    car_id          BIGINT NOT NULL COMMENT '车辆ID',
    vehicle_id      BIGINT DEFAULT NULL COMMENT '用车申请ID',
    use_time        DATETIME DEFAULT NULL COMMENT '用车时间',
    return_time     DATETIME DEFAULT NULL COMMENT '归还时间',
    mileage_out     DECIMAL(10,1) DEFAULT NULL COMMENT '出车里程',
    mileage_in      DECIMAL(10,1) DEFAULT NULL COMMENT '回车里程',
    PRIMARY KEY (id),
    KEY idx_car (car_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆使用记录';

-- [基础] 用印申请
CREATE TABLE IF NOT EXISTS office_seal (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    applicant_id    BIGINT DEFAULT NULL COMMENT '申请人ID',
    applicant_name  VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    seal_id         BIGINT DEFAULT NULL COMMENT '印章(扩展)',
    seal_type       VARCHAR(50) DEFAULT NULL COMMENT '印章类型(字典 oa_seal_type)',
    matter_type     VARCHAR(50) DEFAULT NULL COMMENT '事项类型',
    file_count      INT DEFAULT NULL COMMENT '份数',
    usage_desc      VARCHAR(500) DEFAULT NULL COMMENT '用途说明',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2通过3驳回4已用印',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用印申请';

-- [扩展] 印章主数据(一印一行，仅承载印章档案与状态)
CREATE TABLE IF NOT EXISTS office_seal_registry (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    seal_name       VARCHAR(50) NOT NULL COMMENT '印章名称',
    seal_type       VARCHAR(50) DEFAULT NULL COMMENT '印章类型',
    keeper_id       BIGINT DEFAULT NULL COMMENT '保管人',
    keeper_name     VARCHAR(50) DEFAULT NULL COMMENT '保管人姓名',
    status          TINYINT DEFAULT 0 COMMENT '0启用1停用2销毁',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='印章主数据';

-- [扩展] 用印台账(每次实际用印一条记录，见流程设计/06 规则4)
CREATE TABLE IF NOT EXISTS office_seal_record (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    seal_id         BIGINT NOT NULL COMMENT '印章ID(office_seal_registry)',
    seal_name       VARCHAR(50) DEFAULT NULL COMMENT '印章名称(冗余)',
    apply_id        BIGINT DEFAULT NULL COMMENT '用印申请ID(office_seal)',
    keeper_id       BIGINT DEFAULT NULL COMMENT '用印操作保管人ID',
    keeper_name     VARCHAR(50) DEFAULT NULL COMMENT '保管人姓名',
    file_count      INT DEFAULT NULL COMMENT '实际盖章份数',
    use_time        DATETIME DEFAULT NULL COMMENT '用印时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_seal (seal_id),
    KEY idx_apply (apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用印台账';

-- [基础] 出差申请
CREATE TABLE IF NOT EXISTS office_trip (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    applicant_id    BIGINT DEFAULT NULL COMMENT '申请人ID',
    applicant_name  VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    destination     VARCHAR(100) DEFAULT NULL COMMENT '目的地',
    start_date      DATE DEFAULT NULL COMMENT '开始日期',
    end_date        DATE DEFAULT NULL COMMENT '结束日期',
    days            DECIMAL(6,1) DEFAULT NULL COMMENT '天数',
    travel_mode     VARCHAR(50) DEFAULT NULL COMMENT '交通(字典 oa_travel_mode)',
    budget          DECIMAL(10,2) DEFAULT NULL COMMENT '预算金额',
    reason          VARCHAR(500) DEFAULT NULL COMMENT '事由',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2通过3驳回4已报销',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出差申请';

-- [基础] 办公资产
CREATE TABLE IF NOT EXISTS office_asset (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    asset_code      VARCHAR(50) DEFAULT NULL COMMENT '资产编码',
    asset_name      VARCHAR(100) NOT NULL COMMENT '资产名称',
    category        VARCHAR(50) DEFAULT NULL COMMENT '类别(字典 oa_asset_category)',
    spec            VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    unit            VARCHAR(20) DEFAULT NULL COMMENT '单位',
    quantity        INT DEFAULT NULL COMMENT '数量',
    amount          DECIMAL(12,2) DEFAULT NULL COMMENT '金额',
    purchase_date   DATE DEFAULT NULL COMMENT '购置日期',
    location        VARCHAR(100) DEFAULT NULL COMMENT '存放位置',
    use_dept_id     BIGINT DEFAULT NULL COMMENT '使用部门ID',
    use_user_id     BIGINT DEFAULT NULL COMMENT '使用人ID',
    use_user_name   VARCHAR(50) DEFAULT NULL COMMENT '使用人姓名',
    depreciation    DECIMAL(12,2) DEFAULT NULL COMMENT '累计减损',
    status          TINYINT DEFAULT 0 COMMENT '0闲置1在用2维修3报废',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_code (asset_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公资产';

-- [扩展] 资产申请(领用/调拨/减损/报废)
CREATE TABLE IF NOT EXISTS office_asset_apply (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    asset_id        BIGINT DEFAULT NULL COMMENT '资产ID',
    apply_type      VARCHAR(20) DEFAULT NULL COMMENT 'requisition领用 transfer调拨 reduce减损 scrap报废',
    applicant_id    BIGINT DEFAULT NULL COMMENT '申请人ID',
    applicant_name  VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
    from_dept_id    BIGINT DEFAULT NULL COMMENT '调出部门ID',
    to_dept_id      BIGINT DEFAULT NULL COMMENT '调入部门ID',
    quantity        INT DEFAULT NULL COMMENT '数量',
    reason          VARCHAR(500) DEFAULT NULL COMMENT '事由',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2通过3驳回',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_asset (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产申请(领用/调拨/减损/报废)';

-- [扩展] 资产变动记录(全生命周期留痕)
CREATE TABLE IF NOT EXISTS office_asset_record (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    asset_id        BIGINT NOT NULL COMMENT '资产ID',
    action          VARCHAR(20) DEFAULT NULL COMMENT 'in入库 requisition领用 transfer调拨 reduce减损 scrap报废',
    from_dept_id    BIGINT DEFAULT NULL COMMENT '调出部门ID',
    to_dept_id      BIGINT DEFAULT NULL COMMENT '调入部门ID',
    from_user_id    BIGINT DEFAULT NULL COMMENT '授权人ID',
    to_user_id      BIGINT DEFAULT NULL COMMENT '被授权人ID',
    quantity        INT DEFAULT NULL COMMENT '数量',
    operator        VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    action_time     DATETIME DEFAULT NULL COMMENT '操作时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_asset (asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产变动记录';

-- [基础] 办公用品(物料)
CREATE TABLE IF NOT EXISTS office_supply (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name            VARCHAR(100) NOT NULL COMMENT '用品名称',
    category        VARCHAR(50) DEFAULT NULL COMMENT '分类',
    spec            VARCHAR(100) DEFAULT NULL COMMENT '规格型号',
    unit            VARCHAR(20) DEFAULT NULL COMMENT '单位',
    stock           INT DEFAULT 0 COMMENT '库存数量',
    warning_stock   INT DEFAULT 0 COMMENT '预警库存',
    price           DECIMAL(10,2) DEFAULT NULL COMMENT '单价',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公用品(物料)';

-- [扩展] 办公用品出入库记录
CREATE TABLE IF NOT EXISTS office_supply_stock (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    supply_id       BIGINT NOT NULL COMMENT '用品ID',
    stock_type      VARCHAR(10) DEFAULT 'in' COMMENT 'in入库 out出库',
    quantity        INT DEFAULT NULL COMMENT '数量',
    before_stock    INT DEFAULT NULL COMMENT '变更前库存',
    after_stock     INT DEFAULT NULL COMMENT '变更后库存',
    operator        VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    relate_id       BIGINT DEFAULT NULL COMMENT '关联申请ID(出库)',
    action_time     DATETIME DEFAULT NULL COMMENT '操作时间',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_supply (supply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公用品出入库记录';

-- [扩展] 办公用品申请
CREATE TABLE IF NOT EXISTS office_supply_apply (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    applicant_id    BIGINT DEFAULT NULL COMMENT '申请人ID',
    applicant_name  VARCHAR(50) DEFAULT NULL COMMENT '申请人姓名',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    items_json      TEXT DEFAULT NULL COMMENT '申请明细JSON[{supplyId,qty}]',
    reason          VARCHAR(500) DEFAULT NULL COMMENT '事由',
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2通过3驳回4已领用',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公用品申请';

-- [基础] 考勤记录
CREATE TABLE IF NOT EXISTS office_attendance (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT DEFAULT NULL COMMENT '用户ID',
    user_name       VARCHAR(50) DEFAULT NULL COMMENT '用户姓名',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    att_date        DATE DEFAULT NULL COMMENT '考勤日期',
    shift_id        BIGINT DEFAULT NULL COMMENT '班次ID',
    clock_in        TIME DEFAULT NULL COMMENT '上班打卡时间',
    clock_out       TIME DEFAULT NULL COMMENT '下班打卡时间',
    status          VARCHAR(20) DEFAULT 'normal' COMMENT 'normal正常 late迟到 earlyLeave早退 absent缺勤',
    work_hours      DECIMAL(4,1) DEFAULT NULL COMMENT '工时(小时)',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id),
    KEY idx_user_date (user_id, att_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录';

-- [扩展] 考勤规则
CREATE TABLE IF NOT EXISTS office_att_rule (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    rule_name       VARCHAR(100) NOT NULL COMMENT '规则名称',
    work_days       VARCHAR(20) DEFAULT '1,2,3,4,5' COMMENT '工作日(周几)',
    allow_late_min  INT DEFAULT 0 COMMENT '允许迟到分钟',
    allow_early_min INT DEFAULT 0 COMMENT '允许早退分钟',
    overtime_rule   VARCHAR(500) DEFAULT NULL COMMENT '加班规则(JSON)',
    trip_rule       VARCHAR(500) DEFAULT NULL COMMENT '出差公出规则(JSON)',
    dept_id         BIGINT DEFAULT NULL COMMENT '适用部门(空=全部)',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤规则';

-- [扩展] 班次
CREATE TABLE IF NOT EXISTS office_shift (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    shift_name      VARCHAR(50) NOT NULL COMMENT '班次名称',
    start_time      TIME DEFAULT NULL COMMENT '开始时间',
    end_time        TIME DEFAULT NULL COMMENT '结束时间',
    rest_start      TIME DEFAULT NULL COMMENT '休息开始时间',
    rest_end        TIME DEFAULT NULL COMMENT '休息结束时间',
    dept_id         BIGINT DEFAULT NULL COMMENT '部门ID',
    status          TINYINT DEFAULT 0 COMMENT '状态(0正常 1停用)',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次';

-- [扩展] 节假日
CREATE TABLE IF NOT EXISTS office_holiday (
    id              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    holiday_name    VARCHAR(50) NOT NULL COMMENT '节假日名称',
    holiday_date    DATE NOT NULL COMMENT '节假日日期',
    holiday_type    VARCHAR(20) DEFAULT 'holiday' COMMENT 'holiday法定假日 workday调休上班',
    year            INT DEFAULT NULL COMMENT '年度',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by       BIGINT DEFAULT NULL COMMENT '创建人ID',
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by       BIGINT DEFAULT NULL COMMENT '更新人ID',
    deleted         TINYINT DEFAULT 0 COMMENT '是否删除(0否 1是)',
    PRIMARY KEY (id),
    KEY idx_date (holiday_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日';

-- ============================================================================
-- 建库完成
-- 说明：
--   - 基础表与工程 schema.sql 一致，可直接用于新库初始化；
--   - 扩展表覆盖报价清单中安全策略、公文交换、委托授权、考勤规则、
--     资产全生命周期、办公用品出入库等能力，按需逐步启用。
--   - 字典初始数据、流程定义初始数据、管理员账号等见 oa-backend 的 data.sql。
-- ============================================================================
