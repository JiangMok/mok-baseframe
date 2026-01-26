package com.mok.baseframe.security.jwt;

import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service

public class TokenBlacklistService {
    private static final Logger log = LogUtils.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenParser jwtTokenParser;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate,
                                 JwtTokenParser jwtTokenParser) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenParser = jwtTokenParser;
    }

    /**
     * 将Token加入黑名单
     */
    public void addToBlacklist(String token) {
        try {
            // 获取Token的过期时间
            Date expirationDate = jwtTokenParser.getExpirationFromToken(token);
            Date now = new Date();
            long ttl = expirationDate.getTime() - now.getTime();

            if (ttl > 0) {
                // 计算Token的MD5作为key（不存储原始Token，更安全）
                String tokenMd5 = jwtTokenParser.getTokenMd5(token);
                String key = BLACKLIST_PREFIX + tokenMd5;

                // 存储到Redis，自动过期
                redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
                log.info("Token已加入黑名单，剩余时间: {}秒", ttl / 1000);
            }
        } catch (Exception e) {
            log.error("加入黑名单失败", e);
        }
    }

    /**
     * 检查Token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenMd5 = jwtTokenParser.getTokenMd5(token);
            String key = BLACKLIST_PREFIX + tokenMd5;

            // 检查Redis中是否存在该key
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("检查黑名单失败", e);
            return false; // 如果检查失败，安全起见不允许访问
        }
    }


}