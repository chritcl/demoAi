---
status: active
---

# WP-000：仓库审计与凭据风险处理

## 标识

- ID：WP-000
- 标题：Repository audit and credential remediation
- 所属 Stage：S0
- 状态：in_progress
- 优先级：0
- 风险等级：critical
- 依赖工作包：[]
- 阻塞工作包：[WP-001]

## 范围

### 允许修改路径

- `docs/execution/`（最小 Bootstrap 目录、STATE.json、工作包规范）
- `AGENTS.md`
- `.gitignore`
- `old/README.md`
- `old/` 内已跟踪文本文件（仅限凭据脱敏）
- 已提交的 `old/**/target/`、`old/**/*.class`、`old/**/*.jar` 等生成产物删除

### 禁止修改路径

- 新后端、PC、移动端业务代码
- 新数据库表
- `old/` 内业务逻辑、SQL 结构、页面行为
- 除凭据脱敏外任何对 `old/` 的实质性重写

## 目标

建立仓库执行底座，完成审计，消除凭据风险，清理构建产物，建立可跨会话恢复的状态。

## 背景

仓库存在遗留代码、已提交构建产物、硬编码凭据、README 旧路径与旧状态声明，需要先行治理。

## 输入

- 仓库当前状态
- `demoAi-Codex执行体系总指令.md`

## 范围内

1. 创建执行体系最小目录结构。
2. 创建最小 `AGENTS.md`。
3. 创建初始 `STATE.json`。
4. 创建 WP-000 至 WP-004 最小工作包规范。
5. 记录仓库审计结果。
6. 对 `old/` 内数据库密码、Redis 密码、JWT 密钥、默认管理员密码、旧服务地址进行脱敏。
7. 删除已提交的 `old/**/target/`、`.class`、`.jar` 等生成产物。
8. 修正 `.gitignore`：不再忽略根 `pnpm-lock.yaml`，增加 `old/**/target/` 等。

## 范围外

- 重写 Git 历史
- 访问外部系统
- 验证旧凭据是否仍可用
- 新架构设计
- 新功能实现

## 实施步骤

1. 只读检查仓库状态。
2. 创建执行体系最小目录。
3. 创建 Bootstrap 文件。
4. 编写审计报告。
5. 对 `old/` 内文本文件执行凭据脱敏。
6. 删除已提交生成产物。
7. 修正 `.gitignore`。
8. 运行验证器（WP-004 完成后）。

## 验收标准

- `docs/execution/` 目录存在。
- `STATE.json` 合法且包含 WP-000 至 WP-004 及 G0 至 G6。
- `WP-000` 处于 `done` 时，`WP-001` 被提升为 `ready`。
- 审计报告记录所有发现。
- `old/` 内真实凭据已替换为环境变量占位符或明显无效值。
- 已提交生成产物已删除。
- `.gitignore` 不忽略根 `pnpm-lock.yaml`。

## 必须执行的验证命令

```powershell
node tools/execution/validate.mjs
git diff --check
git status --short
```

## 预期证据

`docs/execution/evidence/WP-000-repository-audit.md`

## 回滚方法

- 凭据脱敏：通过 Git 历史恢复，但 Git 历史本身保留原值，需要用户后续决定重写历史。
- 生成产物删除：可通过重新编译恢复。
- 分支回退：在 `codex/execution-system` 上执行 `git revert` 或 `git reset --hard`（仅允许由用户显式执行）。

## 停止条件

- 工作区存在无法归属的用户修改。
- `old/` 之外发现真实凭据。
- 凭据用途无法判断。
- 需要访问外部系统。
- 需要重写 Git 历史。

## 架构影响

无，只建立治理基础。

## 数据库影响

无，不修改新数据库。

## 安全影响

高。脱敏后的值仍可能残留在 Git 历史，需用户确认是否重写历史。

## 完成报告路径

`docs/execution/evidence/WP-000-repository-audit.md`
