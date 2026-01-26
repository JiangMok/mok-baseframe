package com.mok.baseframe.utils;

import com.mok.baseframe.entity.UserEntity;
import com.mok.baseframe.security.SecurityUser;
import com.mok.baseframe.service.SecurityUserService;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @description: 安全工具类
 * @author: JN
 * @date: 2026/1/7
 */
@Component

public class SecurityUtils {
    private static final Logger log = LogUtils.getLogger(SecurityUtils.class);
    private final SecurityUserService userService;

    public SecurityUtils(SecurityUserService userService) {
        this.userService = userService;
    }

    /**
     * @description: 获取当前登录用户对象
     * @author: JN
     * @date: 2026/1/7
     * @param: []
     * @return: com.mok.securityframework.entity.User
     **/
    public UserEntity getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("认证失败");
                return null;
            }

            // 修改：直接从Authentication的principal中获取用户信息
            Object principal = authentication.getPrincipal();
            if (principal instanceof SecurityUser) {
                // 修改：直接返回SecurityUser中存储的UserEntity
                log.debug("获取到 SecurityUser 中存储的用户对象");
                return ((SecurityUser) principal).getUserEntity();
            }

            // 修改：如果principal不是SecurityUser类型，可能是其他情况
            // 可以添加日志记录或者直接返回null
            String username = authentication.getName();
            if (username == null || "anonymousUser".equals(username)) {
                return null;
            }

            // 修改：只有在缓存中没有用户信息时才查询数据库
            // 这里可以添加缓存逻辑，但最好是第一种方式
            log.debug("通过查询数据库来获取当前登录的用户对象");
            return userService.lambdaQuery()
                    .eq(UserEntity::getUsername, username)
                    .eq(UserEntity::getIsDeleted, 0)
                    .one();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @description: 获取当前登录用户ID
     * @author: JN
     * @date: 2026/1/7
     * @param: []
     * @return: java.lang.String
     **/
    public String getCurrentUserId() {
        UserEntity userEntity = getCurrentUser();
        return userEntity != null ? userEntity.getId() : null;
    }

    /**
     * @description: 获取当前登录用户名
     * @author: JN
     * @date: 2026/1/7
     * @param: []
     * @return: java.lang.String
     **/
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * @description: 判断当前用户是否是超级管理员（admin）
     * @author: JN
     * @date: 2026/1/7
     * @param: []
     * @return: boolean
     **/
    public boolean isSuperAdmin() {
        UserEntity userEntity = getCurrentUser();
        return userEntity != null && "admin".equals(userEntity.getUsername());
    }

    /**
     * @description: 判断当前用户是否已认证
     * @author: JN
     * @date: 2026/1/7
     * @param: []
     * @return: boolean
     **/
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}