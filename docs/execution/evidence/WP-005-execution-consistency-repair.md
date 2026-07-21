---
status: active
---

# WP-005 完成证据：G0 后执行体系一致性修复

## 基本信息

- 工作包：WP-005
- 标题：Post-G0 execution consistency repair
- 阶段：S1
- 状态：done
- 完成时间：2026-07-21T23:35:00+08:00
- 证据文件：本文件

## 实际修改文件

### 修改文件

- `README.md`
- `AGENTS.md`
- `docs/README.md`
- `docs/product/GOAL.md`
- `docs/product/SCOPE.md`
- `docs/product/TRACEABILITY.md`
- `docs/execution/EXECUTION-PROTOCOL.md`
- `docs/execution/STAGE-GATES.md`
- `docs/execution/ROADMAP.md`
- `docs/execution/STATE.json`
- `docs/quality/README.md`
- `docs/operations/README.md`
- `docs/architecture/DOMAIN-MAP.md`
- `docs/architecture/DATA-ARCHITECTURE.md`
- `docs/architecture/adr/ADR-001.md`
- `tools/execution/validate.mjs`
- `.github/workflows/execution-contract.yml`
- `old/oa-backend/src/main/resources/application.yml`

### 新增文件

- `docs/execution/work-packages/WP-005-execution-consistency-repair.md`
- `docs/execution/work-packages/WP-006-requirement-baseline.md`
- `docs/execution/work-packages/WP-007-domain-map-and-package-boundary.md`
- `docs/execution/work-packages/WP-008-core-technology-and-architecture-adr.md`
- `docs/execution/work-packages/WP-009-api-security-and-non-functional-baseline.md`
- `docs/execution/work-packages/WP-010-g1-gate-materials.md`
- `docs/execution/gates/G0.md`
- 本证据文件：`docs/execution/evidence/WP-005-execution-consistency-repair.md`

## 修复的状态漂移

1. `README.md` 不再硬编码当前阶段、当前门禁、当前工作包、最近完成工作包或执行分支，统一声明以 `docs/execution/STATE.json` 为唯一真源。
2. `AGENTS.md` 权威顺序修正为用户指令 > AGENTS.md > GOAL/SCOPE > 已接受 ADR > active 架构 > 当前工作包 > 原始功能清单 > 遗留参考；明确 `STATE.json` 只记录动态执行状态，不覆盖产品/架构决策。
3. `docs/execution/EXECUTION-PROTOCOL.md` 和 `docs/execution/ROADMAP.md` 不再硬编码动态状态，只描述执行流程和阶段目标。
4. `docs/quality/README.md` 和 `docs/operations/README.md` 移除硬编码的 S0/G0/WP-002 描述。

## 修复后的阶段模型

- 阶段恢复为 S0～S6：
  - S0：仓库治理与执行底座
  - S1：需求与架构基线
  - S2：工程骨架
  - S3：第一条端到端纵向切片
  - S4：一期能力
  - S5：二期能力
  - S6：发布准备
- 门禁调整：
  - G1 只要求需求、领域地图、业务包、数据所有权、依赖方向、契约、ADR、数据架构、API/安全/非功能基线批准；不再要求工程骨架已创建。
  - G2 才要求后端/PC/移动端骨架可构建、数据库迁移基线可执行、CI 通过、ArchUnit 通过。
  - G3 对应第一条纵向切片人工验收。

## TRACEABILITY 修复统计

- 已按功能清单“阶段”列校正以下条目：
  - 用印：二期 → 一期
  - 出差：二期 → 一期
  - 智慧考勤：三期 → 二期
  - 权限管理：二期 → 一期
  - 组织架构：二期 → 一期
  - 成员管理：二期 → 一期
  - 日志管理：二期 → 一期
  - 文件服务：二期 → 一期
  - 流程管理：三期 → 一期
  - 流程起草：二期 → 一期
  - 公文交换：保持三期 deferred
- 当前临时统计：42/9/4（总计 55），与系统导图目标 31/14/8（总计 53）不一致，已标记为 `needs-review` 并交由 WP-006 复核。

## 业务包编号修复结果

- 信息发布、通知公告、公文管理统一映射到 `s2_document`（移除不确定的 `s1_portal 或 s2_document` 和错误的 `s3_office`）。
- 综合办公映射到 `s3_office`（移除不存在的 `s4_office`）。
- 流程能力保持映射到 `s4_workflow`。
- 业务包候选清单保持：`s0_xtsz`、`s1_portal`、`s2_document`、`s3_office`、`s4_workflow`、`s5_contacts`、`s6_file`、`s7_message`、`s8_audit`。
- 未新增或拆分业务包，最终方案由 G1 批准。

## 验证器新增检查

- STATE 与阶段关系：验证 active_stage 存在、active_gate 属于 active_stage、阶段工作进行中时 active_gate 为 null、当前阶段有可执行工作包、最多一个 in_progress、依赖已完成。
- 禁止动态状态副本：检查 README.md、AGENTS.md、EXECUTION-PROTOCOL.md、ROADMAP.md、quality/README.md、operations/README.md 中不再硬编码当前阶段/门禁/工作包。
- active 文档遍历：递归扫描 active Markdown，检查 frontmatter 状态并验证相对链接目标存在。
- 业务包一致性：检查 TRACEABILITY 中业务包必须存在于 DOMAIN-MAP，可发现 `s4_office` 等错误。
- 需求阶段统计：统计 TRACEABILITY 的一期/二期/三期，检查是否误用 P0/P1/P2，检查三期 deferred。
- Owner 检查：DOMAIN-MAP 中 owner 不得含“或”、不得多 owner、owner 必须存在。
- 门禁证据：approved 的 human/hybrid 门禁必须存在证据文件。
- 循环依赖：使用标准 DFS 检测工作包循环依赖。

## CI 修复内容

- `.github/workflows/execution-contract.yml` 改为：
  - `pull_request`：拉取 base ref 后执行 `git diff --check origin/<base_ref>...HEAD`。
  - `push`：执行 `git show --check --oneline --no-renames HEAD`。
  - 继续执行 `node tools/execution/validate.mjs`。

## G0 证据状态

- 已创建 `docs/execution/gates/G0.md`，记录 G0 自动条件、WP-000～WP-004 状态、批准提交 SHA（216dba7）、已知风险。
- G0 自动条件已满足，但 Git 历史保留旧凭据，仓库无法证明外部凭据已完成轮换或环境已下线，该风险继续保留。

## 未解决风险

1. 需求追踪矩阵（TRACEABILITY）与功能清单/系统导图的阶段统计口径不一致（42/9/4 总计 55 vs 31/14/8 总计 53），由 WP-006 复核并冻结最终基线。
2. 旧 Git 历史保留凭据，G0 只能证明当前树已脱敏，不能证明外部凭据已轮换。
3. 业务包编号仍为候选基线，需 G1 批准。
4. 流程引擎、JDK/Spring Boot 最终版本等 ADR 尚未完成决策。

## 下一步唯一入口

- WP-006：需求基线与阶段映射（状态：ready）。
- 未开始 WP-006 的具体执行；未批准任何 ADR；未创建工程骨架；未开发业务代码；未推送远程。
