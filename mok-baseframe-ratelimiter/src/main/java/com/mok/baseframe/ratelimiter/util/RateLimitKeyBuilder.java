package com.mok.baseframe.ratelimiter.util;

import com.mok.baseframe.ratelimiter.annotation.PreventDuplicate;
import com.mok.baseframe.ratelimiter.annotation.RateLimit;
import com.mok.baseframe.ratelimiter.config.RateLimiterProperties;
import com.mok.baseframe.ratelimiter.expression.SpelExpressionEvaluator;
import com.mok.baseframe.utils.LogUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * 限流Key构建器
 */
@Component
public class RateLimitKeyBuilder {

    /**
     * 添加日志
     */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RateLimitKeyBuilder.class);
    private final RateLimiterProperties properties;

    private final SpelExpressionEvaluator spelEvaluator;

    public RateLimitKeyBuilder(@Qualifier("mok.ratelimiter-com.mok.baseframe.ratelimiter.config.RateLimiterProperties")
                               RateLimiterProperties properties,
                               SpelExpressionEvaluator spelEvaluator) {
        this.properties = properties;
        this.spelEvaluator = spelEvaluator;
    }

    public String buildRateLimitKey(JoinPoint joinPoint, RateLimit rateLimit) {
        StringBuilder keyBuilder = new StringBuilder(properties.getRedisKeyPrefix());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName(); // 使用简单类名
        String methodName = method.getName();
        String uri = getRequestUri();

        log.info("构建限流Key - 类名: {}, 方法名: {}, URI: {}", className, methodName, uri);

        // 添加作用域前缀
        switch (rateLimit.scope()) {
            case API:
                keyBuilder.append("api:");
                break;
            case USER:
                String userId = getCurrentUserId();
                keyBuilder.append("user:").append(userId).append(":");
                log.info("用户级别限流 - 用户ID: {}", userId);
                break;
            case IP:
                String ip = getClientIp();
                keyBuilder.append("ip:").append(ip).append(":");
                log.info("IP级别限流 - 客户端IP: {}", ip);
                break;
            case GLOBAL:
                keyBuilder.append("global:");
                break;
        }

        // 添加方法和URI
        keyBuilder.append(className).append(".").append(methodName);
        if (uri != null && !uri.equals("unknown")) {
            keyBuilder.append(":").append(uri);
        }

        // 添加自定义Key（如果有）
        String customKey = rateLimit.key();
        if (customKey != null && !customKey.isEmpty()) {
            try {
                String evaluatedKey = spelEvaluator.evaluate(customKey, joinPoint);
                if (evaluatedKey != null && !evaluatedKey.isEmpty()) {
                    keyBuilder.append(":").append(evaluatedKey);
                }
            } catch (Exception e) {
                log.warn("解析SpEL表达式失败: {}", customKey, e);
            }
        }

        String finalKey = keyBuilder.toString();
        log.info("生成的限流Key: {}", finalKey);

        return finalKey;
    }

    /**
     * 构建防重复提交Key
     */
    public String buildDuplicateKey(JoinPoint joinPoint, PreventDuplicate preventDuplicate) {
        StringBuilder keyBuilder = new StringBuilder(properties.getDuplicateKeyPrefix());

        // 添加用户信息
        keyBuilder.append("user:").append(getCurrentUserId()).append(":");

        // 添加方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        keyBuilder.append("method:").append(method.getDeclaringClass().getName())
                  .append(".").append(method.getName()).append(":");

        // 处理自定义Key（支持SpEL表达式）
        String customKey = preventDuplicate.key();
        if (customKey != null && !customKey.isEmpty()) {
            String evaluatedKey = spelEvaluator.evaluate(customKey, joinPoint);
            keyBuilder.append("custom:").append(evaluatedKey).append(":");
        }

        // 如果启用参数检查，添加参数哈希
        if (preventDuplicate.checkParams()) {
            String paramsHash = hashParams(joinPoint.getArgs());
            keyBuilder.append("params:").append(paramsHash);
        }

        return keyBuilder.toString();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            HttpServletRequest request = getHttpServletRequest();
            if (request == null) {
                log.warn("无法获取HttpServletRequest，返回默认IP");
                return "127.0.0.1"; // 默认IP
            }

            // 尝试从多个Header获取IP
            String[] headers = {
                    "X-Forwarded-For",
                    "Proxy-Client-IP",
                    "WL-Proxy-Client-IP",
                    "HTTP_CLIENT_IP",
                    "HTTP_X_FORWARDED_FOR",
                    "X-Real-IP"
            };

            String ip = null;
            for (String header : headers) {
                ip = request.getHeader(header);
                if (isValidIp(ip)) {
                    break;
                }
            }

            // 如果从Header获取不到，使用RemoteAddr
            if (!isValidIp(ip)) {
                ip = request.getRemoteAddr();
            }

            // 处理多个IP的情况
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }

            // 特殊IP处理
            if ("0:0:0:0:0:0:0:1".equals(ip) || "127.0.0.1".equals(ip)) {
                ip = "127.0.0.1";
            }

            log.debug("获取到的客户端IP: {}", ip);
            return ip;
        } catch (Exception e) {
            log.error("获取客户端IP失败", e);
            return "127.0.0.1"; // 默认IP
        }
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            // 这里需要根据你的安全框架获取当前用户ID
            // 如果是测试环境，可以返回固定值
            Object principal = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();

            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            }

            log.warn("无法获取用户ID，返回默认值");
            return "anonymous";
        } catch (Exception e) {
            log.error("获取用户ID失败", e);
            return "anonymous";
        }
    }

    /**
     * 验证IP是否有效
     */
    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
    /**
     * 获取请求URI
     */
    private String getRequestUri() {
        try {
            HttpServletRequest request = getHttpServletRequest();
            return request != null ? request.getRequestURI() : "unknown";
        } catch (Exception e) {
            log.error("获取请求URI失败", e);
            return "unknown";
        }
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.error("获取HttpServletRequest失败", e);
            return null;
        }
    }

    /**
     * 计算参数哈希
     */
    private String hashParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "empty";
        }

        try {
            StringBuilder paramsStr = new StringBuilder();
            for (Object arg : args) {
                if (arg != null) {
                    paramsStr.append(arg.toString());
                }
            }

            return DigestUtils.md5DigestAsHex(
                paramsStr.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "error";
        }
    }
}