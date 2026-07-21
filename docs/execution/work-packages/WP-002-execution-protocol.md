---
status: active
---

# WP-002：Codex 执行协议

## 标识

- ID：WP-002
- 标题：Codex execution protocol
- 所属 Stage：S0
- 状态：draft
- 优先级：2
- 风险等级：high
- 依赖工作包：[WP-001]
- 阻塞工作包：[WP-003, WP-004]

## 范围

### 允许修改路径

- `AGENTS.md`
- `docs/execution/STATE.json`
- `docs/execution/EXECUTION-PROTOCOL.md`
- `docs/execution/WORK-PACKAGE-TEMPLATE.md`
- `docs/execution/STAGE-GATES.md`
- `docs/execution/ROADMAP.md`
- `docs/execution/EVIDENCE-FORMAT.md`

### 禁止修改路径

- product 文档（WP-001）
- 架构文档（WP-003）
- 验证器代码（WP-004）
- 业务代码

## 目标

将 WP-000 创建的最小 Bootstrap 文件扩展为正式执行协议，使 Codex 可以跨会话恢复执行。

## 背景

仓库需要机器可读状态和可重复执行算法，避免依赖聊天上下文。

## 输入

- WP-001 输出
- 初始 `AGENTS.md`
- 初始 `STATE.json`
- 工作包模板

## 范围内

1. 扩展 `AGENTS.md` 为正式规则。
2. 完善 `STATE.json` 状态机。
3. 编写 `EXECUTION-PROTOCOL.md`：Codex 每次启动后的固定流程。
4. 完善 `WORK-PACKAGE-TEMPLATE.md`。
5. 编写 `STAGE-GATES.md`：阶段与门禁。
6. 编写 `ROADMAP.md`：S0-S6 路线图。
7. 编写 `EVIDENCE-FORMAT.md`：证据规范。

## 范围外

- 业务实现
- 架构决策
- CI 具体实现（WP-004）

## 实施步骤

1. 扩展 AGENTS.md。
2. 完善 STATE.json。
3. 编写执行协议。
4. 编写阶段门禁。
5. 编写工作包模板。
6. 编写路线图和证据格式。

## 验收标准

- `EXECUTION-PROTOCOL.md` 定义完整执行算法。
- `STAGE-GATES.md` 包含 S0-S6 及 G0-G6。
- `WORK-PACKAGE-TEMPLATE.md` 包含所有必要字段。
- `STATE.json` 合法且状态一致。

## 必须执行的验证命令

```powershell
node tools/execution/validate.mjs
git diff --check
```

## 预期证据

`docs/execution/evidence/WP-002-execution-protocol.md`

## 回滚方法

- 文档回退可通过 `git revert` 实现。

## 停止条件

- 与 WP-001 文档冲突。
- 工作包范围扩大到需要业务决策。

## 架构影响

无直接架构影响，但决定工作包推进方式。

## 数据库影响

无。

## 安全影响

低。不处理凭据。

## 完成报告路径

`docs/execution/evidence/WP-002-execution-protocol.md`
