package com.mok.baseframe.order.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 订单模块配置类
 */
@Configuration
@ComponentScan(basePackages = "com.mok.baseframe.order")
@EnableScheduling
public class OrderModuleConfig {
}
