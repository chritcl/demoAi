# demoAi / 协同办公平台

本仓库正在重建一个可部署、可测试、可审计、可持续演进的协同办公平台。

## 项目目标

以 `docs/功能开发清单.xlsx` 中的一期和二期功能为范围，完成协同办公平台的全新构建。
旧代码位于 `old/`，仅作参考，不得直接复用。

## 活动文档入口

1. [`AGENTS.md`](./AGENTS.md) — 执行权威与禁止项
2. [`docs/GOAL.md`](./docs/GOAL.md) — 总目标
3. [`docs/EXECUTION.md`](./docs/EXECUTION.md) — 执行规则
4. [`docs/STATE.json`](./docs/STATE.json) — 机器可读执行状态
5. [`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md) — 技术架构
6. [`docs/功能开发清单.xlsx`](./docs/功能开发清单.xlsx) — 业务范围唯一真源
7. [`docs/work-packages/`](./docs/work-packages/) — 工作包规范

## 技术架构

- 后端：单 Maven、单 Spring Boot、业务包分层单体架构。
- PC 端：Vue3 + TypeScript + Element Plus。
- 移动端：uni-app + Vue3 H5。

## 旧工程声明

根目录不再存在 `oa-backend`、`oa-frontend`、`oa-mobile` 可运行工程。旧工程已移入 `old/`。
所有旧启动脚本仅供参考，不保证可运行。

## 如何恢复 Codex 执行

Codex 新会话启动后，按以下顺序读取：

1. `AGENTS.md`
2. `docs/GOAL.md`
3. `docs/EXECUTION.md`
4. `docs/STATE.json`
5. 当前工作包（`docs/work-packages/WP-XX.md`）

然后根据状态选择处于 `ready` 或 `in_progress` 的工作包继续执行。

## 许可证

待定。
