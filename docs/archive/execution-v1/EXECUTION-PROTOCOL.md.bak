---
status: active
---

# 执行协议（EXECUTION-PROTOCOL）

本文件定义 Codex 每次启动后必须遵循的固定执行流程。所有写入必须对应到处于 `in_progress` 的工作包。

当前阶段、活动门禁和工作包状态以 `docs/execution/STATE.json` 为唯一真源；本协议不再硬编码这些动态信息。

## 文档权威顺序

1. 用户当前明确指令。
2. `AGENTS.md`
3. `docs/product/GOAL.md` 和 `docs/product/SCOPE.md`
4. 已接受的 ADR
5. active 架构文档
6. 当前工作包规范
7. 原始功能清单与流程材料
8. `docs/legacy/` 与 `old/`

## 启动流程

Codex 每次进入仓库后，按以下顺序执行只读检查：

1. 读取 `AGENTS.md`。
2. 读取 `docs/product/GOAL.md`。
3. 读取 `docs/execution/STATE.json`。
4. 读取 `docs/execution/EXECUTION-PROTOCOL.md`。
5. 读取 `docs/execution/STAGE-GATES.md`。
6. 读取当前 `in_progress` 或优先级最高的 `ready` 工作包规范。

## 工作区检查

启动后必须立即执行：

```powershell
git status --short
git branch --show-current
```

根据结果进入以下分支之一：

- 当前分支不是 `codex/execution-system` 且不是 `main`：
  - 报告 `BLOCKED`。
  - 停止。
- 当前分支是 `main`：
  - 如果 `codex/execution-system` 不存在，创建并切换。
  - 如果已存在，切换到该分支。
- 当前分支是 `codex/execution-system`：
  - 继续执行。

如果工作区存在未提交修改：

- 不得丢弃。
- 不得执行 `git reset --hard` 或 `git clean -fd`。
- 如果这些修改无法明确归属于当前 `in_progress` 工作包，报告 `BLOCKED` 并停止。
- 如果可归属，在 evidence 中记录后继续。

## 工作包执行流程

1. 确认当前处于 `in_progress` 的工作包；如果不存在，选择优先级最高的 `ready` 工作包，将其状态更新为 `in_progress`。
2. 读取该工作包规范。
3. 按规范执行，所有写入对应到该工作包。
4. 工作包完成后：
   - 运行 `node tools/execution/validate.mjs`。
   - 运行 `git diff --check`。
   - 如果通过，将状态更新为 `review`。
   - 再次运行验证。
   - 如果仍通过，将状态更新为 `done`，填写 `completed_at` 和 `evidence`。
   - 运行 `git status --short` 确认工作区干净。
5. 每个工作包独立提交。

## 门禁与阶段升级

- 阶段和门禁定义在 `docs/execution/STAGE-GATES.md`。
- 当前阶段、活动门禁、门禁状态以 `docs/execution/STATE.json` 为唯一真源。
- 阶段工作进行中时，`active_gate` 为 `null`。
- 只有进入门禁评估时才设置对应 Gate ID。
- 自动门禁通过后，由 Codex 更新 `active_stage` 和 `active_gate`。
- 人工/hybrid 门禁到达 `waiting_human` 状态时，必须停止并等待用户明确批准。

## 强制停止条件

出现以下任意情况时停止自动执行：

- 工作区存在无法归属的用户修改。
- 在 `old/` 之外发现新的真实凭据。
- 需要修改 Git 历史。
- 需要访问真实服务器或数据库。
- 两份同级权威文档互相冲突。
- 工作包范围明显扩大。
- 需要决定流程引擎。
- 需要决定最终技术版本。
- 需要决定跨业务包数据库关系。
- 自动验证无法执行。
- 到达人工门禁条件。

## 分支策略

- 所有执行体系工作包在 `codex/execution-system` 分支完成。
- 不得推送到远程。
- 未来业务工作包进入 S1 后，按 `STAGE-GATES.md` 规定的分支策略执行。

## 与 AGENTS.md 的关系

本文件是执行流程；`AGENTS.md` 是仓库级权威和禁止项。两者冲突时，以 `AGENTS.md` 为准，但冲突本身应作为强制停止条件上报。
