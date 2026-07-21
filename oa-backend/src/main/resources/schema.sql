-- ============================================================
-- 协同办公平台 (OA Platform) 数据库表结构
-- 数据库: oa_platform  (MySQL 8.x, utf8mb4)
-- 幂等: 均使用 CREATE TABLE IF NOT EXISTS，可重复执行
-- ============================================================

CREATE DATABASE IF NOT EXISTS oa_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE oa_platform;

-- ------------------- 系统用户 -------------------
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    dept_id         BIGINT          DEFAULT NULL COMMENT '部门ID',
    username        VARCHAR(50)     NOT NULL COMMENT '用户名',
    nickname        VARCHAR(50)     DEFAULT NULL COMMENT '昵称',
    password        VARCHAR(100)    DEFAULT NULL COMMENT '密码',
    email           VARCHAR(100)    DEFAULT NULL COMMENT '邮箱',
    phone           VARCHAR(20)     DEFAULT NULL COMMENT '手机',
    avatar          VARCHAR(255)    DEFAULT NULL COMMENT '头像',
    gender          TINYINT         DEFAULT 2 COMMENT '性别 0男1女2未知',
    status          TINYINT         DEFAULT 0 COMMENT '状态 0正常1停用',
    login_ip        VARCHAR(50)     DEFAULT NULL COMMENT '最后登录IP',
    login_date      DATETIME        DEFAULT NULL COMMENT '最后登录时间',
    pinyin          VARCHAR(100)    DEFAULT NULL COMMENT '姓名拼音',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT          DEFAULT NULL,
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT          DEFAULT NULL,
    deleted         TINYINT         DEFAULT 0,
    remark          VARCHAR(500)    DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- ------------------- 角色 -------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    role_name       VARCHAR(50)     NOT NULL COMMENT '角色名',
    role_key        VARCHAR(50)     NOT NULL COMMENT '角色编码',
    sort            INT             DEFAULT 0,
    status          TINYINT         DEFAULT 0,
    data_scope      TINYINT         DEFAULT 1 COMMENT '数据范围',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT          DEFAULT NULL,
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT          DEFAULT NULL,
    deleted         TINYINT         DEFAULT 0,
    remark          VARCHAR(500)    DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- ------------------- 菜单/权限 -------------------
