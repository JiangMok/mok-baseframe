package com.mok.baseframe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mok.baseframe.dao.PermissionMapper;
import com.mok.baseframe.entity.PermissionEntity;
import com.mok.baseframe.entity.UserEntity;
import com.mok.baseframe.service.PermissionCacheService;
import com.mok.baseframe.service.SecurityUserService;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description: 权限缓存 service 接口实现类
 * @author: JN
 * @date: 2026/1/5
 */

@Service
public class PermissionCacheServiceImpl implements PermissionCacheService {
    private static final Logger log = LogUtils.getLogger(PermissionCacheServiceImpl.class);

    //用户权限缓存 key
    private static final String USER_PERMISSION_KEY = "security:user:permissions:%s";
    //用户菜单缓存 key
    private static final String USER_MENU_KEY = "security:user:menus:%s";
    //缓存过期时间 : 30分钟
    private static final long CACHE_EXPIRE = 30;
    //用户存在的 key
    private static final String USER_EXISTS_KEY = "security:user:exists:%s";
    // 空值标记
    private static final String NULL_VALUE = "NULL";
    // 空值缓存时间（较短）
    private static final long NULL_CACHE_EXPIRE = 5;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityUserService securityUserService;
    private final PermissionMapper permissionMapper;

    public PermissionCacheServiceImpl(RedisTemplate<String, Object> redisTemplate,
                                      PermissionMapper permissionMapper,
                                      SecurityUserService securityUserService) {
        this.redisTemplate = redisTemplate;
        this.permissionMapper = permissionMapper;
        this.securityUserService = securityUserService;
    }

    @Override
    public List<String> getPermissionsByUserIdWithCache(String userId,String source) {
        log.debug("数据来源:{}",source);
        // 1. 检查用户是否存在（本地内存缓存）
        Boolean userExists = checkUserExists(userId);
        if (Boolean.FALSE.equals(userExists)) {
            return Collections.emptyList();
        }

        // 2. 检查Redis缓存
        String permissionKey = String.format(USER_PERMISSION_KEY, userId);
        Object cached = redisTemplate.opsForValue().get(permissionKey);

        // 2.1 如果是空值标记
        if (NULL_VALUE.equals(cached)) {
            log.debug("用户{}不存在（空值缓存）", userId);
            return Collections.emptyList();
        }
        // 2.2 如果是正常数据
        if (cached instanceof List) {
            List<String> permissions = (List<String>) cached;
            log.debug("从缓存获取用户{}权限，数量：{}", userId, permissions.size());
            return permissions;
        }
        // 3. 查询数据库
        log.debug("缓存中没有权限数据,开始查询数据库......");
        List<PermissionEntity> permissionsList = permissionMapper.selectPermissionsByUserId(userId);
        List<String> permissions = new ArrayList<>();
        if (permissionsList != null && !permissionsList.isEmpty()) {
            for (PermissionEntity permissionEntity : permissionsList) {
                permissions.add(permissionEntity.getPermissionCode());
            }
        }
        // 4. 处理结果
        if (permissions.isEmpty()) {
            // 用户不存在或没有权限
            log.debug("用户{}没有任何权限",userId);
            cacheNullValue(permissionKey);
            return Collections.emptyList();
        } else {
            log.debug("开始缓存用户权限");
            redisTemplate.opsForValue().set(
                    permissionKey,
                    permissions,
                    CACHE_EXPIRE,
                    TimeUnit.MINUTES
            );
            log.debug("用户{}权限已缓存，数量：{}", userId, permissions.size());
        }
        return permissions;

    }

    @Override
    public void clearUserPermissionCache(String userId) {
        // 根据用户 ID 生成权限缓存 key
        String permissionKey = String.format(USER_PERMISSION_KEY, userId);
        // 根据用户 ID 生成菜单缓存key
        String menuKey = String.format(USER_MENU_KEY, userId);
        // 删除 Redis 中的权限缓存
        redisTemplate.delete(permissionKey);
        // 删除 Redis 中的菜单缓存
        redisTemplate.delete(menuKey);
        // 记录调试日志
        log.debug("已清除用户{}权限缓存", userId);
    }

    @Override
    public void clearAllPermissionCache() {
        // 定义缓存key的模式，匹配所有用户权限和菜单缓存
        String pattern = "security:user:*";
        // keys(pattern)获取所有匹配模式的key
        // delete(keys)批量删除这些key
        // 注意：keys操作在生产环境中可能影响性能，大数据量时建议使用scan命令
        redisTemplate.delete(redisTemplate.keys(pattern));

        // 记录调试日志
        log.debug("已清除所有用户权限缓存");
    }

    /**
     * @description: 检查用户是否存在（带缓存）
     * @author: JN
     * @date: 2026/1/5 15:22
     * @param: [userId]
     * @return: java.lang.Boolean
     **/
    private Boolean checkUserExists(String userId) {
        String existsKey = String.format(USER_EXISTS_KEY, userId);
        Object cached = redisTemplate.opsForValue().get(existsKey);
        if (cached != null) {
            log.info("用户{}存在于缓存中",userId);
            return Boolean.valueOf(cached.toString());
        }
        // 查询数据库
        log.info("用户{}不存在于缓存中,开始查询数据库",userId);
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getId, userId);
        boolean exists = securityUserService.exists(wrapper);
        log.info(exists ? "用户存在数据库中":"用户不存在");
        // 缓存结果（用户不存在也缓存，但时间较短）
        // 存在缓存30分钟，不存在缓存5分钟
        long expireTime = exists ? 30 : 5;
        redisTemplate.opsForValue().set(
                existsKey,
                String.valueOf(exists),
                expireTime,
                TimeUnit.MINUTES
        );
        return exists;
    }

    /**
     * @description: 缓存空值
     * @author: JN
     * @date: 2026/1/5 15:29
     * @param: [key]
     * @return: void
     **/
    private void cacheNullValue(String key) {
        redisTemplate.opsForValue().set(
                key,
                NULL_VALUE,
                NULL_CACHE_EXPIRE,
                TimeUnit.MINUTES
        );
    }
}
