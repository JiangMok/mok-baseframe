package com.mok.baseframe.ratelimiter.annotation;

import java.lang.annotation.*;

/*
 *防重复提交
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventDuplicate {

    /**
     * 防重复提交的Key，支持SpEL表达式
     * 例如：#user.id + '-' + #request.type
     */
    String key() default "";

    /**
     * 锁定的时间（秒）
     */
    int lockTime() default 3;

    /**
     * 提示信息
     */
    String message() default "";

    /**
     * 是否检查请求参数，默认检查
     */
    boolean checkParams() default true;
}