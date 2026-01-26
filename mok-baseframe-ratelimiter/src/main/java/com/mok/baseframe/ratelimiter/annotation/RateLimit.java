package com.mok.baseframe.ratelimiter.annotation;

import com.mok.baseframe.ratelimiter.enums.RateLimitType;
import com.mok.baseframe.ratelimiter.enums.RateLimitScope;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解类
**/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 限流Key前缀，默认为空，会使用类名+方法名
     */
    String key() default "";
    
    /**
     * 限流类型，默认为滑动窗口
     */
    RateLimitType type() default RateLimitType.SLIDING_WINDOW;
    
    /**
     * 限流作用域，默认为接口级别
     */
    RateLimitScope scope() default RateLimitScope.API;
    
    /**
     * 时间窗口大小
     */
    long window() default 60;
    
    /**
     * 时间单位
     */
    TimeUnit unit() default TimeUnit.SECONDS;
    
    /**
     * 在时间窗口内允许的最大请求数
     */
    long limit() default 10;
    
    /**
     * 令牌桶容量（仅限令牌桶算法使用）
     */
    long capacity() default 20;
    
    /**
     * 令牌生成速率（每秒，仅限令牌桶算法使用）
     */
    double rate() default 5.0;
    
    /**
     * 超过限流后的提示信息
     */
    String message() default "";
    
    /**
     * 是否启用，默认启用
     */
    boolean enabled() default true;
}