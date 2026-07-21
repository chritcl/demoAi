# 协同办公平台（OA Platform）

> 基于 `OA报价20270714.xlsx` 功能清单生成的企业级协同办公系统。
> 技术栈：**Spring Boot 3 + MyBatis-Plus + MySQL + Redis + Spring Security/JWT**（后端）
> + **Vue3 + Vite + Element Plus**（PC 端）+ **Vue3 + Vant**（移动 H5 端）。

## 一、目录结构

```
协同办公平台/
├── OA报价20270714.xlsx        # 原始需求清单
├── oa-backend/                # 后端（Spring Boot 3，端口 10001）
├── oa-frontend/               # PC 端（Vue3 + Element Plus，端口 10002）
├── oa-mobile/                 # 移动 H5 端（Vue3 + Vant，端口 10003）
├── start.bat / stop.bat / restart.bat   # Windows 一键启停（双击，内部调用 oa.ps1）
├── oa.ps1                     # PowerShell 启停引擎 (start|stop|restart|status)
└── README.md
```

## 二、功能覆盖（对照报价清单）

| 一级功能 | 实现情况 |
|---|---|
| ① 统一门户 | 门户首页（统计/最近公告/待办/快捷导航）、消息提醒、应用入口、统一待办/已办（工作台）、搜索（通讯录） |
| ② 信息发布 | 信息发布文章 + 栏目 + 草稿/提交审核/审核通过发布/驳回 |
| ③ 通知公告 | 公告 CRUD + 发布/撤回 + 置顶 + 阅读统计 |
| ④ 公文管理 | 发文（起草→提交审批→自动文号→归档）、收文登记、文号生成、公文查询、公文统计 |
| ⑤ 公文交换 | 收发文传输基础（收文登记/传递/查询） |
| ⑥ 综合办公 | 用车、用印、出差、请休假（均接审批流程）+ 资产管理、办公用品（出入库）、智慧考勤（打卡） |
| ⑦ 通讯录 | 组织/通讯录树、搜索（姓名/模糊/拼音/电话/邮箱）、联系人详情 |
| ⑧ 工作台 | 个人信息、修改密码、待办/已办/办结、流程起草（发文/请假…）、我的发起、委托授权（转办） |
| ⑨ 移动办公 | **移动 H5 端**（消息/公告/流程审批与发起/文件查询/通讯录/个人设置） |
| ⑩ 应用支撑平台 | 身份认证(JWT)、流程管理(审批引擎)、权限管理(RBAC+菜单按钮)、组织架构、成员管理、日志管理、文件服务、水印/安全策略（预留） |

## 三、环境要求

- JDK 17、Maven 3.8+
- Node.js 18+、npm
- MySQL 8.x、Redis 5+

## 四、快速启动

### 0）一键启停（推荐）

**方式一：双击**（最简单）
在项目根目录双击 `start.bat` / `stop.bat` / `restart.bat` 即可。`.bat` 会以
`-ExecutionPolicy Bypass` 调用 PowerShell 引擎 `oa.ps1`，无需手动放开执行策略。

**方式二：PowerShell 终端**
```powershell
.\oa.ps1 start      # 启动 后端(10001) + PC(10002) + 移动(10003)
.\oa.ps1 stop       # 停止全部
.\oa.ps1 restart    # 重启
.\oa.ps1 status     # 查看端口监听状态
```
> 若系统执行策略拦截，可在当前会话临时放开：`Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`
> 或用上面「方式一」的 `.bat`（已自动带 `-ExecutionPolicy Bypass`）。

| 服务 | 地址 |
|---|---|
| 后端 API | http://localhost:10001 （文档 /doc.html） |
| PC 端 | http://localhost:10002 |
| 移动端 | http://localhost:10003 （浏览器开设备模拟器） |

> 首次启动后端约需 20–40 秒（编译 + 下载依赖）。
> 停止采用「按端口定位进程 + 进程树清理」（mvn→java 等子进程一并终止）。

### 1）准备数据库与缓存
- MySQL 默认连接：`localhost:3306`，账号 `root` / `root`（如不同请改 `oa-backend/src/main/resources/application.yml`）
- Redis 默认：`localhost:6379`
- **无需手动建库建表**：首次启动后端会自动执行 `schema.sql`（建表）+ `data.sql`（初始数据），幂等可重复执行。

### 2）手动启动（如不使用一键脚本）
```bash
# 后端（端口 10001）
cd oa-backend && mvn spring-boot:run
# 或打包运行：mvn clean package -DskipTests && java -jar target/oa-platform.jar
# API 文档 http://localhost:10001/doc.html   账号 admin / admin123

# PC 端（端口 10002）
cd oa-frontend && npm install && npm run dev

# 移动端（端口 10003）
cd oa-mobile && npm install && npm run dev
```
生产构建：分别在 `oa-frontend`、`oa-mobile` 执行 `npm run build`（输出 `dist/`、`dist-mobile/`，移动端可直接放入 Android/iOS/鸿蒙 WebView 壳工程复用）。

## 五、账号与权限

| 账号 | 密码 | 角色 | 说明 |
|---|---|---|---|
| admin | admin123 | 超级管理员 | 拥有全部权限 |
| zhangsan | 123456 | 普通员工 | 可发起请假/用车/用印/出差、查看公告/通讯录 |
| lisi | 123456 | 部门主管 | 审批各流程（角色 leader 节点解析到该用户） |

权限基于「角色-菜单/按钮」RBAC，前端动态路由由后端 `/auth/routers` 下发，按钮级用 `v-hasPerm`/`user.hasPerm()` 控制。

## 六、核心设计要点

- **统一响应**：`R<T>{code,msg,data}`，全局异常处理（`GlobalExceptionHandler`），分页 `PageQuery`/`PageResult`。
- **认证**：JWT 无状态，登录信息缓存于 Redis，登出加入黑名单；验证码（可开关）。
- **审批引擎**：`flow_definition→flow_node→flow_instance→flow_task`，支持发起/通过/驳回/转办；
  业务（公文/请假/用车/用印/出差）通过 `FlowCompletedEvent` 事件解耦回写自身状态。
- **审计**：`@OperLog` AOP 切面异步记录操作日志。
- **拼音搜索**：pinyin4j 自动生成 `sys_user.pinyin`，支持通讯录拼音检索。
- **文件**：本地存储，`/file/upload`、`/file/{id}`（图片可直接预览）。

## 七、说明与扩展

- 移动端按 H5 开发（已确认），覆盖「移动端基础能力」；原生 Android/iOS/鸿蒙客户端可对该 H5 做 WebView 封装，一份代码三端通用。
- 报价清单中的「自定义水印/禁止截屏/黑白名单/手势指纹/安全审计/版本设置/文件设置」等安全策略项已在「应用支撑平台」预留入口与数据结构，可在 `oa.captcha`/安全模块按需扩展。
- 维保、第三方软硬件（服务器/电子印章/WPS 中台/信创中间件等）按报价清单「备注」不在本工程范围内。

## 八、构建验证

| 模块 | 验证命令 | 结果 |
|---|---|---|
| 后端 | `mvn clean package -DskipTests` | ✅ 通过，产出 `oa-platform.jar` |
| PC 端 | `npm run build` | ✅ 通过 |
| 移动端 | `npm run build` | ✅ 通过 |
