package com.mok.baseframe.security.jwt;

import com.mok.baseframe.utils.LogUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;


@Component

public class JwtTokenParser {
    private static final Logger log = LogUtils.getLogger(JwtTokenParser.class);

    //    @Value("${jwt.secret-key}")
//    private String secretKey;
    private final JwtProperties jwtProperties;
    private String encodedSecretKey;

    public JwtTokenParser(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        encodedSecretKey = Base64.getEncoder().encodeToString(jwtProperties.getSecret().getBytes());
    }

    /**
     * 从Token获取用户名（只解析，不验证）
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            log.error("解析Token失败", e);
            return null;
        }
    }

    /**
     * 从Token获取过期时间
     */
    public Date getExpirationFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("解析Token过期时间失败", e);
            return null;
        }
    }

    /**
     * 获取Token的MD5（供黑名单使用）
     */
    public String getTokenMd5(String token) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(token.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(token.hashCode());
        }
    }

    public Claims getClaimsFromToken(String token) {
        try {
            //使用JWT解析器解析令牌
            return Jwts.parser()
                    //设置验证密钥
                    .verifyWith(getSecretKey())
                    //构建解析器
                    .build()
                    //解析已签名的令牌
                    .parseSignedClaims(token)
                    //获取声明
                    .getPayload();
        } catch (Exception e) {
            log.error("解析JWT Token失败", e);
            return null;
        }
    }

    private SecretKey getSecretKey() {
        // Keys.hmacShaKeyFor : 将字符串转换为HMAC-SHA密钥
        // jwtProperties.getSecret() : 从配置获取密钥字符串
        // etBytes(StandardCharsets.UTF_8) : 转换为UTF8字节数组
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}