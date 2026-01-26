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
 * 令牌桶限流策略
 */
@Component
public class TokenBucketStrategy implements RateLimitStrategy {
    private static final Logger log = LogUtils.getLogger(TokenBucketStrategy.class);
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Redis令牌桶限流脚本
     */
    private static final String TOKEN_BUCKET_SCRIPT = 
            "local key = KEYS[1] " +
            "local capacity = tonumber(ARGV[1]) " +
            "local rate = tonumber(ARGV[2]) " +
            "local tokens = tonumber(ARGV[3]) " +
            "local now = tonumber(ARGV[4]) " +
            " " +
            "local bucket = redis.call('hmget', key, 'tokens', 'lastRefillTime') " +
            "local currentTokens = capacity " +
            "local lastRefillTime = now " +
            " " +
            "if bucket[1] and bucket[2] then " +
            "    currentTokens = tonumber(bucket[1]) " +
            "    lastRefillTime = tonumber(bucket[2]) " +
            "    " +
            "    local timePassed = now - lastRefillTime " +
            "    local refillTokens = math.floor(timePassed * rate) " +
            "    currentTokens = math.min(capacity, currentTokens + refillTokens) " +
            "    " +
            "    if timePassed > 0 then " +
            "        lastRefillTime = now " +
            "    end " +
            "end " +
            " " +
            "if currentTokens >= tokens then " +
            "    currentTokens = currentTokens - tokens " +
            "    redis.call('hmset', key, 'tokens', currentTokens, 'lastRefillTime', lastRefillTime) " +
            "    redis.call('expire', key, math.ceil(capacity / rate) * 2) " +  // 设置过期时间
            "    return 0 " +  // 0表示通过
            "else " +
            "    local needTokens = tokens - currentTokens " +
            "    local waitTime = math.ceil(needTokens / rate) " +
            "    return waitTime " +  // 返回需要等待的秒数
            "end";
    
    public TokenBucketStrategy(@Qualifier("rateLimiterRedisTemplate")RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public RateLimitResult execute(RateLimitContext context) {
        String key = context.getKey();
        long capacity = context.getCapacity();
        double rate = context.getRate();
        long currentTime = context.getCurrentTime();
        
        // 默认每次请求消耗1个令牌
        long tokens = 1;
        
        // 执行Lua脚本
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(TOKEN_BUCKET_SCRIPT);
        script.setResultType(Long.class);
        
        Long result = redisTemplate.execute(script, 
            Arrays.asList(key), 
            capacity, rate, tokens, currentTime);
        
        RateLimitResult rateLimitResult = new RateLimitResult();
        rateLimitResult.setAllowed(result != null && result == 0);
        
        if (!rateLimitResult.isAllowed() && result != null && result > 0) {
            rateLimitResult.setRetryAfter(result);
        }
        
        return rateLimitResult;
    }
    
    @Override
    public String getType() {
        return RateLimitType.TOKEN_BUCKET.getValue();
    }
}