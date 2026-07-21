---
status: active
---

# WP-004：执行体系自动验证

## 标识

- ID：WP-004
- 标题：Execution system auto-validation
- 所属 Stage：S0
- 状态：draft
- 优先级：4
- 风险等级：medium
- 依赖工作包：[WP-002, WP-003]
- 阻塞工作包：[]

## 范围

### 允许修改路径

- `tools/execution/validate.mjs`
- `.github/workflows/execution-contract.yml`

### 禁止修改路径

- 业务代码
- 架构文档
- 产品文档

## 目标

建立无第三方依赖验证器和 CI 门禁，确保执行协议可自动验证。

## 背景

需要自动检查状态机、链接、凭据、提交追踪等。

## 输入

- WP-002 执行协议
- WP-003 架构文档

## 范围内

1. 创建 `tools/execution/validate.mjs`：使用 Node.js 内置模块。
2. 验证 STATE.json 合法性、工作包唯一性、依赖存在性、循环依赖、done 依赖、in_progress 数量、spec 文件存在性、evidence 文件存在性、gate 一致性、active 文档链接、README 旧启动说明、Vant 声明、old/ 引用、凭据扫描、.gitignore 对 pnpm-lock.yaml 的忽略、提交追踪。
3. 创建 `.github/workflows/execution-contract.yml`：PR/push 触发，安装 Node LTS，运行验证器，验证 Markdown 和 JSON。

## 范围外

- 后端、PC、移动端构建命令
- 测试命令
- 部署命令

## 实施步骤

1. 编写验证器。
2. 测试验证器。
3. 创建 CI workflow。
4. 运行验证。

## 验收标准

- `node tools/execution/validate.mjs` 在 WP-000 至 WP-004 完成后通过。
- CI workflow 可解析且只执行允许步骤。
- 验证器不依赖第三方 npm 包。

## 必须执行的验证命令

```powershell
node tools/execution/validate.mjs
git diff --check
```

## 预期证据

`docs/execution/evidence/WP-004-execution-validation.md`

## 回滚方法

- 删除或修改 `tools/execution/validate.mjs` 和 workflow 文件。

## 停止条件

- 自动验证无法执行。
- 需要引入第三方依赖。

## 架构影响

无。

## 数据库影响

无。

## 安全影响

高。凭据扫描器发现未脱敏凭据时阻止进入下一阶段。

## 完成报告路径

`docs/execution/evidence/WP-004-execution-validation.md`
