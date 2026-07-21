---
status: active
---

# 后端架构

## 总体结构

后端采用一个 Maven 工程、一个 Spring Boot 应用、一个部署单元。

不拆分为多个业务 Maven 模块，不建设微服务，不按技术层在顶层统一放置全部 controller、service、mapper 和 entity。

顶层按照业务领域划分 Java 包，每个业务包内部按照传统分层组织。

## 候选根包

```text
com.oa.platform
```

## 候选业务包结构

```text
com.oa.platform
├── common
├── config
├── s0_xtsz
│   ├── controller
│   ├── service
│   │   └── impl
│   ├── mapper
│   ├── entity
│   ├── dto
│   ├── vo
│   ├── converter
│   ├── enums
│   └── event
├── s1_portal
├── s2_document
├── s3_office
├── s4_workflow
├── s5_contacts
├── s6_file
├── s7_message
└── s8_audit
```

最终业务包名称和编码必须通过领域地图确认。

`s0_`、`s1_` 等编号是稳定业务域编码，不代表开发顺序或依赖顺序。业务域编码一经批准不得因为新增模块而整体重新编号。

## 包内分层职责

### controller

- 负责 HTTP 参数接收。
- 负责参数校验。
- 调用本业务包 Service。
- 将 Service 返回值包装为统一 `Result<T>`；该规则适用于 JSON 业务接口。
- 禁止编写业务逻辑。
- 禁止直接调用 Mapper。
- 禁止使用 Entity 作为请求或响应。
- 禁止直接调用其他业务包 Mapper 或 Entity。

### service

- Service 接口默认只供所属业务包内部使用；只有在领域地图中登记为跨包公开契约、并使用统一公开契约标记的接口，才允许被其他业务包调用。
- Service 实现放入 `service.impl`。
- 负责业务规则、事务控制和跨资源编排。
- Service 不返回 `Result<T>`。
- Service 返回业务结果、DTO、VO、分页结果或基本类型。
- 业务失败抛出 `BusinessException`。
- 其他业务包只能依赖已登记的 Service 接口，禁止依赖未登记 Service、`service.impl`、Mapper 或 Entity。

### mapper

- 只负责本业务包拥有的数据表。
- 只能由本业务包 Service 实现调用。
- 禁止被 Controller 调用。
- 禁止被其他业务包调用。
- 禁止通过 Mapper 绕过目标业务包 Service 修改其他业务域数据。
- 复杂查询写 XML，并进行参数化。
- 禁止未经批准的跨业务域 SQL JOIN。

### entity

- 只表示本业务包拥有的持久化数据。
- 禁止作为 Controller 请求或响应对象。
- 禁止作为跨业务包 Service 参数或返回值。
- 禁止被其他业务包直接引用。
- 不得为同一张表在多个业务包重复创建 Entity。

### dto

- 用于请求参数、业务命令和 Service 调用参数。
- 跨业务包使用的 DTO 必须保持最小化和稳定。
- DTO 不得包含 Mapper、Entity 或框架上下文对象。

### vo

- 用于 Controller 返回值或只读业务摘要。
- 跨业务包返回信息时使用专用 SummaryVO，禁止返回完整 Entity。
- 不得暴露数据库内部字段和敏感信息。

### converter

- 负责 Entity、DTO、VO 之间的转换。
- 禁止在 Converter 中查询数据库或执行副作用。

## 统一响应规则

所有 JSON 业务 API 统一返回：

```java
Result<T>
```

例如：

```java
public Result<SysUserVO> detail(Long id)
```

Service 接口禁止返回：

```java
Result<T>
```

Service 正常返回业务对象，异常时抛出：

```java
BusinessException
```

由：

```java
GlobalExceptionHandler
```

统一转换为失败响应。

分页接口统一返回：

```java
Result<PageResult<T>>
```

不得同时存在多套 JSON 响应结构。二进制文件下载、流式响应、SSE 和 WebSocket 握手属于协议级例外，不包装为 `Result<T>`；此类例外必须在 API 契约中登记，普通 JSON 错误响应仍由统一异常体系处理。

## 跨业务包调用规则

允许：

```text
业务包 A 的 Service 实现 → 业务包 B 的 Service 接口
```

禁止：

