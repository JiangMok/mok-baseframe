package com.mok.baseframe.ratelimiter.service.impl;

import com.mok.baseframe.ratelimiter.config.RateLimiterProperties;
import com.mok.baseframe.ratelimiter.core.DuplicateSubmitService;
import com.mok.baseframe.ratelimiter.core.RateLimiterService;
import com.mok.baseframe.ratelimiter.exception.DuplicateSubmitException;
import com.mok.baseframe.ratelimiter.exception.RateLimitException;
import com.mok.baseframe.ratelimiter.model.RateLimitContext;
import com.mok.baseframe.ratelimiter.model.RateLimitResult;
import com.mok.baseframe.ratelimiter.strategy.RateLimitStrategyFactory;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 限流服务实现
 */
@Service
public class RateLimiterServiceImpl implements RateLimiterService, DuplicateSubmitService {
    private static final Logger log = LogUtils.getLogger(RateLimiterServiceImpl.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final RateLimitStrategyFactory strategyFactory;
    private final RateLimiterProperties properties;
    
    public RateLimiterServiceImpl(@Qualifier("rateLimiterRedisTemplate")RedisTemplate<String, Object> redisTemplate,
                                 RateLimitStrategyFactory strategyFactory,
                                  @Qualifier("mok.ratelimiter-com.mok.baseframe.ratelimiter.config.RateLimiterProperties")
                                  RateLimiterProperties properties) {
        this.redisTemplate = redisTemplate;
        this.strategyFactory = strategyFactory;
        this.properties = properties;
    }
    
    @Override
    public boolean isAllowed(RateLimitContext context) {
        return check(context).isAllowed();
    }
    
    @Override
    public RateLimitResult check(RateLimitContext context) {
        return strategyFactory.getStrategy(context.getType()).execute(context);
    }
    
    @Override
    public void clear(String key) {
        redisTemplate.delete(key);
    }
    
    @Override
    public long getRemaining(String key) {
        // 这里需要根据具体的策略实现
        // 简化实现：返回key的剩余生存时间作为参考
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null ? ttl : 0;
    }
    
    @Override
    public long getResetTime(String key) {
        Long ttl = redisTemplate.getExpire(key);
        if (ttl != null && ttl > 0) {
            return System.currentTimeMillis() / 1000 + ttl;
        }
        return 0;
    }
    
    @Override
    public boolean isAllowed(String key, int lockTime) {
        return isAllowed(key, lockTime, properties.getDefaultDuplicateMessage());
    }
    
    @Override
    public boolean isAllowed(String key, int lockTime, String message) {
        return checkAndLock(key, lockTime);
    }
    
    @Override
    public void release(String key) {
        redisTemplate.delete(properties.getDuplicateKeyPrefix() + key);
    }
    
    @Override
    public boolean checkAndLock(String key, int lockTime) {
        String fullKey = properties.getDuplicateKeyPrefix() + key;
        
        // Lua脚本保证原子性
        String lockScript = 
            "if redis.call('setnx', KEYS[1], ARGV[1]) == 1 then " +
            "    return redis.call('expire', KEYS[1], ARGV[2]) " +
            "else " +
            "    return 0 " +
            "end";
        
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(lockScript);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, 
            Arrays.asList(fullKey), 
            "locked", lockTime);
        
        return result != null && result == 1;
    }
    
    /**
     * 手动限流检查（编程式使用）
     */
    public void checkRateLimit(String key, long limit, long window) {
        RateLimitContext context = RateLimitContext.builder()
            .key(key)
            .limit(limit)
            .window(window)
            .build();
        
        RateLimitResult result = check(context);
        if (!result.isAllowed()) {
            throw new RateLimitException(properties.getDefaultRateLimitMessage(), result.getRetryAfter());
        }
    }
    
    /**
     * 手动防重复提交检查（编程式使用）
     */
    public void checkDuplicateSubmit(String key, int lockTime) {
        if (!checkAndLock(key, lockTime)) {
            throw new DuplicateSubmitException(properties.getDefaultDuplicateMessage());
        }
    }
}