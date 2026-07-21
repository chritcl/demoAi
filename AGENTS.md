# AGENTS.md — demoAi 仓库执行权威

## 文档权威顺序

1. 用户当前明确指令。
2. 根目录 `AGENTS.md`（本文件）。
3. `docs/product/GOAL.md` 和 `docs/product/SCOPE.md`。
4. 已接受的 ADR（`docs/architecture/adr/ADR-*.md` 中状态为 `accepted` 的记录）。
5. active 架构文档（`docs/architecture/` 中 `status: active` 的文档）。
6. 当前工作包规范（`docs/execution/work-packages/WP-XXX-*.md` 中处于 `in_progress` 或 `ready` 的工作包）。
7. 原始功能清单与流程材料（`docs/功能开发清单.xlsx`、`docs/系统导图.md`、`docs/oa/` 等）。
8. 遗留参考：`docs/legacy/` 与 `old/`。

> `docs/execution/STATE.json` 是动态执行状态的唯一真源，记录当前阶段、活动门禁、工作包状态、门禁状态和阻塞原因。`STATE.json` 不覆盖产品和架构决策，工作包不得覆盖已接受的 ADR 或 active 架构约束。
>
> `docs/execution/evidence/` 只证明执行结果，不产生新的架构规范或产品决策。

## 禁止项

- 禁止把 `old/` 中的代码直接复制为新实现。
- 禁止新代码依赖 `old/`。
- 禁止在文档中把旧功能声明为新系统已完成。
- 禁止在 active 文档中将 Vant 声明为移动端当前技术栈。
- 禁止在 `old/` 之外硬编码密码、密钥、数据库地址、Redis 地址和默认账号。
- 禁止在提交信息中使用 `1`、`add`、`update`、`fix`、`done`。
- 禁止推送远程。
- 禁止修改 Git 历史。
- 禁止丢弃用户未提交修改。
- 禁止使用 `npm` 或 `yarn` 执行依赖安装、删除、更新及项目脚本。
- 禁止在依赖工作包完成前将后续工作包标记为 `in_progress`。
- 禁止在自动验证未通过时进入下一阶段。
- 禁止在工作包范围扩大时继续执行而不上报。

## 当前工作包入口

- 首次执行：读取 `docs/execution/STATE.json`，按 `EXECUTION-PROTOCOL.md` 恢复或选择工作包。
- 所有写入必须对应到处于 `in_progress` 的工作包。
- 状态、阻塞原因和验证证据必须保存在仓库中。

## 编码约束

- 所有代码注释使用中文。
- 所有文件写入使用 UTF-8 without BOM。
- 项目使用 `pnpm` 作为包管理工具。
- 项目依赖锁定文件统一使用 `pnpm-lock.yaml`。
- 禁止创建 `package-lock.json`、`yarn.lock`。

## 提交规范

- 提交信息必须包含工作包 ID，例如 `(WP-002)`。
- 推荐格式：`<类型>(WP-XXX): <描述>`。
- 类型可以是 `chore`、`docs`、`ci`、`feat`、`refactor` 等。
- 禁止提交信息：`1`、`add`、`update`、`fix`、`done`。
- 每个工作包独立提交。

## 验证要求

- 提交前必须执行 `node tools/execution/validate.mjs` 和 `git diff --check`。
- 提交后必须再次执行 `node tools/execution/validate.mjs` 和 `git status --short`。
- 验证失败不得进入下一阶段。

## 状态恢复

- Codex 新会话启动后必须按 `EXECUTION-PROTOCOL.md` 的启动流程执行只读检查。
- 工作区存在未提交修改时，必须判断其是否归属于当前 `in_progress` 工作包。
- 如果无法归属，报告 `BLOCKED` 并停止，不得修改任何仓库文件。

## 并行子智能体

- 只读审计可以并行执行。
- 所有写入由主协调智能体统一完成。
- 多个子智能体不得同时修改同一文件。
- 未来需要并行写入时，必须使用隔离 Git worktree。
- 未使用隔离 worktree 时，只允许并行只读分析。

## 强制停止条件

出现以下任意情况时停止自动执行：

- 工作区存在无法归属的用户修改。
- 在 `old/` 之外发现新的真实凭据，或凭据来源/用途无法判断。
- 需要修改 Git 历史。
- 需要访问真实服务器或数据库。
- 两份同级权威文档互相冲突。
- 工作包范围明显扩大。
- 需要决定流程引擎。
- 需要决定最终技术版本。
- 需要决定跨业务包数据库关系。
- 自动验证无法执行。
- G0 到达人工批准条件。

## 门禁与阶段

- 阶段和门禁定义在 `docs/execution/STAGE-GATES.md`。
- 当前阶段、活动门禁和门禁状态以 `docs/execution/STATE.json` 为唯一真源。
- 自动门禁通过后，hybrid 门禁仍需用户明确批准。
