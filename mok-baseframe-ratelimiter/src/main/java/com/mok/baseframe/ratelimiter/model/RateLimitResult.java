package com.mok.baseframe.ratelimiter.model;

/**
 * 限流结果
 */
public class RateLimitResult {
    
    private boolean allowed;
    private Long retryAfter;
    private Long currentCount;
    private Long limitCount;
    private String key;

    @Override
    public String toString() {
        return "RateLimitResult{" +
                "allowed=" + allowed +
                ", retryAfter=" + retryAfter +
                ", currentCount=" + currentCount +
                ", limitCount=" + limitCount +
                ", key='" + key + '\'' +
                '}';
    }

    public RateLimitResult() {
        this.allowed = true;
    }
    
    public static RateLimitResult allowed() {
        return new RateLimitResult();
    }
    
    public static RateLimitResult denied(Long retryAfter) {
        RateLimitResult result = new RateLimitResult();
        result.allowed = false;
        result.retryAfter = retryAfter;
        return result;
    }
    
    public boolean isAllowed() {
        return allowed;
    }
    
    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }
    
    public Long getRetryAfter() {
        return retryAfter;
    }
    
    public void setRetryAfter(Long retryAfter) {
        this.retryAfter = retryAfter;
    }
    
    public Long getCurrentCount() {
        return currentCount;
    }
    
    public void setCurrentCount(Long currentCount) {
        this.currentCount = currentCount;
    }
    
    public Long getLimitCount() {
        return limitCount;
    }
    
    public void setLimitCount(Long limitCount) {
        this.limitCount = limitCount;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
}