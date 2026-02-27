# mok-baseframe
spring boot+spring security+jwt+mysql+mybatis plus+rabbit MQ搭建的后端基础框架
# MOK-BaseFrame 项目 README

## 1. 项目概述

MOK-BaseFrame 是一个基于 **Spring Boot 3.x** 和 **Spring Security** 构建的现代化权限管理系统后端框架。项目采用模块化设计，集成了 **JWT 认证**、**RBAC 权限控制**、**Redis 缓存**、**RabbitMQ 消息队列**、**操作日志**、**文件管理**、**验证码**、**限流与防重复提交**等核心功能，旨在为快速开发企业级应用提供一个坚实、高效、可扩展的基础平台。

**核心设计理念**：
- **无状态认证**：基于 JWT 的 Token 认证，适合分布式部署。
- **模块解耦**：各功能模块独立，通过清晰的接口协作。
- **高并发支持**：利用 Redis 缓存、消息队列、限流等机制应对高流量场景。
- **安全性**：内置防重复提交、限流、敏感数据脱敏、Token 黑名单等安全措施。
- **可观测性**：操作日志、健康检查、系统监控端点。

## 2. 技术栈

| 技术 | 用途 | 关键特性 |
|------|------|----------|
| Spring Boot 3.x | 基础框架 | IOC、AOP、自动配置、属性绑定 |
| Spring Security | 认证与授权 | `@PreAuthorize`、过滤器链、UserDetailsService |
| JWT (jjwt) | 令牌管理 | 生成、验证、解析 Token |
| Spring Data Redis | 缓存与分布式协调 | 权限缓存、Token 黑名单、分布式锁、限流计数器 |
| RabbitMQ | 消息队列 | 异步日志、订单取消延迟消息、库存更新最终一致性 |
| MyBatis Plus | ORM 框架 | 分页插件、自动填充、Lambda 查询、乐观锁 |
| MySQL | 数据库 | 存储业务数据 |
| Jackson / Fastjson2 | JSON 处理 | 序列化、反序列化、JSON 脱敏 |
| Hutool | 工具库 | ID 生成、随机字符串、Bean 复制 |
| SLF4J | 日志门面 | 日志记录 |
| Swagger / OpenAPI | 接口文档 | 自动生成 API 文档，集成 Bearer 认证 |
| Apache Commons IO | 文件操作 | IOUtils、FilenameUtils |
| Lua | Redis 脚本 | 保证原子操作（限流、分布式锁） |
| BCrypt | 密码加密 | 密码编码器 |

## 3. 模块架构

项目采用分层模块化设计，各模块职责清晰，可独立使用或按需集成。

```bash
mok-baseframe
├── common # 基础核心模块：统一响应、异常体系、分页、常量、工具类
├── core # 核心功能模块：操作日志注解与切面、配置管理
├── security # 安全认证模块：JWT、权限检查、用户详情、过滤器
├── ratelimiter # 限流与防重复提交模块：注解、策略、Lua 脚本
├── captcha # 验证码模块：生成图片、存储 Redis、验证
├── file # 文件管理模块：上传、下载、删除、分页查询
├── mq # 消息队列模块：RabbitMQ 配置、生产者、消费者
├── order # 订单模块（示例业务模块）：商品、优惠券、订单、秒杀
└── permission # 权限管理模块（示例业务模块）：用户、角色、权限管理
```

**模块依赖关系**：
- 所有业务模块（order、permission）依赖 common、core、security、ratelimiter、file、mq 等基础模块。
- 基础模块之间可能相互依赖（如 core 依赖 common、mq）。

## 4. 模块详细介绍

### 4.1 基础核心模块 (common)

提供项目的基础设施：
- **统一响应 `R<T>`**：标准化的接口返回格式，包含 code、msg、data、timestamp。
- **业务异常体系**：`BusinessException` 及其子类，支持 Builder 模式。
- **全局异常处理器**：统一处理各类异常（参数校验、认证授权、数据库、运行时等），转换为 `R` 响应。
- **分页参数 `PageParam` 与分页结果 `PageResult`**：封装分页请求和响应，与 MyBatis Plus 无缝集成。
- **常量接口**：定义响应码、Token 头等常量。
- **枚举**：定义业务操作类型等。
- **工具类**：JSON 脱敏、日志获取、密码生成、响应写入等。

