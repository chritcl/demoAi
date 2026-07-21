---
status: active
---

# WP-000 仓库审计报告

## 审计基本信息

- 审计工作包：WP-000
- 仓库：`https://github.com/chritcl/demoAi.git`
- 审计分支：main（审计时）→ codex/execution-system（执行分支）
- 审计日期：2026-07-21
- 审计人：Codex 执行智能体
- 工作区状态：存在 1 个未跟踪文件 `demoAi-Codex执行体系总指令.md`，属于任务输入，按首次 Bootstrap 例外处理

## 1. 分支与提交

- 当前分支：main
- 目标分支：codex/execution-system（已创建并切换）
- 最近 10 条提交（存在大量不规范提交信息）：
  - `1ddb65f 1`
  - `a56bc0f feat(platform): 初始化协同办公平台项目架构`
  - `9e7a595 addd`
  - `bcb51fe add`
  - `c42c1c0 add`
  - `4c5d6fe add`
  - `6970655 add`
  - `5ae3239 1`
  - `268679a add`
  - `1622402 /add`

## 2. 根目录结构

```
.
├── .git
├── .gitignore
├── demoAi-Codex执行体系总指令.md   # 未跟踪，任务输入
├── docs/
├── old/
└── README.md
```

## 3. docs/ 文件清单

- `docs/功能开发清单.xlsx`：原始需求清单，53 功能
- `docs/技术栈约束/01-后端技术栈约束.md`
- `docs/技术栈约束/02-PC端技术栈约束.md`
- `docs/技术栈约束/03-移动端技术栈约束.md`：已声明 uni-app + Vue3，与约束一致
- `docs/建库/oa_platform_full.sql`：旧建库 SQL，需标记为 legacy-reference
- `docs/系统导图.md`：旧系统导图，含旧包结构
- `docs/oa/00-流程总览.md` 至 `09-办公用品申请流程.md`：旧流程文档
- `docs/README.md`：旧文档导航，缺少状态标记

## 4. old/ 结构

- `old/oa-backend/`：旧后端工程，含 `pom.xml`、`src/`、已提交 `target/`
- `old/oa-frontend/`：旧 PC 端工程，含 `package-lock.json`、`.env.*`
- `old/oa-mobile/`：旧移动端工程，含 `package-lock.json`、`.env.*`
- `old/oa.ps1`、`old/start.bat`、`old/stop.bat`、`old/restart.bat`：旧启动脚本
- `old/OA报价20270714.xlsx`：原始报价清单

## 5. 文档冲突

- `README.md` 仍声称旧项目是当前工程，列出旧功能已实现、旧启动命令、旧路径。
- `README.md` 移动端技术栈写为 `Vue3 + Vant`，与约束 `uni-app + Vue3` 冲突。
- `README.md` 使用 `npm`，与约束 `pnpm` 冲突。
- `docs/系统导图.md` 使用旧包结构，未按新业务包分层单体编码。
- 旧流程文档缺少 `status` 元数据，无法通过一致性验证。

## 6. 已跟踪生成产物

以下生成产物已提交到 Git，需删除：

- `old/oa-backend/target/**`（含 `.class`、`.yml`、`.sql`、编译状态文件）
- 总计约 150+ 个 target/ 下文件

## 7. 凭据发现（不输出真实值）

| 文件路径 | 行号 | 凭据类型 | 风险级别 | 备注 |
|---|---|---|---|---|
| `README.md` | 78, 92-94 | 默认账号密码 | 高 | 根文档公开输出 |
| `old/oa-backend/src/main/resources/application.yml` | 35 | 数据库地址 | 高 | 真实公网 IP |
| `old/oa-backend/src/main/resources/application.yml` | 37 | 数据库密码 | 高 | 生产/测试密码 |
| `old/oa-backend/src/main/resources/application.yml` | 54 | Redis 密码 | 高 | 生产/测试密码 |
| `old/oa-backend/src/main/resources/application.yml` | 75 | JWT 密钥 | 高 | 硬编码 secret |
| `old/oa-backend/src/main/resources/application.yml` | 92 | 默认管理员密码 | 高 | 硬编码 |
| `old/oa-backend/src/main/resources/data.sql` | 4, 16 | 默认账号密码注释 | 中 | 历史测试数据 |
| `old/oa-backend/target/classes/application.yml` | 同 source | 同上 | 高 | 生成产物副本 |
| `old/oa-backend/src/main/java/com/oa/platform/security/JwtProperties.java` | 20 | JWT 默认密钥 | 高 | 代码默认值 |
| `old/oa-backend/src/main/java/com/oa/platform/system/service/SysUserService.java` | 85 | 默认用户密码 | 中 | 重置密码默认值 |
| `old/oa-backend/src/main/java/com/oa/platform/OaPlatformApplication.java` | 36 | 默认账号密码 | 中 | 启动日志 |
| `old/oa-frontend/src/views/login/index.vue` | 32, 54 | 默认账号密码 | 中 | 登录页提示 |
| `old/oa-frontend/src/views/system/user/index.vue` | 86 | 默认用户密码 | 中 | 新增用户默认值 |
| `old/oa-mobile/src/views/login.vue` | 20, 36 | 默认账号密码 | 中 | 登录页提示 |
| `old/oa.ps1` | 60 | 默认账号密码 | 中 | 启动脚本提示 |

## 8. .gitignore 问题

- 当前 `.gitignore` 包含 `pnpm-lock.yaml`，违反根级锁文件必须受控的约束。
- 当前 `.gitignore` 包含 `package-lock.json`，但缺少 `old/**/package-lock.json`。
- 当前 `.gitignore` 对 `old/**/target/`、`.class`、`.jar` 等无覆盖，导致生成产物被提交。

## 9. 缺失文件

- 根目录无 `AGENTS.md`（Bootstrap 创建）。
- 根目录无 `package.json`（后续工作包处理）。
- 无 `docs/execution/`、无 `docs/product/`、无 `docs/architecture/`、无 `docs/quality/`、无 `docs/operations/`、无 `docs/legacy/`。
- 无 `tools/execution/validate.mjs`（WP-004 创建）。
- 无 `.github/workflows/`（WP-004 创建）。

## 10. 处理建议

1. 创建 `codex/execution-system` 分支并切换。
2. 创建最小 Bootstrap 目录与文件。
3. 对 `old/` 内已跟踪文本文件执行凭据脱敏，替换为环境变量占位符。
4. 删除 `old/oa-backend/target/` 等已提交生成产物。
5. 修正 `.gitignore`，移除 `pnpm-lock.yaml` 根级忽略，增加 `old/**` 生成产物规则。
6. 创建 `old/README.md` 与 `docs/legacy/LEGACY-REFERENCE-POLICY.md`，声明遗留边界。
7. 对 `docs/建库/oa_platform_full.sql` 添加 legacy-reference 头部警告。

## 11. 风险声明

- 所有脱敏操作仅影响工作树和后续提交，Git 历史仍保留原凭据。是否重写 Git 历史需要用户人工决定，Codex 不自行处理。
- 凭据用途基于上下文推断，已确定为遗留测试/示例数据，但用户仍需确认对应环境是否已撤销或轮换。

## 证据清单

- 本报告：[`docs/execution/evidence/WP-000-repository-audit.md`](./WP-000-repository-audit.md)
- 工作包规范：[`docs/execution/work-packages/WP-000-repository-audit.md`](../work-packages/WP-000-repository-audit.md)
