---
status: active
---

# 需求追踪矩阵

## 编号规则

- `REQ-{DOMAIN}-{NNN}`：业务需求编号，稳定不变。
- 业务域编码：`PORTAL`（门户）、`INFO`（信息发布）、`NOTICE`（通知公告）、`DOC`（公文管理）、`EXCHANGE`（公文交换）、`OFFICE`（综合办公）、`CONTACTS`（通讯录）、`WORKBENCH`（工作台）、`MOBILE`（移动办公）、`PLATFORM`（应用支撑平台）。
- 三期需求标记为 `deferred`。

## 追踪表

| 需求编号 | 业务域 | 业务包/前端功能域 | 工作包 | 验收标准 | 测试证据 | 阶段 |
|---|---|---|---|---|---|---|
| REQ-PORTAL-001 | 统一门户 | s1_portal / pc-portal | 待 S1 分配 | 门户首页展示统计、公告、待办 | 待创建 | 一期 |
| REQ-PORTAL-002 | 统一门户 | s1_portal / pc-portal | 待 S1 分配 | 消息提醒列表与未读数 | 待创建 | 一期 |
| REQ-PORTAL-003 | 统一门户 | s1_portal / pc-portal | 待 S1 分配 | 应用入口可配置 | 待创建 | 一期 |
| REQ-PORTAL-004 | 统一门户 | s1_portal / pc-portal | 待 S1 分配 | 统一待办/已办列表 | 待创建 | 一期 |
| REQ-PORTAL-005 | 统一门户 | s1_portal / pc-portal | 待 S1 分配 | 通讯录搜索 | 待创建 | 一期 |
| REQ-INFO-001 | 信息发布 | s2_document / pc-portal | 待 S1 分配 | 栏目 CRUD | 待创建 | 一期 |
| REQ-INFO-002 | 信息发布 | s2_document / pc-portal | 待 S1 分配 | 文章 CRUD | 待创建 | 一期 |
| REQ-INFO-003 | 信息发布 | s2_document / pc-portal | 待 S1 分配 | 草稿/提交审核 | 待创建 | 一期 |
| REQ-INFO-004 | 信息发布 | s2_document / pc-portal | 待 S1 分配 | 审核通过/驳回 | 待创建 | 一期 |
| REQ-NOTICE-001 | 通知公告 | s2_document / pc-portal | 待 S1 分配 | 公告 CRUD | 待创建 | 一期 |
| REQ-NOTICE-002 | 通知公告 | s2_document / pc-portal | 待 S1 分配 | 公告发布/撤回 | 待创建 | 一期 |
| REQ-NOTICE-003 | 通知公告 | s2_document / pc-portal | 待 S1 分配 | 公告置顶 | 待创建 | 一期 |
| REQ-NOTICE-004 | 通知公告 | s2_document / pc-portal | 待 S1 分配 | 阅读统计 | 待创建 | 一期 |
| REQ-DOC-001 | 公文管理 | s3_office / pc-document | 待 S1 分配 | 发文起草 | 待创建 | 一期 |
| REQ-DOC-002 | 公文管理 | s3_office / pc-document | 待 S1 分配 | 发文提交审批 | 待创建 | 一期 |
| REQ-DOC-003 | 公文管理 | s3_office / pc-document | 待 S1 分配 | 自动文号 | 待创建 | 一期 |
| REQ-DOC-004 | 公文管理 | s3_office / pc-document | 待 S1 分配 | 收文登记 | 待创建 | 一期 |
| REQ-DOC-005 | 公文管理 | s3_office / pc-document | 待 S2 分配 | 发文归档 | 待创建 | 二期 |
| REQ-DOC-006 | 公文管理 | s3_office / pc-document | 待 S2 分配 | 公文统计 | 待创建 | 二期 |
| REQ-EXCHANGE-001 | 公文交换 | s3_office / pc-document | 待 S5 分配 | 收文登记/传递 | 待创建 | 三期 |
| REQ-EXCHANGE-002 | 公文交换 | s3_office / pc-document | 待 S5 分配 | 收文查询 | 待创建 | 三期 |
| REQ-EXCHANGE-003 | 公文交换 | s3_office / pc-document | 待 S5 分配 | 发文传输基础 | 待创建 | 三期 |
| REQ-OFFICE-001 | 综合办公 | s4_office / pc-office | 待 S1 分配 | 用车申请流程 | 待创建 | 一期 |
| REQ-OFFICE-002 | 综合办公 | s4_office / pc-office | 待 S1 分配 | 请休假申请流程 | 待创建 | 一期 |
| REQ-OFFICE-003 | 综合办公 | s4_office / pc-office | 待 S2 分配 | 用印申请流程 | 待创建 | 二期 |
| REQ-OFFICE-004 | 综合办公 | s4_office / pc-office | 待 S2 分配 | 出差申请流程 | 待创建 | 二期 |
| REQ-OFFICE-005 | 综合办公 | s4_office / pc-office | 待 S2 分配 | 资产管理 | 待创建 | 二期 |
| REQ-OFFICE-006 | 综合办公 | s4_office / pc-office | 待 S2 分配 | 资产领用 | 待创建 | 二期 |
| REQ-OFFICE-007 | 综合办公 | s4_office / pc-office | 待 S2 分配 | 办公用品出入库 | 待创建 | 二期 |
| REQ-OFFICE-008 | 综合办公 | s4_office / pc-office | 待 S5 分配 | 智慧考勤打卡 | 待创建 | 三期 |
| REQ-CONTACTS-001 | 通讯录 | s5_contacts / pc-contacts | 待 S1 分配 | 组织架构 | 待创建 | 一期 |
| REQ-CONTACTS-002 | 通讯录 | s5_contacts / pc-contacts | 待 S1 分配 | 通讯录树 | 待创建 | 一期 |
| REQ-CONTACTS-003 | 通讯录 | s5_contacts / pc-contacts | 待 S1 分配 | 联系人搜索与详情 | 待创建 | 一期 |
| REQ-WORKBENCH-001 | 工作台 | s1_portal / pc-workbench | 待 S1 分配 | 个人信息 | 待创建 | 一期 |
| REQ-WORKBENCH-002 | 工作台 | s1_portal / pc-workbench | 待 S1 分配 | 修改密码 | 待创建 | 一期 |
| REQ-WORKBENCH-003 | 工作台 | s1_portal / pc-workbench | 待 S1 分配 | 待办/已办/办结 | 待创建 | 一期 |
| REQ-WORKBENCH-004 | 工作台 | s1_portal / pc-workbench | 待 S1 分配 | 我的发起 | 待创建 | 一期 |
| REQ-WORKBENCH-005 | 工作台 | s1_portal / pc-workbench | 待 S2 分配 | 流程起草 | 待创建 | 二期 |
| REQ-WORKBENCH-006 | 工作台 | s1_portal / pc-workbench | 待 S2 分配 | 委托授权（转办） | 待创建 | 二期 |
| REQ-MOBILE-001 | 移动办公 | mobile / h5-message | 待 S1 分配 | 移动消息 | 待创建 | 一期 |
| REQ-MOBILE-002 | 移动办公 | mobile / h5-notice | 待 S1 分配 | 移动公告 | 待创建 | 一期 |
| REQ-MOBILE-003 | 移动办公 | mobile / h5-approve | 待 S1 分配 | 移动流程审批与发起 | 待创建 | 一期 |
| REQ-MOBILE-004 | 移动办公 | mobile / h5-file | 待 S1 分配 | 移动文件查询 | 待创建 | 一期 |
| REQ-MOBILE-005 | 移动办公 | mobile / h5-contacts | 待 S2 分配 | 移动通讯录 | 待创建 | 二期 |
| REQ-MOBILE-006 | 移动办公 | mobile / h5-mine | 待 S2 分配 | 移动个人设置 | 待创建 | 二期 |
| REQ-PLATFORM-001 | 应用支撑平台 | s0_xtsz / pc-system | 待 S1 分配 | 身份认证（JWT） | 待创建 | 一期 |
| REQ-PLATFORM-002 | 应用支撑平台 | s0_xtsz / pc-system | 待 S2 分配 | 权限管理（RBAC + 菜单/按钮） | 待创建 | 二期 |
| REQ-PLATFORM-003 | 应用支撑平台 | s0_xtsz / pc-system | 待 S2 分配 | 组织架构 | 待创建 | 二期 |
| REQ-PLATFORM-004 | 应用支撑平台 | s0_xtsz / pc-system | 待 S2 分配 | 成员管理 | 待创建 | 二期 |
| REQ-PLATFORM-005 | 应用支撑平台 | s0_xtsz / pc-system | 待 S2 分配 | 日志管理 | 待创建 | 二期 |
| REQ-PLATFORM-006 | 应用支撑平台 | s0_xtsz / pc-system | 待 S2 分配 | 文件服务 | 待创建 | 二期 |
| REQ-PLATFORM-007 | 应用支撑平台 | s4_workflow / pc-system | 待 S5 分配 | 流程管理 | 待创建 | 三期 |
| REQ-PLATFORM-008 | 应用支撑平台 | s0_xtsz / pc-system | 待 S5 分配 | 水印/安全策略 | 待创建 | 三期 |

## 统计

- 一期：31 项
- 二期：13 项
- 三期：9 项
- 总计：53 项

## 说明

- 业务包名称 `s0_xtsz`、`s1_portal` 等为暂定编码，需经 ADR 与领域地图确认。
- 工作包、验收标准和测试证据将在 S1-S5 阶段逐步细化。
- 三期需求当前仅保留映射，不进入当前实施工作包。