### 4.2 核心功能模块 (core)

提供系统核心功能支持：
- **操作日志 `@OperationLog`**：通过 AOP 拦截请求，收集日志信息，通过 RabbitMQ 异步发送，实现与业务解耦。
- **配置管理**：验证码配置、文件存储配置、操作日志配置、MyBatis-Plus 配置、Redis 配置、Swagger 配置、Web 配置。
- **MyBatis-Plus 自动填充**：自动填充创建时间、更新时间。
- **Redis 序列化配置**：使用 Jackson 实现 JSON 序列化。
- **Swagger 集成**：自动生成 API 文档，支持 Bearer 认证。

### 4.3 安全认证模块 (security)

实现基于 JWT 的无状态认证与授权：
- **JWT 管理**：生成、验证、解析 Token，支持自定义 claims，提供刷新令牌。
- **Token 黑名单**：退出登录时将 Token 加入 Redis 黑名单，自动过期。
- **用户详情加载**：`CustomUserDetailsServiceImpl` 根据用户名加载用户，封装为 `SecurityUser`。
- **权限检查器 `PermissionChecker`**：从缓存获取用户权限，配合 `@PreAuthorize` 使用方法级权限控制。
- **认证过滤器 `JwtAuthenticationFilter`**：拦截请求，验证 Token，设置认证信息。
- **异常处理器 `SecurityExceptionHandler`**：统一处理认证失败和权限不足异常，返回 JSON 格式。
- **安全配置**：禁用 CSRF、配置 CORS、放行公开接口、无状态会话。
- **系统管理接口**：清除缓存、获取系统信息、健康检查。

### 4.4 限流与防重复提交模块 (ratelimiter)

提供接口级别的流量控制和防重复提交：
- **限流注解 `@RateLimit`**：支持固定窗口、滑动窗口、令牌桶算法；作用域包括 API、用户、IP、全局。
- **防重复提交注解 `@PreventDuplicate`**：基于 Redis 分布式锁，锁定时间内阻止重复请求。
- **策略工厂**：根据注解类型选择对应的限流策略（Lua 脚本实现原子操作）。
- **Key 构建器**：根据作用域、用户、IP、参数等生成唯一 Redis Key。
- **监控端点**：通过 Actuator 查看限流状态和 Key 数量。

### 4.5 验证码模块 (captcha)

提供图形验证码功能：
- **生成验证码**：支持字符型（去除了易混淆字符）和数学计算型，生成图片并返回 Base64 和唯一 key。
- **存储与验证**：验证码存储在 Redis，设置过期时间，验证后立即删除，确保一次性使用。
- **限流保护**：生成接口使用 `@RateLimit` 限制单 IP 频率。

### 4.6 文件管理模块 (file)

实现文件的统一管理：
- **文件上传**：支持普通文件和头像上传，按日期分目录存储，生成可访问的完整 URL。
- **文件下载**：通过 ID 下载，自动更新下载次数。
- **文件查询**：分页查询，支持按文件名、类型、上传用户等条件过滤。
- **文件删除**：支持单个和批量删除（逻辑删除，保留物理文件）。
- **配置化**：存储路径、URL 前缀、允许类型、基础 URL 均可配置。

### 4.7 消息队列模块 (mq)

基于 RabbitMQ 实现异步消息处理：
- **队列与交换机定义**：操作日志、订单支付、订单取消、库存更新等队列，配置死信、TTL、长度限制。
- **操作日志生产者与消费者**：将日志消息异步发送并持久化到数据库，支持幂等性检查。
- **消息可靠性**：手动确认、重试机制、死信队列处理失败消息。
- **JSON 消息转换器**：消息自动序列化为 JSON。

### 4.8 订单模块 (order) - 示例业务模块