```text
业务包 A → 业务包 B 的 Mapper
业务包 A → 业务包 B 的 Entity
业务包 A → 业务包 B 的 service.impl
业务包 A 的 Mapper XML → 直接修改业务包 B 的表
```

允许跨业务包调用的 Service 接口必须同时满足：

1. 在 `DOMAIN-MAP.md` 中登记。
2. 使用经 ADR 确认的公开契约标记，供 ArchUnit 自动识别。
3. 明确允许的调用方业务包。
4. 明确输入、输出和错误语义。
5. 不返回 Entity。
6. 不暴露目标业务包内部持久化结构。
7. 不得形成循环依赖。

跨业务包调用的参数和返回值只能使用：

- 基本类型。
- 业务 ID。
- DTO。
- VO。
- 明确的只读结果对象。
- 领域事件。

不得传递 Entity。

业务包依赖必须保持单向，禁止循环依赖。

如果出现：

```text
A → B → A
```

必须重新划分职责、增加上层编排服务或使用事件解耦，禁止通过延迟注入、ApplicationContext 动态取 Bean 等方式绕过循环依赖检查。

## 数据所有权

- 每张表必须声明唯一 owner 业务包。
- 只有 owner 业务包可以通过自身 Mapper 直接对该表执行增删改。
- 其他业务包必须通过 owner 业务包的 Service 接口访问数据。
- 跨业务域报表和搜索属于显式例外，必须只读、有单独查询对象、在架构文档中登记，不得利用该查询对象修改其他业务域数据，不得成为普通业务写操作的绕过通道。

## 事务边界

- 单业务包写操作由该业务包 Service 实现控制事务。
- 多业务包同步编排由最上层编排 Service 控制事务。
- 被调用 Service 不得吞掉异常。
- 事务失败必须通过异常触发回滚。
- 禁止使用 `Result.fail()` 表示 Service 事务失败。
- 事务内禁止执行远程调用、大文件 IO 和不受控循环写操作。
- 非关键消息、通知和审计动作优先在事务提交后通过事件执行。
- 核心数据一致性不得依赖异步事件最终补偿，除非有明确设计和恢复机制。

## common 边界

`common` 只允许包含真正跨业务通用且无业务语义的能力，例如：

- `Result<T>`。
- 分页对象。
- 全局异常。
- 通用校验注解。
- Web 基础配置。
- 安全上下文接口。
- 时间、ID 等基础设施抽象。

禁止把以下内容放入 common：

- 具体业务 Service。
- 具体业务 Entity。
- 具体业务状态码。
- 某个业务包专用工具。
- 为绕过循环依赖而抽出的杂物类。
- 所有模块都可以随意调用的万能 Mapper 或万能 Service。

新增 common 内容必须说明至少两个真实业务包的复用场景，否则保留在所属业务包内。

## 架构自动验证

在工程骨架阶段引入 ArchUnit。

必须自动验证：

1. Controller 不得依赖 Mapper。
2. Controller 不得依赖 Entity。
3. 其他业务包不得依赖目标业务包的 Mapper。
4. 其他业务包不得依赖目标业务包的 Entity。
5. 其他业务包不得依赖目标业务包的 `service.impl`。
6. Mapper 只能被所属业务包的 Service 实现访问。
7. common 不得依赖具体业务包。
8. 业务包之间不得存在循环依赖。
9. 跨业务包只允许依赖已登记且带公开契约标记的 Service 接口，以及该契约使用的 DTO、VO、Event 和 `common` 基础类型。
10. 包结构不符合约定时构建失败。

不得只把这些规则写入文档而不建立自动化测试。

## 本阶段限制

当前 S0 和 S1 只编写架构文档、领域地图、依赖规则和架构测试计划。

在 G1 架构门禁通过前：

- 不创建实际业务 Controller。
- 不创建实际业务 Service。
- 不创建实际 Mapper。
- 不创建实际 Entity。
- 不生成大量占位包和空类。
- 不根据旧代码直接决定全部业务包。
- 不把旧包结构视为新架构事实。

G1 必须批准：

- 业务包清单。
- 每个业务包职责。
- 每张表的 owner 业务包。
- 允许的业务包依赖方向。
- 跨包调用契约。
- 跨域查询例外。
- 架构测试规则。
