package com.mok.baseframe.security.jwt;

import com.mok.baseframe.utils.LogUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: JWT Token提供者 >>> token生成验证器  创建顺序:2
 * @author: JN
 * @date: 2026/1/6 11:09
 * @param:
 * @return:
 **/

@Component

public class JwtTokenProvider {
    private static final Logger log = LogUtils.getLogger(JwtTokenProvider.class);

    private final JwtProperties jwtProperties;
    private final UserDetailsService userDetailsService;
    private String secretKey;

    public JwtTokenProvider(JwtProperties jwtProperties,
                            UserDetailsService userDetailsService) {
        this.jwtProperties = jwtProperties;
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(jwtProperties.getSecret().getBytes());
    }


    /**
     * 验证Token（增加黑名单检查）
     */
    public boolean validateToken(String token) {
        try {
            // 2. 验证签名和过期时间
            Claims claims = getClaimsFromToken(token);

            if (claims == null) {
                return false;
            }
            // 3. 检查Token是否已过期
            return !claims.getExpiration().before(new Date());

        } catch (ExpiredJwtException e) {
            log.error("Token已过期");
        } catch (MalformedJwtException e) {
            log.error("Token不正确");
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Token验证失败,token: {}", token);
        }
        return false;
    }

    /**
     * 从Token获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从Token获取过期时间
     */
    public Date getExpirationFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    public String generateToken(String username) {
        return generateToken(username, new HashMap<>());
    }

    /**
     * 获取认证信息
     */
    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(
                userDetails, "", userDetails.getAuthorities());
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
            log.error("解析JWT Token失败,token:{}", token);
            return null;
        }
    }

    /**
     * @description: 生成JWT令牌(带自定义声明)
     * @author: JN
     * @date: 2025/12/31 19:38
     * @param: [username, claims]
     * @return: java.lang.String
     **/
    public String generateToken(String username, Map<String, Object> claims) {
        //使用JWT构建起创建令牌
        return Jwts.builder()
                //设置自定以声明
                .claims(claims)
                //设置主题(通常是用户名)
                .subject(username)
                //设置签发时间
                .issuedAt(new Date())
                //设置过期时间
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getTokenExpire()))
                //使用密钥签名
                .signWith(getSecretKey())
                //压缩成最终字符串
                .compact();
    }


    /**
     * @description: 生成刷新令牌
     * @author: JN
     * @date: 2025/12/31 19:36
     * @param: [username]
     * @return: java.lang.String
     **/
    public String generateRefreshToken(String username) {
        //使用JWT构建起创建令牌
        return Jwts.builder()
                //设置主题
                .subject(username)
                //设置签发时间
                .issuedAt(new Date())
                //设置过期时间(使用刷新令牌的过期时间配置)
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpire()))
                //使用密钥签名
                .signWith(getSecretKey())
                //压缩生成最终字符串
                .compact();
    }

    private SecretKey getSecretKey() {
        // Keys.hmacShaKeyFor : 将字符串转换为HMAC-SHA密钥
        // jwtProperties.getSecret() : 从配置获取密钥字符串
        // etBytes(StandardCharsets.UTF_8) : 转换为UTF8字节数组
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @description: 解析 Bearer 令牌字符串
     * @author: JN
     * @date: 2025/12/31 19:39
     * @param: [bearerToken]
     * @return: java.lang.String
     **/
    public String resolveToken(String bearerToken) {
        //检查bearerToken不为空,兵器已配置的前缀+空格开头
        if (bearerToken != null && bearerToken.startsWith(jwtProperties.getPrefix() + " ")) {
            //去掉前缀和空格,得到纯JWT令牌
            return bearerToken.substring(jwtProperties.getPrefix().length() + 1);
        }
        return null;
    }

}


