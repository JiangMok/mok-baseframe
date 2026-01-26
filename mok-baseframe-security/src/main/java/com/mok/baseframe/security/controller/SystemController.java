package com.mok.baseframe.security.controller;

import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.common.R;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.monitor.service.HealthCheckService;
import com.mok.baseframe.service.PermissionCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.aop.support.AopUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description: 系统controller
 * @author: JN
 * @date: 2026/1/5 17:33
 * @param:
 * @return:
 **/

@RestController
@RequestMapping("/system")

@Tag(name = "系统管理", description = "系统相关接口")
public class SystemController {

    private final PermissionCacheService permissionCacheService;

    private final HealthCheckService healthCheckService;

    public SystemController(PermissionCacheService permissionCacheService,
                            HealthCheckService healthCheckService) {
        this.permissionCacheService = permissionCacheService;
        this.healthCheckService = healthCheckService;
    }

    /**
     * @description: 清除权限缓存
     * @author: JN
     * @date: 2026/1/6 14:39
     * @param: [userId]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "清除权限缓存")
    @OperationLog(title = "清除权限缓存", businessType = BusinessType.CLEAN)
    @PostMapping("/cache/clear-permissions")
    @PreAuthorize("@permissionChecker.hasPermission('system:cache:clear')")
    public R<String> clearPermissionCache(
            @Parameter(description = "用户ID，为空则清除所有用户缓存")
            @RequestParam(name = "userId", required = false) String userId) {
// 测试代码
        System.out.println("清除权限缓存 - 当前类: " + this.getClass().getName());
        System.out.println("清除权限缓存 - 是否是AOP代理: " + AopUtils.isAopProxy(this));
        if (userId != null) {
            permissionCacheService.clearUserPermissionCache(userId);
            return R.ok("已清除用户权限缓存");
        } else {
            permissionCacheService.clearAllPermissionCache();
            return R.ok("已清除所有权限缓存");
        }
    }

    /**
     * @description: 获取系统信息
     * @author: JN
     * @date: 2026/1/6 14:40
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Operation(summary = "获取系统信息")
    @OperationLog(title = "获取系统信息", businessType = BusinessType.QUERY)
    @GetMapping("/info")
//    @PreAuthorize("@permissionChecker.hasPermission('system:info:query')")
    public R<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = Map.of(
                "appName", "MOK-BaseFrame",
                "version", "1.0.0",
                "javaVersion", System.getProperty("java.version"),
                "osName", System.getProperty("os.name"),
                "osArch", System.getProperty("os.arch"),
                "userHome", System.getProperty("user.home"),
                "timestamp", System.currentTimeMillis(),
                "upTime", getUptime()
        );

        return R.ok(info);
    }

    /**
     * @description: 健康检查
     * @author: JN
     * @date: 2026/1/6 14:55
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Operation(summary = "健康检查")
    @GetMapping("/health")
    @OperationLog(title = "系统监控", businessType = BusinessType.QUERY, saveResponseData = false)
    public R<Map<String, Object>> healthCheck() {
        Map<String, Object> health = healthCheckService.performHealthCheck();
        return R.ok(health);
    }


    /**
     * @description: 获取应用运行时间
     * @author: JN
     * @date: 2026/1/6 14:55
     * @param: []
     * @return: java.lang.String
     **/
    private String getUptime() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        return formatUptime(uptimeMillis);
    }

    /**
     * @description: 格式化运行时间
     * @author: JN
     * @date: 2026/1/6 14:55
     * @param: [millis]
     * @return: java.lang.String
     **/
    private String formatUptime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天 ");
        }
        if (hours > 0) {
            sb.append(hours).append("小时 ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟 ");
        }
        sb.append(seconds).append("秒");

        return sb.toString();
    }
}