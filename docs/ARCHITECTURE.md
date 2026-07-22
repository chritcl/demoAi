# 技术架构

## 后端

采用一个 Maven 工程、一个 Spring Boot 应用、一个部署单元。

顶层按业务域分包：

```text
com.oa.platform
├── common
├── config
├── s0_xtsz
├── s1_portal
├── s2_document
├── s3_office
├── s4_workflow
├── s5_contacts
├── s6_file
├── s7_message
└── s8_audit
```

`s0_`、`s1_` 等编号是稳定业务域编码，不代表开发顺序或依赖顺序。业务域编码一经批准不得因新增模块而整体重新编号。

每个业务包内部分层：

```text
controller
service
service.impl
mapper
entity
dto
vo
```

### 包内分层职责

#### controller

- 负责 HTTP 参数接收和参数校验。
- 调用本业务包 Service。
- 将 Service 返回值包装为统一 `Result<T>`。
- 禁止编写业务逻辑。
- 禁止直接调用 Mapper。
- 禁止使用 Entity 作为请求或响应。

#### service

- Service 接口默认仅供所属业务包内部使用。
- 跨业务包公开 Service 接口必须在对应工作包中声明。
- 声明必须包含调用方、输入、输出、错误语义和数据所有权边界。
- 禁止形成未声明的隐式跨包依赖。
- Service 实现放入 `service.impl`。
- 负责业务规则、事务控制和跨资源编排。
- Service 不返回 `Result<T>`，返回业务结果、DTO、VO、分页结果或基本类型。
- 业务失败抛出 `BusinessException`。

#### mapper

- 只负责本业务包拥有的数据表。
- 只能由本业务包 Service 实现调用。
- 禁止被 Controller 或其他业务包调用。

#### entity

- 只表示本业务包拥有的持久化数据。
- 禁止作为 Controller 请求或响应对象。
- 禁止作为跨业务包 Service 参数或返回值。

### 统一响应规则

所有 JSON 业务 API 统一返回 `Result<T>`。Service 接口禁止返回 `Result<T>`。业务失败抛出 `BusinessException`，由 `GlobalExceptionHandler` 统一转换为失败响应。

二进制文件下载、流式响应、SSE 和 WebSocket 握手属于协议级例外，不包装为 `Result<T>`。

### 跨业务包调用规则

允许：业务包 A 的 Service 实现 → 业务包 B 的 Service 接口。

禁止：

- 业务包 A → 业务包 B 的 Mapper
- 业务包 A → 业务包 B 的 Entity
- 业务包 A → 业务包 B 的 service.impl

业务包依赖必须保持单向，禁止循环依赖。使用 ArchUnit 验证边界。

### 数据所有权

- 每张表必须声明唯一 owner 业务包。
- 只有 owner 业务包可以通过自身 Mapper 直接对该表执行增删改。
- 其他业务包必须通过 owner 业务包的 Service 接口访问数据。

### 事务边界

- 单业务包写操作由该业务包 Service 实现控制事务。
- 多业务包同步编排由最上层编排 Service 控制事务。
- 事务失败必须通过异常触发回滚。
- 禁止使用 `Result.fail()` 表示 Service 事务失败。

### common 边界

`common` 只允许包含真正跨业务通用且无业务语义的能力，例如 `Result<T>`、分页对象、全局异常、通用校验注解、Web 基础配置、安全上下文接口。禁止把具体业务 Service、Entity、状态码放入 common。

## 数据库

- 新数据库重新设计，不迁移旧数据。
- 旧 SQL 只作参考。
- 每张表只能有一个 owner 业务包。
- 只有 owner 业务包可以直接通过 Mapper 修改表。
- 其他业务包通过 Service 接口访问。
- 使用版本化数据库迁移。
- 不使用单个巨型 SQL 作为长期演进方式。
- 具体迁移工具在 WP-01 中确定，默认优先考虑 Flyway。

## PC 端

Vue3 + TypeScript + Vite + Element Plus + Pinia。

## 移动端

uni-app + Vue3，当前只交付 H5。

Android、iOS、鸿蒙、水印、禁止截屏、手势和指纹等三期能力不进入当前实现范围。

## 包管理

- 根级 pnpm workspace。
- 根级唯一 pnpm-lock.yaml。
- 禁止创建 package-lock.json 和 yarn.lock。