演示了如何利用基础模块构建复杂业务：
- **商品管理**：增删改查、库存管理、秒杀商品设置。
- **优惠券管理**：增删改查、抢券（使用 Redis 预减 + 乐观锁 + 分布式锁）、用户优惠券管理。
- **订单管理**：直接支付下单、确认订单（待支付）、支付、取消（超时取消通过 MQ 延迟消息）、订单查询。
- **秒杀功能**：秒杀下单（限流、验证码、Redis 预减库存）、库存预热。
- **发货管理**：发货单创建、发货、收货。
- **消息队列应用**：订单取消延迟队列、支付成功后续处理、库存更新异步处理。
- **缓存应用**：商品/优惠券/秒杀库存 Redis 预减、分布式锁、限流、数据预热。
- **乐观锁**：库存扣减、优惠券扣减、订单状态更新使用版本号。

### 4.9 权限管理模块 (permission) - 示例业务模块

实现经典的 RBAC 权限模型：
- **用户管理**：用户增删改查、状态修改、密码重置、数据权限（普通用户只能管理自己创建的用户）。
- **角色管理**：角色增删改查、状态管理、角色权限分配。
- **权限管理**：权限增删改查、权限树构建、菜单树生成（包含菜单、按钮、接口三种类型）。
- **权限缓存**：用户登录后权限缓存到 Redis，权限变更时自动清除。
- **操作日志**：记录对权限模块的操作。

## 5. 快速开始

### 5.1 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.9+
- Maven 3.6+

### 5.2 配置修改

克隆项目后，修改 `application.yml` 中的以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mok_baseframe?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: root
    password: your-password
  redis:
    host: localhost
    port: 6379
    password:       # 如有密码
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

jwt:
  secret: your-strong-secret-key   # 生产环境务必修改

file:
  storage:
    base-path: /data/uploads        # 文件存储根目录
    base-url: http://localhost:8080/api  # 服务访问地址
```
### 5.3 初始化数据库
执行项目中的 sql/init.sql 脚本，创建数据库和基础表（用户、角色、权限等）。

### 5.4 启动项目
运行 Application.java 的 main 方法。访问 Swagger 文档：http://localhost:8080/api/swagger-ui.html

默认管理员账号：admin / 123456（密码需使用 PasswordGenerator 工具类生成后手动更新数据库）。

## 6. 配置说明汇总
| 配置前缀 | 说明 | 示例 |
| :--- | :--- | :--- |
| jwt | JWT 令牌配置 | jwt.secret, jwt.token-expire |
| captcha | 验证码配置 | captcha.type, captcha.expire |
| file.storage | 文件存储配置 | file.storage.base-path, file.storage.allowed-types |
| operation-log | 操作日志配置 | operation-log.enabled, operation-log.record-get |
| mok.ratelimiter | 限流模块配置 | mok.ratelimiter.default-limit, mok.ratelimiter.redis-key-prefix |
| spring.rabbitmq | RabbitMQ 连接配置 | spring.rabbitmq.host, spring.rabbitmq.port |
| spring.datasource | 数据库连接配置 | spring.datasource.url, spring.datasource.username |
| spring.redis | Redis 连接配置 | spring.redis.host, spring.redis.port |
## 7. API 文档概览
项目集成了 Swagger，启动后访问 /swagger-ui.html 可查看所有接口文档。主要接口分类：

- **认证管理 (/auth)**：登录、登出、刷新 Token、获取当前用户。

- **系统管理 (/system)**：清除缓存、系统信息、健康检查。

- **验证码 (/captcha)**：生成验证码、验证。

- **文件管理 (/files)**：上传、下载、删除、分页查询。

- **操作日志 (/operation-log)**：分页查询、详情、删除、统计。

- **权限管理 (/permission)**：权限树、菜单树、权限增删改查。

- **角色管理 (/role)**：角色增删改查、分配权限。

- **用户管理 (/user)**：用户增删改查、状态修改、密码重置。

- **商品管理 (/product)**：商品增删改查、秒杀设置。

- **优惠券管理 (/coupon)**：优惠券增删改查、抢券。

- **订单管理 (/order)**：下单、支付、取消、查询。

- **秒杀管理 (/seckill)**：秒杀下单、获取验证码。

