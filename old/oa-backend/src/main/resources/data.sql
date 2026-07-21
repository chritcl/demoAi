-- ============================================================
-- 协同办公平台 (OA Platform) 初始数据
-- 幂等: 使用 INSERT IGNORE + 显式主键，可重复执行
-- 默认账号: admin / <REDACTED_DEFAULT_PASSWORD>   普通用户: zhangsan / <REDACTED_DEFAULT_PASSWORD>
-- ============================================================
USE oa_platform;

-- ------------------- 部门 -------------------
INSERT IGNORE INTO sys_dept (id, parent_id, ancestors, dept_name, sort, leader, phone, email, status, create_time) VALUES
(1, 0, '0',     '协同办公集团', 0, '管理员', '13800000000', 'admin@oa.com', 0, NOW()),
(2, 1, '0,1',   '技术部',       1, '李经理', '13800000001', 'tech@oa.com',  0, NOW()),
(3, 1, '0,1',   '人事部',       2, '王经理', '13800000002', 'hr@oa.com',    0, NOW()),
(4, 1, '0,1',   '财务部',       3, '赵经理', '13800000003', 'fin@oa.com',   0, NOW());

-- ------------------- 用户 -------------------
-- 密码: 已脱敏，参见 WP-000 审计报告
INSERT IGNORE INTO sys_user (id, dept_id, username, nickname, password, email, phone, gender, status, pinyin, create_time) VALUES
(1, 1, 'admin',     '超级管理员', '<REDACTED_PASSWORD_HASH>', 'admin@oa.com',  '13800000000', 0, 0, 'guanliyuan', NOW()),
(2, 2, 'zhangsan',  '张三',       '<REDACTED_PASSWORD_HASH>', 'zhangsan@oa.com','13800000001', 0, 0, 'zhangsan',  NOW()),
(3, 2, 'lisi',      '李四',       '<REDACTED_PASSWORD_HASH>', 'lisi@oa.com',    '13800000002', 1, 0, 'lisi',      NOW()),
(4, 3, 'wangwu',    '王五',       '<REDACTED_PASSWORD_HASH>', 'wangwu@oa.com',  '13800000003', 0, 0, 'wangwu',    NOW());

-- ------------------- 角色 -------------------
INSERT IGNORE INTO sys_role (id, role_name, role_key, sort, status, data_scope, create_time) VALUES
(1, '超级管理员', 'admin',  1, 0, 1, NOW()),
(2, '普通员工',   'staff',  2, 0, 5, NOW()),
(3, '部门主管',   'leader', 3, 0, 3, NOW());

-- ------------------- 用户-角色 -------------------
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 2);

