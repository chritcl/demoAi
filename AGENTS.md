# AGENTS.md — demoAi 仓库执行约束

## 文档权威顺序

1. 用户当前明确指令。
2. 根目录 AGENTS.md。
3. docs/GOAL.md。
4. docs/ARCHITECTURE.md。
5. docs/EXECUTION.md。
6. docs/STATE.json（动态状态唯一真源）。
7. 当前工作包规范。
8. docs/功能开发清单.xlsx（业务范围唯一真源）。
9. docs/reference/flows/。
10. old/（遗留参考）。

## Codex 新会话入口

1. 依次读取 AGENTS.md、docs/GOAL.md、docs/ARCHITECTURE.md、docs/EXECUTION.md 和 docs/STATE.json。
2. 如果 STATE.json 表示等待人工阶段验收，停止并等待用户输入“批准并继续”。
3. 否则按 docs/EXECUTION.md 恢复当前工作包或选择下一个可执行工作包。
4. 所有实现、验证、状态更新和提交均只针对当前工作包；不得跳过依赖或扩大范围。

## 仓库级硬约束

- 禁止把 old/ 中的代码直接复制为新实现，禁止新代码依赖 old/。
- 禁止在 old/ 之外硬编码密码、密钥、数据库地址、Redis 地址和默认账号。
- 只使用本地验证命令作为工作包完成和阶段推进依据；禁止依赖 GitHub Actions、远程 CI 或远程流水线状态。
- 不创建根级 package.json、pnpm-workspace.yaml 或 pnpm-lock.yaml。PC 与 mobile 分别维护依赖和锁文件。
- 项目统一使用 pnpm。禁止使用 npm 或 yarn 安装、删除、更新依赖或执行项目脚本。
- 所有代码注释使用中文；所有文本文件使用 UTF-8 without BOM；中文不得使用 Unicode 转义。
- 禁止跨工程使用相对路径直接引用源码；共享能力必须通过明确的公共包或复制边界决策处理。
- 禁止推送远程、修改 Git 历史或丢弃用户未提交修改。
- 依赖工作包完成前，不得开始后续工作包；本地验证未通过前，不得推进阶段。
- 发现用户未提交修改且无法判断归属时停止并报告；普通代码、测试、格式和编译问题必须先在当前工作包范围内修复。
- 不执行可能破坏数据、Git 历史或现有实现的高风险操作；范围明显扩大时停止并报告。

## 提交规范

- 每个工作包创建独立 Git 提交。
- 提交信息必须包含工作包 ID，推荐格式：类型(WP-XX): 描述。
- 类型可使用 chore、docs、feat、fix、refactor 等；禁止使用 1、add、done 作为提交信息。
