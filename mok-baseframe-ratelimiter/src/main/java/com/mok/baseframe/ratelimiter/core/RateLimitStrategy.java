package com.mok.baseframe.ratelimiter.core;

import com.mok.baseframe.ratelimiter.model.RateLimitContext;
import com.mok.baseframe.ratelimiter.model.RateLimitResult;

/**
 * 限流策略接口
 */
public interface RateLimitStrategy {
    
    /**
     * 执行限流检查
     */
    RateLimitResult execute(RateLimitContext context);
    
    /**
     * 获取策略类型
     */
    String getType();
}