---
status: active
---

# WP-001：文档真源重建

## 标识

- ID：WP-001
- 标题：Rebuild documentation source of truth
- 所属 Stage：S0
- 状态：draft
- 优先级：1
- 风险等级：high
- 依赖工作包：[WP-000]
- 阻塞工作包：[WP-002, WP-003]

## 范围

### 允许修改路径

- `README.md`
- `docs/README.md`
- `docs/product/GOAL.md`
- `docs/product/SCOPE.md`
- `docs/product/TRACEABILITY.md`
- `docs/legacy/LEGACY-REFERENCE-POLICY.md`
- `docs/legacy/README.md`
- `docs/quality/README.md`
- `docs/operations/README.md`
- `docs/建库/oa_platform_full.sql`（仅添加头部 legacy-reference 警告）
- `docs/oa/*.md`（状态标记）
- `docs/技术栈约束/*.md`（状态标记）
- `docs/系统导图.md`（状态标记）

### 禁止修改路径

- `old/` 内业务代码
- 新后端/PC/移动代码
- 架构决策 ADR（属于 WP-003）
- 验证器（属于 WP-004）

## 目标

建立统一文档真源，消除 README 与 docs 之间的路径、技术栈、状态冲突，重置旧状态声明。

## 背景

README.md 仍使用旧工程路径、旧启动命令、旧技术栈，并声明旧功能已实现；docs 中部分文档已更新为 uni-app，但缺少统一入口和状态标记。

## 输入

- WP-000 审计报告
- 当前 `docs/` 全部文件
- 当前 `README.md`

## 范围内

1. 重写根 `README.md`：仓库简介、文档导航、如何恢复 Codex 执行、当前状态、禁止项。
2. 重写 `docs/README.md`：完整文档导航，标记旧文档状态。
3. 创建 `docs/product/GOAL.md`：总 Goal、成功标准、范围、不在范围、外部依赖、已知风险、完成定义。
4. 创建 `docs/product/SCOPE.md`：当前范围、暂不实施。
5. 创建 `docs/product/TRACEABILITY.md`：需求编号、业务域、工作包、验收标准。
6. 创建 `docs/legacy/LEGACY-REFERENCE-POLICY.md`：遗留代码允许/禁止行为。
7. 更新 `old/README.md`：声明遗留参考边界。
8. 创建 `docs/quality/README.md`：质量文档边界。
9. 创建 `docs/operations/README.md`：运维文档边界。

## 范围外

- 具体框架小版本决定
- 流程引擎选型
- 架构详细设计
- 新代码实现

## 实施步骤

1. 重写根 README。
2. 重写 docs README。
3. 创建 product 文档。
4. 创建 legacy policy。
5. 创建 quality 和 operations README。
6. 标记旧文档状态。
7. 验证链接。

## 验收标准

- 根 README 不再声称旧项目是当前工程。
- 根 README 不输出旧启动命令。
- docs/README.md 包含完整导航。
- product/GOAL.md 包含所有必要章节。
- 所有旧文档标记为 legacy-reference 或 needs-review。
- 不存在 active 文档声明 Vant 为当前移动技术栈。

## 必须执行的验证命令

```powershell
node tools/execution/validate.mjs
git diff --check
```

## 预期证据

`docs/execution/evidence/WP-001-documentation-source-of-truth.md`

## 回滚方法

- 文档回退可通过 `git revert` 实现。

## 停止条件

- 发现同级文档互相冲突。
- 需要决定技术版本。
- 需要决定流程引擎。

## 架构影响

确定文档权威顺序，为后续架构约束提供上下文。

## 数据库影响

无。

## 安全影响

中。README 不再输出默认账号密码。

## 完成报告路径

`docs/execution/evidence/WP-001-documentation-source-of-truth.md`
