package com.mok.baseframe.task;

import com.mok.baseframe.monitor.service.HealthCheckService;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @description: 定时健康检查
 * @author: JN
 * @date: 2026/1/6 14:57
 * @param:
 * @return:
 **/

@Component
public class HealthCheckTask {
    private static final Logger log = LogUtils.getLogger(HealthCheckTask.class);

    private final HealthCheckService healthCheckService;

    public HealthCheckTask(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    /**
     * 每分钟执行一次健康检查
     */
    @Scheduled(fixedRate = 60000) // 60秒 60*1000
    public void scheduledHealthCheck() {
        try {
            Map<String, Object> health = healthCheckService.performHealthCheck();
            String status = (String) health.get("status");

            if ("DOWN".equals(status)) {
                log.error("❌ 系统健康检查失败: {}", health);
                // 可以发送告警邮件、钉钉消息等
            } else if ("WARNING".equals(status)) {
                log.warn("⚠️ 系统健康检查警告: {}", health);
            } else {
                log.debug("✅ 系统健康检查正常: {}", health);
            }
        } catch (Exception e) {
            log.error("健康检查任务执行失败", e);
        }
    }
}