-- ------------------- 菜单（页面） -------------------
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, is_frame, is_cache, visible, sort, status, create_time) VALUES
-- 门户首页
(1,   0, '门户首页', 'C', '/dashboard', 'dashboard/index', 'portal:dashboard:view', 'dashboard', 1, 0, 0, 1, 0, NOW()),
-- 工作台
(100, 0, '工作台',   'C', '/workbench', 'workbench/index', 'workbench:view', 'desktop', 1, 0, 0, 2, 0, NOW()),
-- 信息发布（目录）
(200, 0,   '信息门户', 'M', '/information', NULL, NULL, 'read', 1, 0, 0, 3, 0, NOW()),
(201, 200, '通知公告', 'C', 'notice', 'portal/notice/index', 'portal:notice:list', 'message', 1, 0, 0, 1, 0, NOW()),
(202, 200, '信息发布', 'C', 'article', 'portal/article/index', 'portal:article:list', 'documentation', 1, 0, 0, 2, 0, NOW()),
-- 公文管理（目录）
(300, 0,   '公文管理', 'M', '/document', NULL, NULL, 'document', 1, 0, 0, 4, 0, NOW()),
(301, 300, '发文管理', 'C', 'send',    'document/send/index',    'document:official:list', 'edit', 1, 0, 0, 1, 0, NOW()),
(302, 300, '收文管理', 'C', 'receive', 'document/receive/index', 'document:official:list', 'download', 1, 0, 0, 2, 0, NOW()),
(303, 300, '公文查询', 'C', 'query',   'document/query/index',   'document:official:list', 'search', 1, 0, 0, 3, 0, NOW()),
(304, 300, '公文统计', 'C', 'stat',    'document/stat/index',    'document:official:list', 'chart', 1, 0, 0, 4, 0, NOW()),
-- 综合办公（目录）
(400, 0,   '综合办公', 'M', '/office', NULL, NULL, 'tool', 1, 0, 0, 5, 0, NOW()),
(401, 400, '请休假',   'C', 'leave',       'office/leave/index',       'office:leave:list',       'date', 1, 0, 0, 1, 0, NOW()),
(402, 400, '用车',     'C', 'vehicle',     'office/vehicle/index',     'office:vehicle:list',     'car', 1, 0, 0, 2, 0, NOW()),
(403, 400, '用印',     'C', 'seal',        'office/seal/index',        'office:seal:list',        'stamp', 1, 0, 0, 3, 0, NOW()),
(404, 400, '出差',     'C', 'trip',        'office/trip/index',        'office:trip:list',        'plane', 1, 0, 0, 4, 0, NOW()),
(405, 400, '资产管理', 'C', 'asset',       'office/asset/index',       'office:asset:list',       'shopping', 1, 0, 0, 5, 0, NOW()),
(406, 400, '办公用品', 'C', 'supply',      'office/supply/index',      'office:supply:list',      'box', 1, 0, 0, 6, 0, NOW()),
(407, 400, '考勤打卡', 'C', 'attendance',  'office/attendance/index',  'office:attendance:list',  'time', 1, 0, 0, 7, 0, NOW()),
-- 通讯录
(500, 0, '通讯录', 'C', '/contacts', 'contacts/index', 'contacts:view', 'people', 1, 0, 0, 6, 0, NOW()),
-- 流程管理（目录）
(600, 0,   '流程管理', 'M', '/flow', NULL, NULL, 'guide', 1, 0, 0, 7, 0, NOW()),
(601, 600, '待办中心', 'C', 'todo',       'flow/todo/index',       'workflow:task:view',        'list', 1, 0, 0, 1, 0, NOW()),
(602, 600, '流程定义', 'C', 'definition', 'flow/definition/index', 'workflow:definition:list',  'tree', 1, 0, 0, 2, 0, NOW()),
-- 系统管理（目录）
(700, 0,   '系统管理', 'M', '/system', NULL, NULL, 'system', 1, 0, 0, 8, 0, NOW()),
(701, 700, '用户管理', 'C', 'user', 'system/user/index', 'system:user:list', 'user', 1, 0, 0, 1, 0, NOW()),
(702, 700, '角色管理', 'C', 'role', 'system/role/index', 'system:role:list', 'peoples', 1, 0, 0, 2, 0, NOW()),
(703, 700, '菜单管理', 'C', 'menu', 'system/menu/index', 'system:menu:list', 'menu', 1, 0, 0, 3, 0, NOW()),
(704, 700, '部门管理', 'C', 'dept', 'system/dept/index', 'system:dept:list', 'tree-table', 1, 0, 0, 4, 0, NOW()),
(705, 700, '字典管理', 'C', 'dict', 'system/dict/index', 'system:dict:list', 'dict', 1, 0, 0, 5, 0, NOW()),
(706, 700, '操作日志', 'C', 'log',  'system/log/index',  'system:log:list',  'log', 1, 0, 0, 6, 0, NOW()),
-- 隐藏菜单：个人中心 / 消息中心
(800, 0, '个人中心', 'C', '/profile', 'profile/index',      'system:user:edit', 'user', 1, 0, 1, 99, 0, NOW()),
(801, 0, '消息中心', 'C', '/message', 'portal/message/index','portal:message:view','message', 1, 0, 1, 99, 0, NOW());