//-------------------------------------------------------------------------------------------------
//package com.mok.securityframework.security.jwt;
//
//import cn.hutool.core.lang.UUID;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
/// **
// * @description: JWT Token提供者   创建顺序:2
// * @author: JN
// * @date: 2025/12/31
// */
/// /lombok注解,自动生成日志对象log
//
/// /spring注解,声明这是一个组件,会被spring容器管理
//@Component
////lombok注解,为final字段生成构造器
//
//public class JwtTokenProvider  {
//       private static final Logger log = LogUtils.getLogger(JwtTokenProvider.class);
//
//    private final JwtProperties jwtProperties;
//    private final RedisTemplate<String, String> redisTemplate;
//
//
//
//    /**
//     * @description: 获取密钥    从配置的字符串生成hmac-sha密钥
//     * @author: JN
//     * @date: 2025/12/31 19:38
//     * @param: []
//     * @return: javax.crypto.SecretKey
//     **/
//    private SecretKey getSecretKey() {
//        // Keys.hmacShaKeyFor : 将字符串转换为HMAC-SHA密钥
//        // jwtProperties.getSecret() : 从配置获取密钥字符串
//        // etBytes(StandardCharsets.UTF_8) : 转换为UTF8字节数组
//        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
//    }
//
//
//    /**
//     * @description: 从令牌中解析声明(claims)
//     * @author: JN
//     * @date: 2025/12/31 19:36
//     * @param: [token]
//     * @return: io.jsonwebtoken.Claims
//     **/
//    public Claims getClaimsFromToken(String token) {
//        try {
//            //使用JWT解析器解析令牌
//            return Jwts.parser()
//                    //设置验证密钥
//                    .verifyWith(getSecretKey())
//                    //构建解析器
//                    .build()
//                    //解析已签名的令牌
//                    .parseSignedClaims(token)
//                    //获取声明
//                    .getPayload();
//        } catch (Exception e) {
//            log.error("解析JWT Token失败", e);
//            return null;
//        }
//    }
//
//
//
//
//    /**
//     * @description: 生成JWT令牌(带自定义声明)
//     * @author: JN
//     * @date: 2025/12/31 19:38
//     * @param: [username, claims]
//     * @return: java.lang.String
//     **/
//    public String generateToken(String username, Map<String, Object> claims) {
//        //使用JWT构建起创建令牌
//        return Jwts.builder()
//                //设置自定以声明
//                .claims(claims)
//                //设置主题(通常是用户名)
//                .subject(username)
//                //设置签发时间
//                .issuedAt(new Date())
//                //设置过期时间
//                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getTokenExpire()))
//                //使用密钥签名
//                .signWith(getSecretKey())
//                //压缩成最终字符串
//                .compact();
//    }
//
//    /**
//     * @description: 生成JWT令牌(不带自定以声明)
//     * @author: JN
//     * @date: 2025/12/31 19:37
//     * @param: [username]
//     * @return: java.lang.String
//     **/
//    public String generateToken(String username) {
//        return generateToken(username, new HashMap<>());
//    }
//
//    /**
//     * @description: 生成刷新令牌
//     * @author: JN
//     * @date: 2025/12/31 19:36
//     * @param: [username]
//     * @return: java.lang.String
//     **/
//    public String generateRefreshToken(String username) {
//        //使用JWT构建起创建令牌
//        return Jwts.builder()
//                //设置主题
//                .subject(username)
//                //设置签发时间
//                .issuedAt(new Date())
//                //设置过期时间(使用刷新令牌的过期时间配置)
//                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpire()))
//                //使用密钥签名
//                .signWith(getSecretKey())
//                //压缩生成最终字符串
//                .compact();
//    }
//
//
//
//    /**
//     * @description: 从令牌中解析声明
//     * @author: JN
//     * @date: 2025/12/31 19:36
//     * @param: [token]
//     * @return: java.lang.String
//     **/
//    public String getUsernameFromToken(String token) {
//        //解析声明
//        Claims claims = getClaimsFromToken(token);
//        return claims != null ? claims.getSubject() : null;
//    }
//
//    /**
//     * @description: 验证令牌是否有效
//     * @author: JN
//     * @date: 2025/12/31 19:38
//     * @param: [token]
//     * @return: boolean
//     **/
//    public boolean validateToken(String token) {
//        try {
//            //解析令牌获取声明
//            Claims claims = getClaimsFromToken(token);
//            return claims != null && claims.getExpiration().after(new Date());
//        } catch (Exception e) {
//            log.error("验证Jwt Token失败", e);
//            return false;
//        }
//    }
//
//    /**
//     * @description: 解析 Bearer 令牌字符串
//     * @author: JN
//     * @date: 2025/12/31 19:39
//     * @param: [bearerToken]
//     * @return: java.lang.String
//     **/
//    public String resolveToken(String bearerToken) {
//        //检查bearerToken不为空,兵器已配置的前缀+空格开头
//        if (bearerToken != null && bearerToken.startsWith(jwtProperties.getPrefix() + " ")) {
//            //去掉前缀和空格,得到纯JWT令牌
//            return bearerToken.substring(jwtProperties.getPrefix().length() + 1);
//        }
//        return null;
//    }
//
//
//}
