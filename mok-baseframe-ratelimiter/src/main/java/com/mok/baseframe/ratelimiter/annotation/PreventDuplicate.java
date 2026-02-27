package com.mok.baseframe.ratelimiter.annotation;

import java.lang.annotation.*;

/**
 * @description: 防重复提交注解类 
 * @author: mok
 * @date: 2026/2/26 20:03
 * @param: 
 * @return: 
**/
//指定该注解只能用于方法上
@Target(ElementType.METHOD)
//注解保留到运行时,便于通过反射读取
@Retention(RetentionPolicy.RUNTIME)
//生成 java 文档是包含该注解
@Documented
public @interface PreventDuplicate {

    /**
     * 防重复提交的Key，支持SpEL表达式
     * 例如：#user.id + '-' + #request.type
     * 作用:自定义唯一标识,用于区分不同的提交场景
     */
    String key() default "";

    /**
     * 锁定的时间（秒）
     * 作用:在该时间段内,相同的key不允许再次提交
     */
    int lockTime() default 3;

    /**
     * 提示信息
     */
    String message() default "";

    /**
     * 是否检查请求参数，默认检查
     * 作用：若为true，则会将请求参数纳入key的生成，避免相同参数重复提交
     */
    boolean checkParams() default true;
}