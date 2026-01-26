package com.mok.baseframe;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 启动器
 * @author: JN
 * @date: 2026/1/7 12:02
 * @param:
 * @return:
 **/
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
        "com.mok.baseframe.*",
        "com.mok.baseframe.base.controller",
        "com.mok.baseframe.core.aspect"
})
@MapperScan("com.mok.baseframe.dao")
public class MokBaseframeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MokBaseframeApplication.class, args);
    }

}
