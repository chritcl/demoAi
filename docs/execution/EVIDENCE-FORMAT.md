---
status: active
---

# 证据格式（EVIDENCE-FORMAT）

本文件定义工作包完成证据的标准格式。

## 证据位置

每个工作包完成后必须生成证据文件：

```text
docs/execution/evidence/WP-XXX-{短标题}.md
```

例如：

```text
docs/execution/evidence/WP-002-execution-protocol.md
```

## 证据必须包含的章节

### 1. 工作包信息

- 工作包 ID
- 标题
- 完成时间
- 完成人（Codex / 用户）

### 2. 完成内容

- 实际修改的文件列表
- 实际创建的文件列表
- 实际删除的文件列表

### 3. 验证命令

- 执行的具体命令
- 命令输出摘要

### 4. 验证结果

- 通过 / 未通过
- 未通过时的阻塞原因

### 5. Git 提交

- 提交 SHA
- 提交信息

### 6. 已知风险

- 已发现但尚未处理的风险
- 建议的后续处理

### 7. 下一步

- 下一步唯一恢复入口或继续点

## 示例

```markdown
---
status: active
---

# WP-002 完成证据

## 工作包信息

- ID：WP-002
- 标题：Codex 执行协议
- 完成时间：2026-07-21
- 完成人：Codex

## 完成内容

- 创建 `docs/execution/EXECUTION-PROTOCOL.md`
- 创建 `docs/execution/STAGE-GATES.md`
- 创建 `docs/execution/WORK-PACKAGE-TEMPLATE.md`
- 创建 `docs/execution/ROADMAP.md`
- 创建 `docs/execution/EVIDENCE-FORMAT.md`
- 扩展 `AGENTS.md`
- 更新 `README.md`

## 验证命令

```powershell
node tools/execution/validate.mjs
git diff --check
```

## 验证结果

- 通过

## Git 提交

- SHA：`abc1234`
- 信息：`docs(WP-002): add Codex execution protocol`

## 已知风险

- 无

## 下一步

- 继续执行 WP-003：架构边界与 ADR 待办
```

## 证据与 STATE.json 的关系

- `STATE.json` 中 `evidence` 字段指向证据文件路径。
- 证据文件必须存在，工作包才能标记为 `done`。
- 提交完成后，必须再次运行验证器并确认工作区干净。