-- ------------------- 菜单（按钮权限） -------------------
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, perms, sort, status, create_time) VALUES
-- 用户
(7011, 701, '用户新增', 'F', 'system:user:add', 1, 0, NOW()),
(7012, 701, '用户修改', 'F', 'system:user:edit', 2, 0, NOW()),
(7013, 701, '用户删除', 'F', 'system:user:remove', 3, 0, NOW()),
(7014, 701, '用户查询', 'F', 'system:user:query', 4, 0, NOW()),
(7015, 701, '重置密码', 'F', 'system:user:resetPwd', 5, 0, NOW()),
-- 角色
(7021, 702, '角色新增', 'F', 'system:role:add', 1, 0, NOW()),
(7022, 702, '角色修改', 'F', 'system:role:edit', 2, 0, NOW()),
(7023, 702, '角色删除', 'F', 'system:role:remove', 3, 0, NOW()),
(7024, 702, '角色查询', 'F', 'system:role:query', 4, 0, NOW()),
-- 菜单/部门/字典
(7031, 703, '菜单新增', 'F', 'system:menu:add', 1, 0, NOW()),
(7032, 703, '菜单修改', 'F', 'system:menu:edit', 2, 0, NOW()),
(7033, 703, '菜单删除', 'F', 'system:menu:remove', 3, 0, NOW()),
(7041, 704, '部门新增', 'F', 'system:dept:add', 1, 0, NOW()),
(7042, 704, '部门修改', 'F', 'system:dept:edit', 2, 0, NOW()),
(7043, 704, '部门删除', 'F', 'system:dept:remove', 3, 0, NOW()),
(7051, 705, '字典新增', 'F', 'system:dict:add', 1, 0, NOW()),
(7052, 705, '字典修改', 'F', 'system:dict:edit', 2, 0, NOW()),
(7053, 705, '字典删除', 'F', 'system:dict:remove', 3, 0, NOW()),
-- 通知公告
(2011, 201, '公告新增', 'F', 'portal:notice:add', 1, 0, NOW()),
(2012, 201, '公告修改', 'F', 'portal:notice:edit', 2, 0, NOW()),
(2013, 201, '公告删除', 'F', 'portal:notice:remove', 3, 0, NOW()),
(2014, 201, '公告发布', 'F', 'portal:notice:publish', 4, 0, NOW()),
-- 信息发布
(2021, 202, '文章新增', 'F', 'portal:article:add', 1, 0, NOW()),
(2022, 202, '文章修改', 'F', 'portal:article:edit', 2, 0, NOW()),
(2023, 202, '文章删除', 'F', 'portal:article:remove', 3, 0, NOW()),
(2024, 202, '文章审核', 'F', 'portal:article:audit', 4, 0, NOW()),
-- 公文
(3011, 301, '公文新增', 'F', 'document:official:add', 1, 0, NOW()),
(3012, 301, '公文修改', 'F', 'document:official:edit', 2, 0, NOW()),
(3013, 301, '公文删除', 'F', 'document:official:remove', 3, 0, NOW()),
(3014, 301, '公文提交', 'F', 'document:official:submit', 4, 0, NOW()),
-- 请假/用车/用印/出差
(4011, 401, '请新增', 'F', 'office:leave:add', 1, 0, NOW()),
(4012, 401, '请修改', 'F', 'office:leave:edit', 2, 0, NOW()),
(4013, 401, '请删除', 'F', 'office:leave:remove', 3, 0, NOW()),
(4014, 401, '请提交', 'F', 'office:leave:submit', 4, 0, NOW()),
(4021, 402, '车新增', 'F', 'office:vehicle:add', 1, 0, NOW()),
(4022, 402, '车修改', 'F', 'office:vehicle:edit', 2, 0, NOW()),
(4023, 402, '车删除', 'F', 'office:vehicle:remove', 3, 0, NOW()),
(4024, 402, '车提交', 'F', 'office:vehicle:submit', 4, 0, NOW()),
(4031, 403, '印新增', 'F', 'office:seal:add', 1, 0, NOW()),
(4032, 403, '印修改', 'F', 'office:seal:edit', 2, 0, NOW()),
(4033, 403, '印删除', 'F', 'office:seal:remove', 3, 0, NOW()),
(4034, 403, '印提交', 'F', 'office:seal:submit', 4, 0, NOW()),
(4041, 404, '差新增', 'F', 'office:trip:add', 1, 0, NOW()),
(4042, 404, '差修改', 'F', 'office:trip:edit', 2, 0, NOW()),
(4043, 404, '差删除', 'F', 'office:trip:remove', 3, 0, NOW()),
(4044, 404, '差提交', 'F', 'office:trip:submit', 4, 0, NOW()),
-- 资产/办公用品/考勤
(4051, 405, '资新增', 'F', 'office:asset:add', 1, 0, NOW()),
(4052, 405, '资修改', 'F', 'office:asset:edit', 2, 0, NOW()),
(4053, 405, '资删除', 'F', 'office:asset:remove', 3, 0, NOW()),
(4061, 406, '物新增', 'F', 'office:supply:add', 1, 0, NOW()),
(4062, 406, '物修改', 'F', 'office:supply:edit', 2, 0, NOW()),
(4063, 406, '物删除', 'F', 'office:supply:remove', 3, 0, NOW());

