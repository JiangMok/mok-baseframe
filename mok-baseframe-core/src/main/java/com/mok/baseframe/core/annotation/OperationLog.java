package com.mok.baseframe.core.annotation;

import com.mok.baseframe.enums.BusinessType;

import java.lang.annotation.*;

/**
 * @description: 操作日志注解
 * @author: JN
 * @date: 2026/1/5 18:13
 * @param:
 * @return:
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    //模块
    String title() default "";

    //业务类型
    BusinessType businessType() default BusinessType.OTHER;

    //是否保存请求参数
    boolean saveRequestParam() default true;

    //是否保存响应参数
    boolean saveResponseData() default true;
}