---
status: active
---

# WP-003：架构边界与 ADR 待办

## 标识

- ID：WP-003
- 标题：Architecture boundary and ADR backlog
- 所属 Stage：S0
- 状态：draft
- 优先级：3
- 风险等级：high
- 依赖工作包：[WP-001, WP-002]
- 阻塞工作包：[WP-004]

## 范围

### 允许修改路径

- `docs/architecture/BACKEND-ARCHITECTURE.md`
- `docs/architecture/DATA-ARCHITECTURE.md`
- `docs/architecture/DOMAIN-MAP.md`
- `docs/architecture/adr/README.md`
- `docs/architecture/adr/ADR-001.md` 至 `ADR-010.md`

### 禁止修改路径

- 实际业务代码
- 实际数据库迁移脚本
- 流程引擎实现

## 目标

建立业务包分层单体后端架构基线、数据库重新设计规则、ADR 体系。

## 背景

架构需要明确边界才能进入 S1/S2。

## 输入

- WP-001 文档真源
- WP-002 执行协议
- 旧系统结构（仅参考）

## 范围内

1. 创建 `BACKEND-ARCHITECTURE.md`：包分层、响应规则、跨包调用规则、数据所有权、事务边界、common 边界、ArchUnit 验证计划。
2. 创建 `DATA-ARCHITECTURE.md`：Flyway、表 owner、外键规则、状态字段、迁移规则。
3. 创建 `DOMAIN-MAP.md`：业务域、业务包、数据 owner。
4. 创建 `adr/README.md`：ADR 状态定义。
5. 创建 ADR-001 至 ADR-010，状态均为 `proposed`。

## 范围外

- 把 ADR 写成 `accepted`
- 创建真实业务表
- 实现流程引擎

## 实施步骤

1. 编写后端架构。
2. 编写数据架构。
3. 编写领域地图。
4. 创建 ADR 目录。
5. 编写 10 个 proposed ADR。

## 验收标准

- 所有 ADR 状态为 `proposed`。
- 架构文档包含所有必要章节。
- 不创建业务 Controller/Service/Mapper/Entity。

## 必须执行的验证命令

```powershell
node tools/execution/validate.mjs
git diff --check
```

## 预期证据

`docs/execution/evidence/WP-003-architecture-boundary.md`

## 回滚方法

- 文档回退可通过 `git revert` 实现。

## 停止条件

- 需要决定流程引擎。
- 需要决定最终技术版本。
- 需要决定跨业务包数据库关系。

## 架构影响

高。奠定后端架构基线。

## 数据库影响

无，不创建新表。

## 安全影响

中。定义数据所有权和访问规则。

## 完成报告路径

`docs/execution/evidence/WP-003-architecture-boundary.md`