-- ------------------- 角色-菜单 -------------------
-- 超级管理员：拥有全部菜单（INSERT IGNORE 幂等）
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;

-- 普通员工：门户/工作台/公告查看/通讯录/请假/用车/用印/出差/考勤
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 100), (2, 200), (2, 201), (2, 202),
(2, 500),
(2, 400), (2, 401), (2, 4011), (2, 4012), (2, 4013), (2, 4014),
(2, 402), (2, 4021), (2, 4022), (2, 4023), (2, 4024),
(2, 403), (2, 4031), (2, 4032), (2, 4033), (2, 4034),
(2, 404), (2, 4041), (2, 4042), (2, 4043), (2, 4044),
(2, 407);

-- 部门主管：在普通员工基础上增加 待办中心 + 流程管理
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 3, id FROM sys_menu WHERE id IN (1,100,200,201,202,300,301,302,303,304,3011,3012,3014,400,401,4011,4012,4013,4014,402,4021,4022,4023,4024,500,600,601);

-- ------------------- 字典类型 -------------------
INSERT IGNORE INTO sys_dict_type (id, dict_name, dict_type, status, create_time) VALUES
(1, '性别',       'sys_user_sex',     0, NOW()),
(2, '状态',       'sys_normal_disable', 0, NOW()),
(3, '公告分类',   'oa_notice_type',   0, NOW()),
(4, '文章栏目',   'oa_article_category', 0, NOW()),
(5, '公文文种',   'oa_doc_category',  0, NOW()),
(6, '公文缓急',   'oa_doc_urgency',   0, NOW()),
(7, '公文密级',   'oa_doc_secrecy',   0, NOW()),
(8, '假别',       'oa_leave_type',    0, NOW()),
(9, '用车车型',   'oa_vehicle_type',  0, NOW()),
(10,'印章类型',   'oa_seal_type',     0, NOW()),
(11,'出差交通',   'oa_travel_mode',   0, NOW()),
(12,'资产分类',   'oa_asset_category',0, NOW()),
(13,'资产状态',   'oa_asset_status',  0, NOW()),
(14,'办公用品分类','oa_supply_category',0, NOW()),
(15,'考勤状态',   'oa_attendance_status',0, NOW());

