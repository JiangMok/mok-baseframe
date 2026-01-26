package com.mok.baseframe.ratelimiter.exception;

/**
 * 限流异常
 */
public class RateLimitException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final String code;
    
    /**
     * 限流剩余时间（秒）
     */
    private final Long retryAfter;
    
    public RateLimitException(String message) {
        super(message);
        this.code = "RATE_LIMIT_EXCEEDED";
        this.retryAfter = null;
    }
    
    public RateLimitException(String code, String message) {
        super(message);
        this.code = code;
        this.retryAfter = null;
    }
    
    public RateLimitException(String message, Long retryAfter) {
        super(message);
        this.code = "RATE_LIMIT_EXCEEDED";
        this.retryAfter = retryAfter;
    }
    
    public RateLimitException(String code, String message, Long retryAfter) {
        super(message);
        this.code = code;
        this.retryAfter = retryAfter;
    }
    
    public String getCode() {
        return code;
    }
    
    public Long getRetryAfter() {
        return retryAfter;
    }
}