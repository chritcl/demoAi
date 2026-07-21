# 协同办公平台 · 交付文档

> 本目录依据《功能开发清单.xlsx》生成，包含：功能开发清单（含难易度）、完整建库 SQL、各业务流程设计（统一使用 `docs/oa/` 版本）、系统导图、各端技术栈约束。

## 目录索引

| 交付物 | 路径 | 说明 |
|---|---|---|
| 📊 功能开发清单 | [功能开发清单.xlsx](功能开发清单.xlsx) | 53 个功能，标注难易度/工时/优先级/阶段/涉及表/所属端 + 难易度统计表 + 图例 |
| 🗄️ 完整建库 SQL | [建库/oa_platform_full.sql](建库/oa_platform_full.sql) | 62 张表（25 基础 + 37 扩展），幂等可执行，覆盖全功能；2026-07 已按清单涉及表 + 流程文档复核修订 |
| 🔁 流程设计 | [oa/](oa/) | 10 份流程文档（含总览），覆盖功能开发清单中 9 个含流程功能 + 1 个公文交换扩展 |
| 🧭 系统导图 | [系统导图.md](系统导图.md) | Mermaid 导图 + 架构图 + 功能树 + 涉及表清单 + 端矩阵 + 阶段划分 |
| ⚙️ 技术栈约束 | [技术栈约束/](技术栈约束/) | 后端 / PC端 / 移动端 三端技术栈约束 |

## OA 流程设计清单（统一版本）

> 共 9 个含流程功能（见功能开发清单「是否含流程」列），对应 9 份流程设计文档 + 1 份总览。

| 序号 | 流程 | 文档 | 难度 | 优先级 | 阶段 | 涉及表 |
|---|---|---|---|---|---|---|
| 总览 | 流程总览 | [oa/00-流程总览.md](oa/00-流程总览.md) | — | — | — | flow_* |
| 01 | 信息发布审核 | [oa/01-信息发布审核流程.md](oa/01-信息发布审核流程.md) | 中等 | P0 | 一期 | portal_article,portal_column,portal_article_file,portal_read_record |
| 02 | 发文审批 | [oa/02-发文审批流程.md](oa/02-发文审批流程.md) | 复杂 | P0 | 一期 | doc_official,doc_send_template,doc_opinion,flow_* |
| 03 | 收文办理 | [oa/03-收文办理流程.md](oa/03-收文办理流程.md) | 复杂 | P0 | 一期 | doc_official,doc_receive_register,flow_* |
| 04 | 请休假审批 | [oa/04-请休假审批流程.md](oa/04-请休假审批流程.md) | 中等 | P0 | 一期 | office_leave,sys_dict_data,flow_* |
| 05 | 用车审批 | [oa/05-用车审批流程.md](oa/05-用车审批流程.md) | 中等 | P0 | 一期 | office_vehicle,office_car,office_car_record,flow_* |
| 06 | 用印审批 | [oa/06-用印审批流程.md](oa/06-用印审批流程.md) | 中等 | P0 | 一期 | office_seal,office_seal_registry,office_seal_record,flow_* |
| 07 | 出差审批 | [oa/07-出差审批流程.md](oa/07-出差审批流程.md) | 中等 | P0 | 一期 | office_trip,flow_* |
| 08 | 资产领用 | [oa/08-资产领用流程.md](oa/08-资产领用流程.md) | 复杂 | P1 | 二期 | office_asset,office_asset_apply,office_asset_record,flow_* |
| 09 | 办公用品申请 | [oa/09-办公用品申请流程.md](oa/09-办公用品申请流程.md) | 中等 | P1 | 二期 | office_supply,office_supply_stock,office_supply_apply,flow_* |

> 公文交换（⑤，P2/三期）的流程扩展见 [oa/02-发文审批流程.md § 九](oa/02-发文审批流程.md)，不单独建流程文档。

## 技术栈约束清单

| 端 | 文档 | 技术栈 |
|---|---|---|
| 后端 | [技术栈约束/01-后端技术栈约束.md](技术栈约束/01-后端技术栈约束.md) | Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + MySQL 8 + Redis + Security/JWT |
| PC 端 | [技术栈约束/02-PC端技术栈约束.md](技术栈约束/02-PC端技术栈约束.md) | Vue3 + Vite5 + Element Plus + Pinia + Vue Router |
| 移动端 | [技术栈约束/03-移动端技术栈约束.md](技术栈约束/03-移动端技术栈约束.md) | uni-app（Vue3 + Vite）+ wot-design-uni + Pinia，一套代码编译 H5 / Android / iOS / 鸿蒙 |

## 快速概览

- **功能规模**：10 大模块、53 个二级功能，预估 505 人日
- **难易度分布**：简单 15（28.3%）/ 中等 24（45.3%）/ 复杂 14（26.4%）
- **交付阶段**：一期 31 功能（235 人日）/ 二期 13 功能（129 人日）/ 三期 9 功能（141 人日）
- **业务流程**：10 份流程文档（含总览），统一基于轻量流程引擎，通过事件解耦回写
- **含流程功能**：9 个（信息发布/发文/收文/请休假/用车/用印/出差/资产/办公用品）+ 公文交换扩展
