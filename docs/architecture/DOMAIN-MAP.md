---
status: active
---

# 领域地图

## 说明

本文件定义业务域、业务包、数据表 owner 和允许的跨包调用契约。所有编码和架构决策必须以本文件为参考。

> 本文件为 S1 候选基线，需经 G1 人工门禁批准。批准前不得生成业务代码。WP-007 负责进一步细化并冻结。

## 业务域与业务包

| 业务域 | 业务包编码 | 说明 | 主要能力 |
|---|---|---|---|
| 系统设置 | s0_xtsz | 平台基础与用户/权限/组织 | 身份认证、用户、角色、权限、组织、字典、系统设置 |
| 统一门户 | s1_portal | 门户首页、工作台、搜索 | 门户首页、消息、待办、已办、搜索 |
| 信息发布 | s2_document | 信息、公告、公文 | 信息发布、通知公告、公文管理 |
| 综合办公 | s3_office | 用车、用印、出差、请休假、资产、用品、考勤 | 各类审批流程 |
| 流程引擎 | s4_workflow | 流程定义、实例、任务 | 流程引擎抽象 |
| 通讯录 | s5_contacts | 组织与联系人 | 组织架构、通讯录、搜索 |
| 文件服务 | s6_file | 文件存储 | 文件上传、下载、存储抽象 |
| 消息服务 | s7_message | 消息与通知 | 站内消息、通知、推送 |
| 审计 | s8_audit | 审计日志 | 操作审计、安全审计 |

## 数据表所有权（候选基线）

| 数据表/表组 | Owner 业务包 | 说明 |
|---|---|---|
| sys_user, sys_role, sys_menu, sys_dept, sys_dict_* | s0_xtsz | 用户、角色、菜单、部门、字典等系统主数据 |
| portal_article, portal_column, portal_read_record, portal_notice, portal_notice_read | s2_document | 信息发布、通知公告 |
| doc_official, doc_send_template, doc_opinion | s2_document | 公文管理 |
| office_leave, office_vehicle, office_seal, office_trip, office_asset_*, office_supply_*, office_attendance_* | s3_office | 综合办公 |
| flow_definition, flow_instance, flow_node, flow_task | s4_workflow | 流程引擎 |
| contacts_* | s5_contacts | 通讯录扩展表 |
| sys_file | s6_file | 文件元数据与存储抽象 |
| message_* | s7_message | 消息与通知 |
| sys_oper_log, audit_* | s8_audit | 操作审计、安全审计 |

## 通讯录与系统设置边界

- `s0_xtsz` 是 `sys_user`、`sys_dept` 等组织/用户主数据的唯一 owner。
- `s5_contacts` 通过已登记的公开 Service 接口或只读模型使用组织和用户信息，不得直接访问 `s0_xtsz` 的 Mapper/Entity。
- 最终边界由 WP-007 细化并 G1 批准。

## 待决策事项

- 信息发布、通知公告、公文管理当前暂归 `s2_document`。WP-007 可提出拆分方案，但不得在 WP-005 中新增业务包。
- `contacts_*` 的具体表结构与 `sys_user`/`sys_dept` 的只读同步方式由 WP-007 决定。
- 跨包调用契约的具体接口定义由 WP-008 和 WP-009 在 ADR 批准后细化。

## 跨包调用契约（待 ADR 冻结）

| 调用方 | 被调用方 | 契约 | 状态 |
|---|---|---|---|
| s1_portal | s0_xtsz | 用户信息查询 | 待设计 |
| s5_contacts | s0_xtsz | 组织与用户只读查询 | 待设计 |
| s3_office | s4_workflow | 流程启动/查询 | 待设计 |
| s2_document | s4_workflow | 流程启动/查询 | 待设计 |
| s7_message | s0_xtsz | 用户消息发送 | 待设计 |
| s2_document | s6_file | 文件上传/下载 | 待设计 |
| s3_office | s6_file | 文件上传/下载 | 待设计 |

## 禁止

- 禁止跨业务包直接访问 Mapper/Entity。
- 禁止循环依赖。
- 禁止未经登记的跨包 Service 调用。
- 禁止在 WP-005 中新增业务包或拆分现有业务包。

## 说明

- 本文件为候选基线，需经 G1 人工门禁批准。
- 流程引擎 ADR 通过后，将更新跨包调用契约。
- 所有 owner 业务包必须唯一，不允许出现“或”表示多个候选 owner。
