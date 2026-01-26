package com.mok.baseframe.ratelimiter.strategy.impl;

import com.mok.baseframe.ratelimiter.core.RateLimitStrategy;
import com.mok.baseframe.ratelimiter.enums.RateLimitType;
import com.mok.baseframe.ratelimiter.model.RateLimitContext;
import com.mok.baseframe.ratelimiter.model.RateLimitResult;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 固定窗口限流策略
 */
@Component
public class FixedWindowStrategy implements RateLimitStrategy {
    private static final Logger log = LogUtils.getLogger(FixedWindowStrategy.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Redis固定窗口限流脚本
     */
    private static final String FIXED_WINDOW_SCRIPT = 
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = tonumber(ARGV[3]) " +
            " " +
            "local count = redis.call('get', key) " +
            " " +
            "if count then " +
            "    count = tonumber(count) " +
            "    if count >= limit then " +
            "        local ttl = redis.call('ttl', key) " +
            "        return ttl " +  // 返回剩余过期时间
            "    else " +
            "        redis.call('incr', key) " +
            "        return 0 " +  // 0表示通过
            "    end " +
            "else " +
            "    redis.call('setex', key, window, 1) " +
            "    return 0 " +  // 0表示通过
            "end";
    
    public FixedWindowStrategy(@Qualifier("rateLimiterRedisTemplate")RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public RateLimitResult execute(RateLimitContext context) {
        String key = context.getKey();
        long limit = context.getLimit();
        long window = context.getWindow();
        long currentTime = context.getCurrentTime();
        
        // 执行Lua脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(FIXED_WINDOW_SCRIPT);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, 
            Arrays.asList(key), 
            limit, window, currentTime);
        
        RateLimitResult rateLimitResult = new RateLimitResult();
        rateLimitResult.setAllowed(result != null && result == 0);
        
        if (!rateLimitResult.isAllowed() && result != null && result > 0) {
            rateLimitResult.setRetryAfter(result);
        }
        
        return rateLimitResult;
    }
    
    @Override
    public String getType() {
        return RateLimitType.FIXED_WINDOW.getValue();
    }
}