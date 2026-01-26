package com.mok.baseframe.ratelimiter.model;

import com.mok.baseframe.ratelimiter.enums.RateLimitScope;
import com.mok.baseframe.ratelimiter.enums.RateLimitType;

/**
 * 限流上下文
 */
public class RateLimitContext {
    
    private String key;
    private RateLimitType type;
    private RateLimitScope scope;
    private long window;
    private long limit;
    private long capacity;
    private double rate;
    private String message;
    private long currentTime;
    private String clientIp;
    private String userId;
    private String method;
    private String uri;

    @Override
    public String toString() {
        return "RateLimitContext{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", scope=" + scope +
                ", window=" + window +
                ", limit=" + limit +
                ", capacity=" + capacity +
                ", rate=" + rate +
                ", message='" + message + '\'' +
                ", currentTime=" + currentTime +
                ", clientIp='" + clientIp + '\'' +
                ", userId='" + userId + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

    // 构造方法
    public RateLimitContext() {
        this.currentTime = System.currentTimeMillis() / 1000;
    }
    
    // Builder模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final RateLimitContext context;
        
        private Builder() {
            this.context = new RateLimitContext();
        }
        
        public Builder key(String key) {
            context.key = key;
            return this;
        }
        
        public Builder type(RateLimitType type) {
            context.type = type;
            return this;
        }
        
        public Builder scope(RateLimitScope scope) {
            context.scope = scope;
            return this;
        }
        
        public Builder window(long window) {
            context.window = window;
            return this;
        }
        
        public Builder limit(long limit) {
            context.limit = limit;
            return this;
        }
        
        public Builder capacity(long capacity) {
            context.capacity = capacity;
            return this;
        }
        
        public Builder rate(double rate) {
            context.rate = rate;
            return this;
        }
        
        public Builder message(String message) {
            context.message = message;
            return this;
        }
        
        public Builder clientIp(String clientIp) {
            context.clientIp = clientIp;
            return this;
        }
        
        public Builder userId(String userId) {
            context.userId = userId;
            return this;
        }
        
        public Builder method(String method) {
            context.method = method;
            return this;
        }
        
        public Builder uri(String uri) {
            context.uri = uri;
            return this;
        }
        
        public RateLimitContext build() {
            return context;
        }
    }
    
    // Getters
    public String getKey() {
        return key;
    }
    
    public RateLimitType getType() {
        return type;
    }
    
    public RateLimitScope getScope() {
        return scope;
    }
    
    public long getWindow() {
        return window;
    }
    
    public long getLimit() {
        return limit;
    }
    
    public long getCapacity() {
        return capacity;
    }
    
    public double getRate() {
        return rate;
    }
    
    public String getMessage() {
        return message;
    }
    
    public long getCurrentTime() {
        return currentTime;
    }
    
    public String getClientIp() {
        return clientIp;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getUri() {
        return uri;
    }
    
    // Setters
    public void setKey(String key) {
        this.key = key;
    }
    
    public void setType(RateLimitType type) {
        this.type = type;
    }
    
    public void setScope(RateLimitScope scope) {
        this.scope = scope;
    }
    
    public void setWindow(long window) {
        this.window = window;
    }
    
    public void setLimit(long limit) {
        this.limit = limit;
    }
    
    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }
    
    public void setRate(double rate) {
        this.rate = rate;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }
    
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }

}