CREATE TABLE IF NOT EXISTS sys_menu (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    parent_id       BIGINT          DEFAULT 0,
    menu_name       VARCHAR(50)     NOT NULL,
    menu_type       CHAR(1)         DEFAULT 'C' COMMENT 'M目录 C菜单 F按钮',
    path            VARCHAR(200)    DEFAULT NULL,
    component       VARCHAR(255)    DEFAULT NULL,
    perms           VARCHAR(100)    DEFAULT NULL,
    query           VARCHAR(255)    DEFAULT NULL,
    redirect        VARCHAR(200)    DEFAULT NULL,
    icon            VARCHAR(50)     DEFAULT NULL,
    is_frame        TINYINT         DEFAULT 1 COMMENT '0外链1否',
    is_cache        TINYINT         DEFAULT 0 COMMENT '0缓存1否',
    visible         TINYINT         DEFAULT 0 COMMENT '0显示1隐藏',
    sort            INT             DEFAULT 0,
    status          TINYINT         DEFAULT 0,
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT          DEFAULT NULL,
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT          DEFAULT NULL,
    deleted         TINYINT         DEFAULT 0,
    remark          VARCHAR(500)    DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限';

-- ------------------- 部门 -------------------
CREATE TABLE IF NOT EXISTS sys_dept (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    parent_id       BIGINT          DEFAULT 0,
    ancestors       VARCHAR(200)    DEFAULT NULL,
    dept_name       VARCHAR(50)     NOT NULL,
    sort            INT             DEFAULT 0,
    leader          VARCHAR(50)     DEFAULT NULL,
    phone           VARCHAR(20)     DEFAULT NULL,
    email           VARCHAR(100)    DEFAULT NULL,
    status          TINYINT         DEFAULT 0,
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT          DEFAULT NULL,
    update_time     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT          DEFAULT NULL,
    deleted         TINYINT         DEFAULT 0,
    remark          VARCHAR(500)    DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门';

-- ------------------- 关联表 -------------------
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色';

CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-菜单';

-- ------------------- 字典 -------------------
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    dict_name   VARCHAR(50) NOT NULL,
    dict_type   VARCHAR(50) NOT NULL,
    status      TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by   BIGINT DEFAULT NULL,
    deleted     TINYINT DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型';

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id          BIGINT NOT NULL AUTO_INCREMENT,
    dict_type   VARCHAR(50) NOT NULL,
    dict_label  VARCHAR(100) NOT NULL,
    dict_value  VARCHAR(100) NOT NULL,
    list_class  VARCHAR(20) DEFAULT NULL,
    sort        INT DEFAULT 0,
    status      TINYINT DEFAULT 0,
    is_default  TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by   BIGINT DEFAULT NULL,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by   BIGINT DEFAULT NULL,
    deleted     TINYINT DEFAULT 0,
    remark      VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据';

-- ------------------- 操作日志 -------------------
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    title           VARCHAR(50) DEFAULT NULL,
    business_type   INT DEFAULT 0,
    method          VARCHAR(200) DEFAULT NULL,
    request_method  VARCHAR(10) DEFAULT NULL,
    oper_name       VARCHAR(50) DEFAULT NULL,
    oper_id         BIGINT DEFAULT NULL,
    oper_url        VARCHAR(255) DEFAULT NULL,
    oper_param      TEXT DEFAULT NULL,
    json_result     TEXT DEFAULT NULL,
    status          TINYINT DEFAULT 0,
    error_msg       VARCHAR(2000) DEFAULT NULL,
    oper_ip         VARCHAR(50) DEFAULT NULL,
    oper_time       DATETIME DEFAULT NULL,
    cost_time       BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_oper_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ------------------- 站内消息 -------------------
CREATE TABLE IF NOT EXISTS sys_message (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    user_id         BIGINT NOT NULL COMMENT '接收人',
    title           VARCHAR(200) DEFAULT NULL,
    content         TEXT DEFAULT NULL,
    type            VARCHAR(20) DEFAULT 'system',
    business_type   VARCHAR(50) DEFAULT NULL,
    business_id     BIGINT DEFAULT NULL,
    is_read         TINYINT DEFAULT 0,
    send_user_id    BIGINT DEFAULT NULL,
    send_user_name  VARCHAR(50) DEFAULT NULL,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';

-- ------------------- 文件 -------------------
CREATE TABLE IF NOT EXISTS sys_file (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    original_name   VARCHAR(255) DEFAULT NULL,
    file_name       VARCHAR(255) DEFAULT NULL,
    file_path       VARCHAR(500) DEFAULT NULL,
    url             VARCHAR(500) DEFAULT NULL,
    size            BIGINT DEFAULT NULL,
    content_type    VARCHAR(100) DEFAULT NULL,
    create_by       BIGINT DEFAULT NULL,
    upload_day      DATE DEFAULT NULL,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件';

-- ------------------- 通知公告 -------------------
CREATE TABLE IF NOT EXISTS portal_notice (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    title           VARCHAR(200) NOT NULL,
    summary         VARCHAR(500) DEFAULT NULL,
    content         LONGTEXT DEFAULT NULL,
    category        VARCHAR(50) DEFAULT NULL,
    status          TINYINT DEFAULT 0 COMMENT '0草稿1已发布2撤回',
    top             TINYINT DEFAULT 0,
    cover           VARCHAR(255) DEFAULT NULL,
    read_count      INT DEFAULT 0,
    publish_user_id BIGINT DEFAULT NULL,
    publish_user_name VARCHAR(50) DEFAULT NULL,
    publish_time    DATETIME DEFAULT NULL,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告';

-- ------------------- 信息发布 -------------------
CREATE TABLE IF NOT EXISTS portal_article (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    title           VARCHAR(200) NOT NULL,
    summary         VARCHAR(500) DEFAULT NULL,
    content         LONGTEXT DEFAULT NULL,
    category        VARCHAR(50) DEFAULT NULL,
    status          TINYINT DEFAULT 0 COMMENT '0草稿1待审2已发布3驳回',
    top             TINYINT DEFAULT 0,
    author          VARCHAR(50) DEFAULT NULL,
    cover           VARCHAR(255) DEFAULT NULL,
    view_count      INT DEFAULT 0,
    publish_user_id BIGINT DEFAULT NULL,
    audit_comment   VARCHAR(500) DEFAULT NULL,
    publish_time    DATETIME DEFAULT NULL,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='信息发布';

-- ------------------- 公文 -------------------
CREATE TABLE IF NOT EXISTS doc_official (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    doc_type        VARCHAR(10) DEFAULT 'send' COMMENT 'send发文 receive收文',
    doc_no          VARCHAR(50) DEFAULT NULL,
    doc_category    VARCHAR(50) DEFAULT NULL,
    seq_no          INT DEFAULT NULL,
    title           VARCHAR(200) NOT NULL,
    urgency         TINYINT DEFAULT 0 COMMENT '0普通1紧急2特急',
    secrecy         TINYINT DEFAULT 0 COMMENT '0公开1秘密2机密',
    content         LONGTEXT DEFAULT NULL,
    attachments     VARCHAR(1000) DEFAULT NULL,
    drafter_user_id BIGINT DEFAULT NULL,
    drafter_name    VARCHAR(50) DEFAULT NULL,
    dept_id         BIGINT DEFAULT NULL,
    from_unit       VARCHAR(100) DEFAULT NULL,
    recipient_scope VARCHAR(255) DEFAULT NULL,
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2已发布3驳回(收文1已登记)',
    publish_date    DATE DEFAULT NULL,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公文';

-- ------------------- 综合办公：请假 -------------------
CREATE TABLE IF NOT EXISTS office_leave (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    applicant_id    BIGINT DEFAULT NULL,
    applicant_name  VARCHAR(50) DEFAULT NULL,
    dept_id         BIGINT DEFAULT NULL,
    leave_type      VARCHAR(50) DEFAULT NULL,
    start_date      DATE DEFAULT NULL,
    end_date        DATE DEFAULT NULL,
    days            DECIMAL(6,1) DEFAULT NULL,
    reason          VARCHAR(500) DEFAULT NULL,
    proxy_user_id   BIGINT DEFAULT NULL,
    status          TINYINT DEFAULT 0 COMMENT '0草稿1审批中2通过3驳回',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请';

-- ------------------- 综合办公：用车 -------------------
CREATE TABLE IF NOT EXISTS office_vehicle (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    applicant_id    BIGINT DEFAULT NULL,
    applicant_name  VARCHAR(50) DEFAULT NULL,
    dept_id         BIGINT DEFAULT NULL,
    use_time        DATETIME DEFAULT NULL,
    return_time     DATETIME DEFAULT NULL,
    origin_place    VARCHAR(100) DEFAULT NULL,
    destination     VARCHAR(100) DEFAULT NULL,
    passengers      INT DEFAULT NULL,
    car_type        VARCHAR(50) DEFAULT NULL,
    reason          VARCHAR(500) DEFAULT NULL,
    status          TINYINT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用车申请';

-- ------------------- 综合办公：用印 -------------------
CREATE TABLE IF NOT EXISTS office_seal (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    applicant_id    BIGINT DEFAULT NULL,
    applicant_name  VARCHAR(50) DEFAULT NULL,
    dept_id         BIGINT DEFAULT NULL,
    seal_type       VARCHAR(50) DEFAULT NULL,
    matter_type     VARCHAR(50) DEFAULT NULL,
    file_count      INT DEFAULT NULL,
    usage_desc      VARCHAR(500) DEFAULT NULL COMMENT '用途说明',
    status          TINYINT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用印申请';

-- ------------------- 综合办公：出差 -------------------
CREATE TABLE IF NOT EXISTS office_trip (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    applicant_id    BIGINT DEFAULT NULL,
    applicant_name  VARCHAR(50) DEFAULT NULL,
    dept_id         BIGINT DEFAULT NULL,
    destination     VARCHAR(100) DEFAULT NULL,
    start_date      DATE DEFAULT NULL,
    end_date        DATE DEFAULT NULL,
    days            DECIMAL(6,1) DEFAULT NULL,
    travel_mode     VARCHAR(50) DEFAULT NULL,
    budget          DECIMAL(10,2) DEFAULT NULL,
    reason          VARCHAR(500) DEFAULT NULL,
    status          TINYINT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出差申请';

-- ------------------- 综合办公：资产 -------------------
CREATE TABLE IF NOT EXISTS office_asset (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    asset_code      VARCHAR(50) DEFAULT NULL,
    asset_name      VARCHAR(100) DEFAULT NULL,
    category        VARCHAR(50) DEFAULT NULL,
    spec            VARCHAR(100) DEFAULT NULL,
    unit            VARCHAR(20) DEFAULT NULL,
    quantity        INT DEFAULT NULL,
    amount          DECIMAL(12,2) DEFAULT NULL,
    location        VARCHAR(100) DEFAULT NULL,
    use_dept_id     BIGINT DEFAULT NULL,
    use_user_id     BIGINT DEFAULT NULL,
    status          TINYINT DEFAULT 0 COMMENT '0闲置1在用2维修3报废',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公资产';

-- ------------------- 综合办公：办公用品 -------------------
CREATE TABLE IF NOT EXISTS office_supply (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    category        VARCHAR(50) DEFAULT NULL,
    spec            VARCHAR(100) DEFAULT NULL,
    unit            VARCHAR(20) DEFAULT NULL,
    stock           INT DEFAULT 0,
    warning_stock   INT DEFAULT 0,
    price           DECIMAL(10,2) DEFAULT NULL,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='办公用品';

-- ------------------- 综合办公：考勤 -------------------
CREATE TABLE IF NOT EXISTS office_attendance (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    user_id         BIGINT DEFAULT NULL,
    user_name       VARCHAR(50) DEFAULT NULL,
    dept_id         BIGINT DEFAULT NULL,
    att_date        DATE DEFAULT NULL,
    clock_in        TIME DEFAULT NULL,
    clock_out       TIME DEFAULT NULL,
    status          VARCHAR(20) DEFAULT 'normal',
    remark          VARCHAR(500) DEFAULT NULL,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_date (user_id, att_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录';

-- ------------------- 流程定义/节点/实例/任务 -------------------
CREATE TABLE IF NOT EXISTS flow_definition (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    flow_key        VARCHAR(50) NOT NULL,
    flow_name       VARCHAR(100) NOT NULL,
    business_type   VARCHAR(50) DEFAULT NULL,
    version         INT DEFAULT 1,
    status          TINYINT DEFAULT 0,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_flow_key (flow_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义';

CREATE TABLE IF NOT EXISTS flow_node (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    flow_id         BIGINT NOT NULL,
    node_name       VARCHAR(100) DEFAULT NULL,
    approver_type   VARCHAR(20) DEFAULT 'user' COMMENT 'user/role/initiator',
    approver_value  VARCHAR(50) DEFAULT NULL,
    approver_name   VARCHAR(100) DEFAULT NULL,
    sort            INT DEFAULT 1,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_flow_id (flow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程节点';

CREATE TABLE IF NOT EXISTS flow_instance (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    flow_id         BIGINT DEFAULT NULL,
    flow_key        VARCHAR(50) DEFAULT NULL,
    business_type   VARCHAR(50) DEFAULT NULL,
    business_id     BIGINT DEFAULT NULL,
    title           VARCHAR(200) DEFAULT NULL,
    start_user_id   BIGINT DEFAULT NULL,
    start_user_name VARCHAR(50) DEFAULT NULL,
    current_node_id BIGINT DEFAULT NULL,
    current_node_name VARCHAR(100) DEFAULT NULL,
    status          VARCHAR(20) DEFAULT 'running',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_business (business_type, business_id),
    KEY idx_start_user (start_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例';

CREATE TABLE IF NOT EXISTS flow_task (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    instance_id     BIGINT NOT NULL,
    flow_id         BIGINT DEFAULT NULL,
    flow_key        VARCHAR(50) DEFAULT NULL,
    business_type   VARCHAR(50) DEFAULT NULL,
    business_id     BIGINT DEFAULT NULL,
    title           VARCHAR(200) DEFAULT NULL,
    node_name       VARCHAR(100) DEFAULT NULL,
    node_sort       INT DEFAULT NULL,
    assignee        BIGINT DEFAULT NULL,
    assignee_name   VARCHAR(50) DEFAULT NULL,
    start_user_id   BIGINT DEFAULT NULL,
    status          VARCHAR(20) DEFAULT 'pending',
    comment         VARCHAR(1000) DEFAULT NULL,
    action_user_id  BIGINT DEFAULT NULL,
    action_user_name VARCHAR(50) DEFAULT NULL,
    action_time     DATETIME DEFAULT NULL,
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT DEFAULT NULL,
    update_time     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by       BIGINT DEFAULT NULL,
    deleted         TINYINT DEFAULT 0,
    remark          VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_assignee (assignee, status),
    KEY idx_action_user (action_user_id),
    KEY idx_business (business_type, business_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程任务';
