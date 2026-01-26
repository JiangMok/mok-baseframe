package com.mok.baseframe.core.config;

import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final Logger log = LogUtils.getLogger(WebConfig.class);

    private final FileStorageConfig fileStorageConfig;

    public WebConfig(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到本地文件系统路径
        // 注意：这里映射到 /api/uploads，因为 server.servlet.context-path=/api
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + fileStorageConfig.getBasePath() + "/")
                .setCachePeriod(3600); // 设置缓存时间

        // 也可以映射其他静态资源路径
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}