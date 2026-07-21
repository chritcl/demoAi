---
status: active
---

# 架构决策记录（ADR）

## 状态定义

- `proposed`：已提出，等待评审。
- `accepted`：已接受，成为项目约束。
- `rejected`：已拒绝。
- `superseded`：已被新 ADR 替代。

## ADR 列表

| 编号 | 标题 | 状态 |
|---|---|---|
| ADR-001 | JDK 与 Spring Boot 版本基线 | proposed |
| ADR-002 | 业务包分层单体和包边界验证方案 | proposed |
| ADR-003 | 流程引擎选型 | proposed |
| ADR-004 | 认证与 Token 模型 | proposed |
| ADR-005 | 数据库迁移与业务包数据所有权 | proposed |
| ADR-006 | API 契约与 OpenAPI 客户端生成 | proposed |
| ADR-007 | 文件存储抽象 | proposed |
| ADR-008 | PC 前端目录和状态管理 | proposed |
| ADR-009 | uni-app 支持端范围 | proposed |
| ADR-010 | 日志、审计和可观测性 | proposed |

## 流程

1. 提出 ADR，状态为 `proposed`。
2. 在对应阶段门禁前完成评审。
3. 评审通过后状态改为 `accepted`。
4. 如需替代，新建 ADR 并将原 ADR 标记为 `superseded`。
