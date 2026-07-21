---
status: active
---

# 执行协议（EXECUTION-PROTOCOL）

本文件定义 Codex 每次启动后必须遵循的固定执行流程。所有写入必须对应到处于 `in_progress` 的工作包。

## 文档权威顺序

1. `AGENTS.md`
2. `docs/product/GOAL.md`
3. `docs/execution/STATE.json`
4. `docs/execution/EXECUTION-PROTOCOL.md`（本文件）
5. `docs/execution/STAGE-GATES.md`
6. `docs/execution/WORK-PACKAGE-TEMPLATE.md`
7. 工作包规范：`docs/execution/work-packages/WP-XXX-*.md`
8. 证据：`docs/execution/evidence/`
9. 架构约束：`docs/architecture/`
10. 遗留参考：`docs/legacy/` 与 `old/`

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
- 如果这些修改无法明确归属于一个已有的 `in_progress` 工作包，则停止，报告 `BLOCKED`。
- 如果可以明确归属于当前 `in_progress` 工作包，按恢复协议继续。

## 状态机

每个工作包具有以下状态：

- `draft`：已定义，未准备就绪。
- `ready`：依赖已满足，可开始执行。
- `in_progress`：正在执行，所有写入必须对应到该工作包。
- `review`：已完成实现，等待验证或人工评审。
- `done`：已通过验证，可追踪到提交。
- `blocked`：因阻塞原因暂停。

状态转换规则：

- `draft` → `ready`：当所有依赖工作包均为 `done`。
- `ready` → `in_progress`：当没有更高优先级或同阶段的 `in_progress` 工作包。
- `in_progress` → `review`：实现完成，已生成 evidence，准备验证。
- `review` → `done`：验证通过，已提交。
- 任何状态 → `blocked`：遇到强制停止条件。
- `blocked` → `ready` 或 `in_progress`：阻塞条件解除后由恢复协议决定。

## 工作包选择算法

1. 如果存在 `in_progress` 工作包，优先继续执行该工作包。
2. 如果不存在 `in_progress` 工作包，选择优先级最高、状态为 `ready` 的工作包。
3. 如果同阶段存在多个 `ready` 工作包，按 `priority` 字段升序选择。
4. 如果选定工作包有依赖未完成，则不得将其标记为 `in_progress`。

## 阶段与门禁

阶段和门禁定义在 `STAGE-GATES.md` 中。

- 当前阶段为 `S0`，目标门禁为 `G0`。
- 只有当本阶段所有工作包完成且门禁条件满足时，才能进入下一阶段。
- 自动门禁未通过时禁止进入下一阶段。
- 人工门禁必须等待用户明确批准。

## 写入规则

1. 所有写入必须对应到处于 `in_progress` 的工作包。
2. 禁止写入不属于当前工作包范围的路径。
3. 禁止修改 Git 历史。
4. 禁止丢弃用户未提交修改。
5. 所有文件写入必须使用 UTF-8 without BOM。
6. 代码注释必须使用中文。

## 提交规则

每个工作包独立提交：

- 提交信息格式：`{类型}(WP-XXX): {描述}`。
- 类型可以是 `chore`、`docs`、`ci`、`feat`、`refactor` 等。
- 提交信息中必须包含工作包 ID，例如 `(WP-002)`。
- 禁止提交信息：`1`、`add`、`update`、`fix`、`done`。

提交前必须执行：

```powershell
node tools/execution/validate.mjs
git diff --check
git status --short
```

提交后必须再次执行：

```powershell
node tools/execution/validate.mjs
git status --short
```

## 验证与证据

每个工作包必须生成 evidence：

- 证据路径：`docs/execution/evidence/WP-XXX-*.md`。
- 证据必须包含：完成内容、验证命令、验证结果、提交 SHA、已知风险。
- 工作包完成后，必须执行其规范中定义的验证命令。

## 恢复协议

当会话中断或工作区存在未提交修改时，按以下协议恢复：

1. 读取 `STATE.json` 确认当前 `in_progress` 工作包。
2. 执行 `git status --short` 确认未提交修改。
3. 如果未提交修改可明确归属于当前 `in_progress` 工作包，则继续执行。
4. 如果状态不一致（例如依赖未完成的工作包被标记为 `in_progress`），先修正 `STATE.json`，再恢复执行。
5. 如果无法恢复，报告 `BLOCKED` 并等待用户指令。

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