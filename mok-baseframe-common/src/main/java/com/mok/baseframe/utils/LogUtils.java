package com.mok.baseframe.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author aha13
 */
public class LogUtils  {
       private static final Logger log = LogUtils.getLogger(LogUtils.class);
    
    // 快速获取Logger
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    // 日志级别判断
    public static boolean isTraceEnabled(Class<?> clazz) {
        return getLogger(clazz).isTraceEnabled();
    }
    
    public static boolean isDebugEnabled(Class<?> clazz) {
        return getLogger(clazz).isDebugEnabled();
    }
    
    public static boolean isInfoEnabled(Class<?> clazz) {
        return getLogger(clazz).isInfoEnabled();
    }
    
    public static boolean isWarnEnabled(Class<?> clazz) {
        return getLogger(clazz).isWarnEnabled();
    }
    
    public static boolean isErrorEnabled(Class<?> clazz) {
        return getLogger(clazz).isErrorEnabled();
    }
}