---
status: active
---

# 文档导航

本目录是 demoAi 仓库的文档真源。所有受文档一致性验证约束的 Markdown 文件均包含 `status` 元数据。

## 产品真源

- [`product/GOAL.md`](./product/GOAL.md) — 总目标、成功标准、范围、不在范围、外部依赖、已知风险、完成定义
- [`product/SCOPE.md`](./product/SCOPE.md) — 当前范围与暂不实施
- [`product/TRACEABILITY.md`](./product/TRACEABILITY.md) — 需求编号、业务域、业务包/前端功能域、工作包、验收标准、测试证据、阶段

## 架构真源

- [`architecture/BACKEND-ARCHITECTURE.md`](./architecture/BACKEND-ARCHITECTURE.md) — 后端业务包分层单体架构（WP-003）
- [`architecture/DATA-ARCHITECTURE.md`](./architecture/DATA-ARCHITECTURE.md) — 数据库重新设计规则（WP-003）
- [`architecture/DOMAIN-MAP.md`](./architecture/DOMAIN-MAP.md) — 领域地图与业务包数据所有权（WP-003）
- [`architecture/adr/`](./architecture/adr/) — 架构决策记录（WP-003）

## 执行体系

- [`execution/EXECUTION-PROTOCOL.md`](./execution/EXECUTION-PROTOCOL.md) — Codex 自动执行协议（WP-002）
- [`execution/STAGE-GATES.md`](./execution/STAGE-GATES.md) — 阶段与门禁（WP-002）
- [`execution/WORK-PACKAGE-TEMPLATE.md`](./execution/WORK-PACKAGE-TEMPLATE.md) — 工作包模板
- [`execution/STATE.json`](./execution/STATE.json) — 机器可读执行状态
- [`execution/work-packages/`](./execution/work-packages/) — 工作包规范
- [`execution/evidence/`](./execution/evidence/) — 工作包完成证据

## 质量体系

- [`quality/README.md`](./quality/README.md) — 质量文档边界、当前状态与后续工作包

## 运维体系

- [`operations/README.md`](./operations/README.md) — 运维文档边界、当前状态与后续工作包

## 遗留参考

- [`legacy/LEGACY-REFERENCE-POLICY.md`](./legacy/LEGACY-REFERENCE-POLICY.md) — 遗留代码参考策略
- [`old/README.md`](../old/README.md) — 遗留代码区边界声明
- `legacy/` 与 `old/` 中其余文件

## 旧文档状态

| 文档 | 状态 | 说明 |
|---|---|---|
| `技术栈约束/01-后端技术栈约束.md` | needs-review | 版本需经 ADR 冻结 |
| `技术栈约束/02-PC端技术栈约束.md` | needs-review | 版本需经 ADR 冻结 |
| `技术栈约束/03-移动端技术栈约束.md` | active | 已确认 uni-app + Vue3 |
| `系统导图.md` | needs-review | 旧包结构，需按新业务包分层单体调整 |
| `oa/00-流程总览.md` | needs-review | 流程参考，需重新验证 |
| `oa/01-信息发布审核流程.md` | needs-review | 流程参考，需重新验证 |
| `oa/02-发文审批流程.md` | needs-review | 流程参考，需重新验证 |
| `oa/03-收文办理流程.md` | needs-review | 流程参考，需重新验证 |
| `oa/04-请休假审批流程.md` | needs-review | 流程参考，需重新验证 |
| `oa/05-用车审批流程.md` | needs-review | 流程参考，需重新验证 |
| `oa/06-用印审批流程.md` | needs-review | 流程参考，需重新验证 |
| `oa/07-出差审批流程.md` | needs-review | 流程参考，需重新验证 |
| `oa/08-资产领用流程.md` | needs-review | 流程参考，需重新验证 |
| `oa/09-办公用品申请流程.md` | needs-review | 流程参考，需重新验证 |
| `建库/oa_platform_full.sql` | legacy-reference | 旧建库 SQL，仅作参考 |

## 索引说明

- `active`：当前有效，受一致性验证约束。
- `needs-review`：需要重新确认，暂不执行一致性验证。
- `legacy-reference`：仅供遗留参考，不执行一致性验证。
- `superseded`：已被新文档替代。
