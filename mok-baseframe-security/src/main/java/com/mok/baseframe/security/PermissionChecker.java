package com.mok.baseframe.security;

import com.mok.baseframe.service.PermissionCacheService;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description: 权限检查器，用于自定义权限校验
 * @author: JN
 * @date: 2026/1/5 18:27
 * @param:
 * @return:
 **/
@Component("permissionChecker")
public class PermissionChecker {
    private static final Logger log = LogUtils.getLogger(PermissionChecker.class);
    private final PermissionCacheService permissionCacheService;

    public PermissionChecker(PermissionCacheService permissionCacheService) {
        this.permissionCacheService = permissionCacheService;
    }


    /**
     * 检查用户是否有指定权限
     */
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof SecurityUser securityUser)) {
            return false;
        }
        // 关键：直接从SecurityUser获取用户ID
        String userId = securityUser.getUserId();
        String userName = securityUser.getUsername();
        // 从缓存获取用户权限
        List<String> permissions =
                permissionCacheService.getPermissionsByUserIdWithCache(userId, "权限检查");
        boolean checkResult = permissions != null && permissions.contains(permission);
        log.info("权限检查:用户:{}使用了{}权限,{}", userName, permission, checkResult ? "通过" : "禁止");
        return checkResult;
    }
}