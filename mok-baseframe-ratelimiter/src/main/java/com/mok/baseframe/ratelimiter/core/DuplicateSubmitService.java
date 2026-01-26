package com.mok.baseframe.ratelimiter.core;

/**
 * 防重复提交服务接口
 */
public interface DuplicateSubmitService {
    
    /**
     * 检查是否允许提交
     */
    boolean isAllowed(String key, int lockTime);
    
    /**
     * 检查是否允许提交（自定义错误信息）
     */
    boolean isAllowed(String key, int lockTime, String message);
    
    /**
     * 释放提交锁
     */
    void release(String key);
    
    /**
     * 检查并锁定（如果允许）
     */
    boolean checkAndLock(String key, int lockTime);
}