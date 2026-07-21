# demoAi / 协同办公平台

> 本仓库正在重建一个可部署、可测试、可审计、可持续演进的协同办公平台。
> 当前阶段、活动门禁和工作包状态以 `docs/execution/STATE.json` 为唯一真源。

## 仓库状态

当前阶段、活动门禁、工作包状态、最近完成工作包和阻塞原因均以
[`docs/execution/STATE.json`](./docs/execution/STATE.json) 为唯一真源。
本 README 不再硬编码上述动态信息，以避免状态漂移。

## 权威入口

1. [`AGENTS.md`](./AGENTS.md) — 执行权威与禁止项
2. [`docs/product/GOAL.md`](./docs/product/GOAL.md) — 总目标
3. [`docs/execution/STATE.json`](./docs/execution/STATE.json) — 机器可读执行状态
4. [`docs/execution/EXECUTION-PROTOCOL.md`](./docs/execution/EXECUTION-PROTOCOL.md) — 执行协议
5. [`docs/execution/STAGE-GATES.md`](./docs/execution/STAGE-GATES.md) — 阶段与门禁
6. [`docs/execution/WORK-PACKAGE-TEMPLATE.md`](./docs/execution/WORK-PACKAGE-TEMPLATE.md) — 工作包模板
7. [`docs/execution/ROADMAP.md`](./docs/execution/ROADMAP.md) — 路线图
8. [`docs/execution/EVIDENCE-FORMAT.md`](./docs/execution/EVIDENCE-FORMAT.md) — 证据格式
9. [`docs/README.md`](./docs/README.md) — 文档导航

## 如何恢复 Codex 执行

Codex 新会话启动后，按以下顺序读取：

1. `AGENTS.md`
2. `docs/product/GOAL.md`
3. `docs/execution/STATE.json`
4. `docs/execution/EXECUTION-PROTOCOL.md`
5. `docs/execution/work-packages/WP-XXX-*.md`

然后根据状态选择处于 `ready` 或 `in_progress` 的工作包继续执行。

## 重要约束

- 旧代码已迁移至 `old/`，仅作为遗留参考。
- 禁止把 `old/` 中的代码直接复制为新实现。
- 禁止新代码依赖 `old/`。
- 旧配置、旧 SQL、旧实体和旧流程实现均不是新系统真源。
- 项目使用 `pnpm` 作为包管理工具。
- 项目依赖锁定文件统一使用 `pnpm-lock.yaml`。
- 移动端采用 `uni-app + Vue3`。
- PC 端采用 `Vue3 + TypeScript`。
- 后端采用单 Maven、单 Spring Boot、业务包分层单体架构。

## 旧工程声明

根目录不再存在 `oa-backend`、`oa-frontend`、`oa-mobile` 可运行工程。旧工程已移入 `old/`。请勿使用 README 中的旧启动命令。所有旧启动脚本（`old/start.bat`、`old/stop.bat`、`old/restart.bat`、`old/oa.ps1`）仅供参考，不保证可运行。

## 快速链接

- [文档总览](./docs/README.md)
- [产品目标与范围](./docs/product/GOAL.md)
- [执行协议](./docs/execution/EXECUTION-PROTOCOL.md)
- [阶段与门禁](./docs/execution/STAGE-GATES.md)
- [遗留参考策略](./docs/legacy/LEGACY-REFERENCE-POLICY.md)
- [旧代码参考](./old/README.md)

## 许可证

待定。
