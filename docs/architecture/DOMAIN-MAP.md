---
status: active
---

# 领域地图

## 说明

本文件定义业务域、业务包、数据表 owner 和允许的跨包调用契约。所有编码和架构决策必须以本文件为参考。

## 业务域与业务包

| 业务域 | 业务包编码 | 说明 | 主要能力 |
|---|---|---|---|
| 系统设置 | s0_xtsz | 平台基础与用户/权限/组织 | 身份认证、权限、组织架构、成员、日志、文件 |
| 统一门户 | s1_portal | 门户首页、工作台、搜索 | 门户首页、消息、待办、已办、搜索 |
| 信息发布 | s2_document | 信息、公告、公文 | 信息发布、通知公告、公文管理 |
| 综合办公 | s3_office | 用车、用印、出差、请休假、资产、用品、考勤 | 各类审批流程 |
| 流程引擎 | s4_workflow | 流程定义、实例、任务 | 流程引擎抽象 |
| 通讯录 | s5_contacts | 组织与联系人 | 组织架构、通讯录、搜索 |
| 文件服务 | s6_file | 文件存储 | 文件上传、下载、存储抽象 |
| 消息服务 | s7_message | 消息与通知 | 站内消息、通知、推送 |
| 审计 | s8_audit | 审计日志 | 操作审计、安全审计 |

## 数据表所有权（初稿）

| 数据表/表组 | Owner 业务包 | 说明 |
|---|---|---|
| sys_user, sys_role, sys_menu, sys_dept, sys_dict_* | s0_xtsz | 用户权限组织 |
| portal_article, portal_column, portal_read_record | s1_portal 或 s2_document | 需确认 |
| doc_official, doc_send_template, doc_opinion | s2_document | 公文 |
| office_leave, office_vehicle, office_seal, office_trip, office_asset_*, office_supply_* | s3_office | 综合办公 |
| flow_definition, flow_instance, flow_node, flow_task | s4_workflow | 流程引擎 |
| contacts_* | s5_contacts | 通讯录 |
| sys_file | s6_file | 文件 |
| message_* | s7_message | 消息 |
| sys_oper_log, audit_* | s8_audit | 审计 |

## 跨包调用契约（待 ADR 冻结）

| 调用方 | 被调用方 | 契约 | 状态 |
|---|---|---|---|
| s1_portal | s0_xtsz | 用户信息查询 | 待设计 |
| s3_office | s4_workflow | 流程启动/查询 | 待设计 |
| s2_document | s4_workflow | 流程启动/查询 | 待设计 |
| s7_message | s0_xtsz | 用户消息发送 | 待设计 |

## 禁止

- 禁止跨业务包直接访问 Mapper/Entity。
- 禁止循环依赖。
- 禁止未经登记的跨包 Service 调用。

## 说明

- 本文件为初稿，需经 G1 人工门禁批准。
- 流程引擎 ADR 通过后，将更新跨包调用契约。
