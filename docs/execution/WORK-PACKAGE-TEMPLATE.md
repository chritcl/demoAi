---
status: active
---

# 工作包模板（WORK-PACKAGE-TEMPLATE）

所有工作包规范必须使用本模板。

## 标识

- ID：WP-XXX
- 标题：简短描述
- 所属 Stage：S0
- 状态：draft / ready / in_progress / review / done / blocked
- 优先级：数字，越小优先级越高
- 风险等级：critical / high / medium / low
- 依赖工作包：[WP-YYY, ...]
- 阻塞工作包：[WP-ZZZ, ...]

## 范围

### 允许修改路径

- 列出本工作包允许修改的文件和目录。

### 禁止修改路径

- 列出本工作包禁止修改的文件和目录。

## 目标

本工作包要达成什么目标。

## 背景

为什么要做这个工作包。

## 输入

- 依赖工作包输出。
- 其他输入。

## 范围内

1. 具体任务 1。
2. 具体任务 2。

## 范围外

- 不包含的内容。

## 实施步骤

1. 步骤 1。
2. 步骤 2。

## 验收标准

- 可验证的完成标准。

## 必须执行的验证命令

```powershell
node tools/execution/validate.mjs
git diff --check
```

## 预期证据

`docs/execution/evidence/WP-XXX-*.md`

## 回滚方法

如何回滚本工作包的修改。

## 停止条件

出现以下情况时必须停止：

- 条件 1。
- 条件 2。

## 架构影响

无 / 低 / 中 / 高。

## 数据库影响

无 / 有。

## 安全影响

无 / 低 / 中 / 高。

## 完成报告路径

`docs/execution/evidence/WP-XXX-*.md`