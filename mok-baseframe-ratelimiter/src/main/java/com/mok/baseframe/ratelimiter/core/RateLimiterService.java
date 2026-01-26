package com.mok.baseframe.ratelimiter.core;

import com.mok.baseframe.ratelimiter.model.RateLimitContext;
import com.mok.baseframe.ratelimiter.model.RateLimitResult;

/**
 * 限流服务接口
 */
public interface RateLimiterService {
    
    /**
     * 检查是否允许请求
     */
    boolean isAllowed(RateLimitContext context);
    
    /**
     * 检查是否允许请求并返回详细信息
     */
    RateLimitResult check(RateLimitContext context);
    
    /**
     * 清除限流记录
     */
    void clear(String key);
    
    /**
     * 获取剩余次数
     */
    long getRemaining(String key);
    
    /**
     * 获取重置时间
     */
    long getResetTime(String key);
}