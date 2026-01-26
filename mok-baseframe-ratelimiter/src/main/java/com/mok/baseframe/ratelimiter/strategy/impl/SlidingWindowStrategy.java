package com.mok.baseframe.ratelimiter.strategy.impl;

import com.mok.baseframe.ratelimiter.core.RateLimitStrategy;
import com.mok.baseframe.ratelimiter.model.RateLimitContext;
import com.mok.baseframe.ratelimiter.model.RateLimitResult;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * 滑动窗口限流策略
 */
@Component
public class SlidingWindowStrategy implements RateLimitStrategy {
    private static final Logger log = LogUtils.getLogger(SlidingWindowStrategy.class);
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis滑动窗口限流脚本
     */
    private static final String SLIDING_WINDOW_SCRIPT =
            "local key = KEYS[1] " +
            "local limit = tonumber(ARGV[1]) " +
            "local window = tonumber(ARGV[2]) " +
            "local current = tonumber(ARGV[3]) " +
            "local requestId = ARGV[4] " +
            "local clearBefore = current - window " +
            " " +
            "redis.call('zremrangebyscore', key, 0, clearBefore) " +
            "local count = redis.call('zcard', key) " +
            " " +
            "if count < limit then " +
            "    redis.call('zadd', key, current, requestId) " +
            "    redis.call('expire', key, window) " +
            "    return 0 " +
            "else " +
            "    local oldest = redis.call('zrange', key, 0, 0, 'withscores') " +
            "    if #oldest > 0 then " +
            "        local retryAfter = window - (current - tonumber(oldest[2])) " +
            "        return retryAfter " +
            "    else " +
            "        return -1 " +
            "    end " +
            "end";

    public SlidingWindowStrategy(@Qualifier("rateLimiterRedisTemplate")RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RateLimitResult execute(RateLimitContext context) {
        String key = context.getKey();
        long limit = context.getLimit();
        long window = context.getWindow();
        long currentTime = context.getCurrentTime(); // 这里应该是毫秒级时间戳

        // 生成唯一请求ID
        String requestId = key + ":" + UUID.randomUUID().toString();

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(SLIDING_WINDOW_SCRIPT);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script,
                Arrays.asList(key),
                limit, window, currentTime, requestId); // 传入唯一标识

        RateLimitResult rateLimitResult = new RateLimitResult();
        rateLimitResult.setKey(key);
        rateLimitResult.setAllowed(result != null && result == 0);
        rateLimitResult.setLimitCount(limit);

        if (!rateLimitResult.isAllowed() && result != null && result > 0) {
            rateLimitResult.setRetryAfter(result);
        }

        return rateLimitResult;
    }

    @Override
    public String getType() {
        return "sliding_window";
    }
}