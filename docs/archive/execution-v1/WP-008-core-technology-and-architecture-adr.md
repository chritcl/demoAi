---
status: draft
---

# WP-008：核心技术与架构 ADR

## 目标

完成 S1 所需的全部核心技术架构决策记录（ADR），为 G1 门禁提供决策真源。

## 范围

- JDK / Spring Boot 版本基线（ADR-001）。
- 业务包分层单体与包边界验证（ADR-002）。
- 流程引擎选型（ADR-003）。
- 认证模型（ADR-004）。
- 数据库迁移工具（ADR-005）。
- API 契约规范（ADR-006）。
- 文件存储策略（ADR-007）。
- PC 端架构（ADR-008）。
- uni-app 范围（ADR-009）。
- 审计与可观测性（ADR-010）。

## 不在范围

- 不实现具体工程骨架。
- 不开始业务功能开发。
- 不修改已被 accepted 的 ADR 历史。

## 依赖

- WP-007：领域地图与业务包边界（done）

## 风险

- 高：流程引擎选型直接影响跨包调用契约。
- 高：技术版本需要用户确认，可能受团队技能约束。
- 中：多个 ADR 之间存在依赖，需要顺序评审。

## 验收标准

1. 所有 S1 相关 ADR 状态为 accepted 或明确 blocked。
2. 每个 ADR 包含上下文、选项、决策、后果、相关 ADR。
3. 决策与 DOMAIN-MAP.md 一致。

## 证据

完成后在 `docs/execution/evidence/WP-008-core-technology-and-architecture-adr.md` 中记录。
