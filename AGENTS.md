# AGENTS.md — demoAi 仓库执行权威

## 文档权威顺序

1. `AGENTS.md`（本文件）
2. `docs/product/GOAL.md`
3. `docs/execution/STATE.json`
4. `docs/execution/EXECUTION-PROTOCOL.md`
5. `docs/execution/STAGE-GATES.md`
6. `docs/execution/WORK-PACKAGE-TEMPLATE.md`
7. 工作包规范：`docs/execution/work-packages/WP-XXX-*.md`
8. 证据：`docs/execution/evidence/`
9. 架构约束：`docs/architecture/`
10. 遗留参考：`docs/legacy/` 与 `old/`

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

## 当前工作包入口

- 首次执行：读取 `docs/execution/STATE.json`，按 `EXECUTION-PROTOCOL.md` 恢复或选择工作包。
- 所有写入必须对应到处于 `in_progress` 的工作包。
- 状态、阻塞原因和验证证据必须保存在仓库中。

## 编码约束

- 所有代码注释使用中文。
- 所有文件写入使用 UTF-8 without BOM。
- 项目使用 `pnpm` 作为包管理工具。
- 项目依赖锁定文件统一使用 `pnpm-lock.yaml`。
