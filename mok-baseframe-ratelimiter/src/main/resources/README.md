# 限流防刷模块使用说明

## 一、功能特性
1. 多种限流算法：滑动窗口、令牌桶、固定窗口
2. 多维度限流：接口级、用户级、IP级、全局级
3. 防重复提交：基于分布式锁，防止重复操作
4. 注解驱动：通过注解轻松配置限流规则
5. 分布式支持：基于Redis，支持集群部署

## 二、快速开始

### 1. 添加依赖
    <dependency>
        <groupId>com.mok.baseframe</groupId>
        <artifactId>mok-baseframe-ratelimiter</artifactId>
        <version>1.0.0</version>
    </dependency>

###  2. 配置模块
配置限流模块（可选）yml配置
```yaml
    mok:
      ratelimiter:
        enabled: true
        redis-key-prefix: "rate:limit:"
        default-window: 60
        default-limit: 10
```

### 3. 使用注解
①限流注解
```java
Java
@RateLimit(
    type = RateLimitType.SLIDING_WINDOW,
    scope = RateLimitScope.USER,
    window = 60,
    limit = 5,
    message = "您操作过于频繁"
)
@GetMapping("/api/users")
public Result<?> getUsers() {
    // 业务逻辑
}
```
②防重复提交注解
```java
Java
@PreventDuplicate(
    key = "#userDTO.username",
    lockTime = 3,
    message = "请勿重复提交"
)
@PostMapping("/api/users")
public Result<?> createUser(@RequestBody UserDTO userDTO) {
    // 业务逻辑
}
```
③编程式使用
```java
Java
@Service
public class SomeService {

    @Autowired
    private RateLimiterServiceImpl rateLimiterService;

    public void someMethod() {
        // 手动限流检查
        rateLimiterService.checkRateLimit("custom:key", 10, 60);

        // 手动防重复提交检查
        rateLimiterService.checkDuplicateSubmit("custom:key", 3);
    }
}
```