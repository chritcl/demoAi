# AGENTS.md — demoAi 仓库执行权威

## 文档权威顺序

1. 用户当前明确指令。
2. 根目录 `AGENTS.md`（本文件）。
3. `docs/GOAL.md`。
4. `docs/ARCHITECTURE.md`。
5. `docs/EXECUTION.md`。
6. `docs/STATE.json`（动态状态唯一真源）。
7. 当前工作包规范（`docs/work-packages/WP-XX.md`）。
8. `docs/功能开发清单.xlsx`（业务范围唯一真源）。
9. `docs/reference/flows/`（流程参考）。
10. `old/`（遗留参考）。

## 新会话入口

- 首次执行：读取 `docs/STATE.json`，按 `docs/EXECUTION.md` 恢复或选择工作包。
- 所有写入必须对应到处于 `in_progress` 的工作包。
- 状态和阻塞原因必须保存在 `docs/STATE.json`。

## 禁止项

- 禁止把 `old/` 中的代码直接复制为新实现。
- 禁止新代码依赖 `old/`。
- 禁止在文档中把旧功能声明为新系统已完成。
- 禁止在 `old/` 之外硬编码密码、密钥、数据库地址、Redis 地址和默认账号。
- 禁止在提交信息中使用 `1`、`add`、`done`。
- 禁止推送远程。
- 禁止修改 Git 历史。
- 禁止丢弃用户未提交修改。
- 禁止使用 `npm` 或 `yarn` 执行依赖安装、删除、更新及项目脚本。
- 禁止在依赖工作包完成前将后续工作包标记为 `in_progress`。
- 禁止在自动验证未通过时进入下一阶段。
- 禁止在工作包范围扩大时继续执行而不上报。

## 编码约束

- 所有代码注释使用中文。
- 所有文件写入使用 UTF-8 without BOM。
- 项目使用 `pnpm` 作为包管理工具。
- 项目依赖锁定文件统一使用 `pnpm-lock.yaml`。
- 禁止创建 `package-lock.json`、`yarn.lock`。

## 提交规范

- 提交信息必须包含工作包 ID，例如 `(WP-02)`。
- 推荐格式：`<类型>(WP-XX): <描述>`。
- 类型可以是 `chore`、`docs`、`ci`、`feat`、`refactor` 等。
- 每个工作包独立提交。

## 自动执行与停止条件

- 执行算法和停止条件定义在 `docs/EXECUTION.md`。
- 当前阶段、活动门禁和门禁状态以 `docs/STATE.json` 为唯一真源。
- 自动门禁通过后，混合门禁仍需用户明确批准。