-- ------------------- 字典数据 -------------------
INSERT IGNORE INTO sys_dict_data (dict_type, dict_label, dict_value, list_class, sort, status, is_default, create_time) VALUES
('sys_user_sex','男','0','primary',1,0,1,NOW()), ('sys_user_sex','女','1','danger',2,0,0,NOW()), ('sys_user_sex','未知','2','info',3,0,0,NOW()),
('sys_normal_disable','正常','0','success',1,0,1,NOW()), ('sys_normal_disable','停用','1','danger',2,0,0,NOW()),
('oa_notice_type','通知','1','primary',1,0,1,NOW()), ('oa_notice_type','公告','2','success',2,0,0,NOW()), ('oa_notice_type','新闻','3','info',3,0,0,NOW()),
('oa_article_category','公司动态','dynamic','primary',1,0,1,NOW()), ('oa_article_category','规章制度','rule','success',2,0,0,NOW()), ('oa_article_category','行业资讯','industry','info',3,0,0,NOW()),
('oa_doc_category','决定','decide','',1,0,0,NOW()), ('oa_doc_category','通知','notice','',2,0,1,NOW()), ('oa_doc_category','报告','report','',3,0,0,NOW()), ('oa_doc_category','函','letter','',4,0,0,NOW()),
('oa_doc_urgency','普通','0','info',1,0,1,NOW()), ('oa_doc_urgency','紧急','1','warning',2,0,0,NOW()), ('oa_doc_urgency','特急','2','danger',3,0,0,NOW()),
('oa_doc_secrecy','公开','0','success',1,0,1,NOW()), ('oa_doc_secrecy','秘密','1','warning',2,0,0,NOW()), ('oa_doc_secrecy','机密','2','danger',3,0,0,NOW()),
('oa_leave_type','事假','personal','',1,0,0,NOW()), ('oa_leave_type','病假','sick','',2,0,0,NOW()), ('oa_leave_type','年假','annual','',3,0,1,NOW()), ('oa_leave_type','调休','comp','',4,0,0,NOW()), ('oa_leave_type','婚假','marriage','',5,0,0,NOW()), ('oa_leave_type','产假','maternity','',6,0,0,NOW()),
('oa_vehicle_type','轿车','car','',1,0,1,NOW()), ('oa_vehicle_type','商务车','business','',2,0,0,NOW()), ('oa_vehicle_type','客车','bus','',3,0,0,NOW()),
('oa_seal_type','公章','official','',1,0,1,NOW()), ('oa_seal_type','合同章','contract','',2,0,0,NOW()), ('oa_seal_type','财务章','finance','',3,0,0,NOW()), ('oa_seal_type','法人章','legal','',4,0,0,NOW()),
('oa_travel_mode','飞机','plane','',1,0,1,NOW()), ('oa_travel_mode','高铁','train','',2,0,0,NOW()), ('oa_travel_mode','汽车','car','',3,0,0,NOW()),
('oa_asset_category','电子设备','electronic','',1,0,1,NOW()), ('oa_asset_category','办公家具','furniture','',2,0,0,NOW()), ('oa_asset_category','车辆','vehicle','',3,0,0,NOW()),
('oa_asset_status','闲置','0','info',1,0,1,NOW()), ('oa_asset_status','在用','1','success',2,0,0,NOW()), ('oa_asset_status','维修','2','warning',3,0,0,NOW()), ('oa_asset_status','报废','3','danger',4,0,0,NOW()),
('oa_supply_category','文具','stationery','',1,0,1,NOW()), ('oa_supply_category','纸张','paper','',2,0,0,NOW()), ('oa_supply_category','耗材','consumables','',3,0,0,NOW()),
('oa_attendance_status','正常','normal','success',1,0,1,NOW()), ('oa_attendance_status','迟到','late','warning',2,0,0,NOW()), ('oa_attendance_status','早退','earlyLeave','warning',3,0,0,NOW()), ('oa_attendance_status','缺勤','absent','danger',4,0,0,NOW());

-- ------------------- 流程定义 + 节点 -------------------
INSERT IGNORE INTO flow_definition (id, flow_key, flow_name, business_type, version, status, create_time) VALUES
(1, 'leave',          '请假审批流程',   'leave',          1, 0, NOW()),
(2, 'vehicle',        '用车审批流程',   'vehicle',        1, 0, NOW()),
(3, 'seal',           '用印审批流程',   'seal',           1, 0, NOW()),
(4, 'trip',           '出差审批流程',   'trip',           1, 0, NOW()),
(5, 'document_send',  '发文审批流程',   'document_send',  1, 0, NOW());

-- 节点：均流转至 部门主管(角色 leader → 解析为 lisi 用户3) → 总经理(admin 用户1)
INSERT IGNORE INTO flow_node (id, flow_id, node_name, approver_type, approver_value, approver_name, sort, create_time) VALUES
(11, 1, '部门主管审核', 'role', 'leader', '部门主管', 1, NOW()),
(12, 1, '总经理审批',   'user', '1',      '管理员',   2, NOW()),
(21, 2, '部门主管审核', 'role', 'leader', '部门主管', 1, NOW()),
(31, 3, '部门主管审核', 'role', 'leader', '部门主管', 1, NOW()),
(41, 4, '部门主管审核', 'role', 'leader', '部门主管', 1, NOW()),
(42, 4, '总经理审批',   'user', '1',      '管理员',   2, NOW()),
(51, 5, '部门审核',     'role', 'leader', '部门主管', 1, NOW()),
(52, 5, '领导签发',     'user', '1',      '管理员',   2, NOW());

