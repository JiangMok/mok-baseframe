package com.mok.baseframe.ratelimiter.aspect;

import com.mok.baseframe.common.BusinessException;
import com.mok.baseframe.ratelimiter.annotation.PreventDuplicate;
import com.mok.baseframe.ratelimiter.annotation.RateLimit;
import com.mok.baseframe.ratelimiter.config.RateLimiterProperties;
import com.mok.baseframe.ratelimiter.enums.RateLimitScope;
import com.mok.baseframe.ratelimiter.exception.DuplicateSubmitException;
import com.mok.baseframe.ratelimiter.exception.RateLimitException;
import com.mok.baseframe.ratelimiter.expression.SpelExpressionEvaluator;
import com.mok.baseframe.ratelimiter.model.RateLimitContext;
import com.mok.baseframe.ratelimiter.service.impl.RateLimiterServiceImpl;
import com.mok.baseframe.ratelimiter.util.RateLimitKeyBuilder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 限流切面
 */
@Aspect
@Component
public class RateLimitAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);
    
    private final RateLimiterServiceImpl rateLimiterService;
    
    private final RateLimiterProperties properties;
    
    private final RateLimitKeyBuilder keyBuilder;
    
    private final SpelExpressionEvaluator spelEvaluator;

    public RateLimitAspect(RateLimiterServiceImpl rateLimiterService,
                           @Qualifier("mok.ratelimiter-com.mok.baseframe.ratelimiter.config.RateLimiterProperties")
                           RateLimiterProperties properties,
                           RateLimitKeyBuilder keyBuilder,
                           SpelExpressionEvaluator spelEvaluator) {
        this.rateLimiterService = rateLimiterService;
        this.properties = properties;
        this.keyBuilder = keyBuilder;
        this.spelEvaluator = spelEvaluator;
    }

    /**
     * 限流切点
     */
    @Before("@annotation(rateLimitAnnotation)")
    public void doRateLimit(JoinPoint joinPoint, RateLimit rateLimitAnnotation) {
        if (!properties.isEnabled() || !rateLimitAnnotation.enabled()) {
            return;
        }
        
        try {
            // 构建限流Key
            String key = keyBuilder.buildRateLimitKey(joinPoint, rateLimitAnnotation);
            
            // 构建限流上下文
            RateLimitContext context = RateLimitContext.builder()
                .key(key)
                .type(rateLimitAnnotation.type())
                .scope(rateLimitAnnotation.scope())
                .window(getWindowInSeconds(rateLimitAnnotation))
                .limit(getLimit(rateLimitAnnotation))
                .capacity(rateLimitAnnotation.capacity())
                .rate(rateLimitAnnotation.rate())
                .message(getMessage(rateLimitAnnotation))
                .build();
            // 执行限流检查
            var result = rateLimiterService.check(context);
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            if (!result.isAllowed()) {
                String message = result.getRetryAfter() != null ?
                    String.format("%s，请等待 %d 秒后重试", context.getMessage(), result.getRetryAfter()) :
                    context.getMessage();
                
//                throw new RateLimitException(message, result.getRetryAfter());
                throw new BusinessException(message);
            }
            
            logger.debug("限流通过: key={}, scope={}", key, rateLimitAnnotation.scope());
            
//        } catch (RateLimitException e) {
//            throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("限流切面错误", e);
            // 限流组件异常不影响业务
        }
    }
    
    /**
     * 防重复提交切点
     */
    @Before("@annotation(preventDuplicateAnnotation)")
    public void doPreventDuplicate(JoinPoint joinPoint, PreventDuplicate preventDuplicateAnnotation) {
        if (!properties.isEnabled()) {
            return;
        }
        
        try {
            // 构建防重复提交Key
            String key = keyBuilder.buildDuplicateKey(joinPoint, preventDuplicateAnnotation);
            int lockTime = preventDuplicateAnnotation.lockTime();
            
            // 检查是否允许提交
            boolean allowed = rateLimiterService.checkAndLock(key, lockTime);
            
            if (!allowed) {
                String message = preventDuplicateAnnotation.message();
                if (message == null || message.isEmpty()) {
                    message = properties.getDefaultDuplicateMessage();
                }
//                throw new DuplicateSubmitException(message);
                throw new BusinessException(message);
            }
            
            logger.debug("重复提交防护通过: key={}", key);
            
//        } catch (DuplicateSubmitException e) {
//            throw e;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("防止重复切面错误", e);
            // 防重复组件异常不影响业务
        }
    }
    
    private long getWindowInSeconds(RateLimit rateLimit) {
        return rateLimit.unit().toSeconds(rateLimit.window());
    }
    
    private long getLimit(RateLimit rateLimit) {
        return rateLimit.limit() > 0 ? rateLimit.limit() : properties.getDefaultLimit();
    }
    
    private String getMessage(RateLimit rateLimit) {
        String message = rateLimit.message();
        return (message == null || message.isEmpty()) ? 
            properties.getDefaultRateLimitMessage() : message;
    }
}