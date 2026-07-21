---
status: active
---

# 数据架构

## 原则

1. 旧 SQL 仅为 legacy reference。
2. 新数据库使用 Flyway 或经 ADR 批准的等价方案。
3. 不使用单个巨型 `CREATE TABLE IF NOT EXISTS` 文件作为长期演进方式。
4. 每张表必须声明唯一 owner 业务包。
5. 只有 owner 业务包可以通过自身 Mapper 直接访问该表。
6. 同一业务包内部可以使用外键。
7. 跨业务包外键默认禁止。
8. 跨业务包数据通过稳定 ID、已登记 Service 接口、事件或读模型关联。
9. 状态字段必须使用领域语义，禁止全系统复用一个模糊 status。
10. 逻辑删除不是全局强制规则。
11. 每类数据必须定义保留、归档和删除策略。
12. 金额、日期、时区、审计字段、并发版本和唯一约束必须统一设计。
13. 新迁移脚本只在架构门禁通过后生成。
14. 本次任务不得创建新业务表。

## 旧 SQL 参考

原文件 `docs/建库/oa_platform_full.sql` 已标记为 `legacy-reference`，不得直接用于新系统建库。

## 迁移策略

- 使用 Flyway 版本化迁移。
- 每个迁移脚本只负责一个明确的结构变更。
- 迁移脚本命名：`V{版本号}__{描述}.sql`。
- 迁移脚本按版本顺序执行，不可回退。
- 新迁移脚本在 G1 门禁通过后开始生成。

## 数据保留与归档

- 流程实例数据：保留 5 年后归档。
- 审计日志：保留 3 年。
- 文件：按业务域定义保留策略。
- 删除操作必须记录审计日志。

## 参见

- `docs/architecture/BACKEND-ARCHITECTURE.md`
- `docs/architecture/DOMAIN-MAP.md`
- `docs/legacy/LEGACY-REFERENCE-POLICY.md`
