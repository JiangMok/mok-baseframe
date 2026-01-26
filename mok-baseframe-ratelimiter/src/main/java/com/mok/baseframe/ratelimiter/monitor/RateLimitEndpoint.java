package com.mok.baseframe.ratelimiter.monitor;

import com.mok.baseframe.ratelimiter.config.RateLimiterProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 限流监控端点
 */
@Component
@Endpoint(id = "ratelimit")
public class RateLimitEndpoint {

    private final RedisTemplate<String, Object> redisTemplate;

    private final RateLimiterProperties properties;

    public RateLimitEndpoint(RedisTemplate<String, Object> redisTemplate,
                             @Qualifier("mok.ratelimiter-com.mok.baseframe.ratelimiter.config.RateLimiterProperties")
                             RateLimiterProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @ReadOperation
    public Map<String, Object> rateLimitInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("enabled", properties.isEnabled());
        info.put("clusterMode", properties.isClusterMode());
        info.put("redisKeyPrefix", properties.getRedisKeyPrefix());
        info.put("duplicateKeyPrefix", properties.getDuplicateKeyPrefix());

        // 获取限流相关Key的数量
        if (redisTemplate != null) {
            try {
                Set<String> rateKeys = redisTemplate.keys(properties.getRedisKeyPrefix() + "*");
                Set<String> duplicateKeys = redisTemplate.keys(properties.getDuplicateKeyPrefix() + "*");

                info.put("rateLimitKeys", rateKeys != null ? rateKeys.size() : 0);
                info.put("duplicateSubmitKeys", duplicateKeys != null ? duplicateKeys.size() : 0);
            } catch (Exception e) {
                info.put("rateLimitKeys", "获取失败");
                info.put("duplicateSubmitKeys", "获取失败");
            }
        } else {
            info.put("rateLimitKeys", "RedisTemplate不可用");
            info.put("duplicateSubmitKeys", "RedisTemplate不可用");
        }

        return info;
    }
}