-- ------------------- 示例：通知公告 -------------------
INSERT IGNORE INTO portal_notice (id, title, summary, content, category, status, top, read_count, publish_user_id, publish_user_name, publish_time, create_time) VALUES
(1, '关于协同办公平台上线的通知', '平台正式上线，欢迎使用。', '<p>各位同事：协同办公平台已正式上线，请各部门积极使用。</p>', '1', 1, 1, 12, 1, '超级管理员', NOW(), NOW()),
(2, '2026年度端午放假安排',       '6月19日至6月21日放假调休。', '<p>根据国家规定，端午节放假3天，请大家合理安排工作。</p>', '2', 1, 0, 8,  1, '超级管理员', NOW(), NOW()),
(3, '公司年度体检通知',           '请于本月内完成体检。',       '<p>本年度员工体检已开放预约，请联系人事部。</p>', '2', 1, 0, 5,  1, '超级管理员', NOW(), NOW());

-- ------------------- 示例：信息发布 -------------------
INSERT IGNORE INTO portal_article (id, title, summary, content, category, status, top, author, view_count, publish_user_id, publish_time, create_time) VALUES
(1, '公司一季度经营情况通报', '营收同比增长15%。', '<p>2026年一季度公司经营情况良好，营收同比增长15%。</p>', 'dynamic', 2, 0, '超级管理员', 23, 1, NOW(), NOW()),
(2, '《员工手册(2026版)》发布', '请全体员工学习。', '<p>新版员工手册已发布，新增远程办公等内容。</p>', 'rule', 2, 1, '超级管理员', 40, 1, NOW(), NOW());

-- ------------------- 示例：公文 -------------------
INSERT IGNORE INTO doc_official (id, doc_type, doc_no, doc_category, title, urgency, secrecy, content, drafter_user_id, drafter_name, dept_id, status, publish_date, create_time) VALUES
(1, 'send', 'OA发〔2026〕0001号', 'notice', '关于印发协同办公平台使用规范的通知', 0, 0, '<p>现将协同办公平台使用规范印发给你们，请遵照执行。</p>', 1, '超级管理员', 1, 2, '2026-07-01', NOW()),
(2, 'receive', NULL, 'report', '关于上级单位调研来访的收文', 1, 0, '<p>上级单位拟于下周来访调研，请做好准备。</p>', 1, '超级管理员', 1, 1, NULL, NOW());

-- ------------------- 示例：办公用品 / 资产 -------------------
INSERT IGNORE INTO office_supply (id, name, category, spec, unit, stock, warning_stock, price, create_time) VALUES
(1, 'A4打印纸', 'paper', '70g 500张/包', '包', 120, 30, 25.00, NOW()),
(2, '中性笔',   'stationery', '0.5mm 黑色', '支', 500, 100, 1.50, NOW()),
(3, '订书机',   'stationery', '标准型', '个', 40, 10, 18.00, NOW()),
(4, '墨盒',     'consumables', '黑色', '个', 15, 5, 220.00, NOW());

INSERT IGNORE INTO office_asset (id, asset_code, asset_name, category, spec, unit, quantity, amount, location, use_dept_id, use_user_id, status, create_time) VALUES
(1, 'AS-2026-0001', '笔记本电脑', 'electronic', 'ThinkPad T14', '台', 1, 8000.00, '技术部办公区', 2, 2, 1, NOW()),
(2, 'AS-2026-0002', '台式电脑',   'electronic', '联想启天',     '台', 1, 3500.00, '人事部办公区', 3, 4, 1, NOW()),
(3, 'AS-2026-0003', '打印机',     'electronic', 'HP LaserJet',  '台', 1, 2500.00, '公共区域',     1, NULL, 1, NOW());

