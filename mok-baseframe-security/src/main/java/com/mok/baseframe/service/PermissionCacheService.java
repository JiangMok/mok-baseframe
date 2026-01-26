package com.mok.baseframe.service;

import java.util.List;

/**
 * @description: 权限缓存service接口
 * @author: JN
 * @date: 2026/1/5 14:43
 * @param:
 * @return:
 **/
public interface PermissionCacheService {


    /**
     * @description: 获取用户权限 >> 带缓存
     * @author: JN
     * @date: 2026/1/5 14:44
     * @param: [userId]
     * @return: java.util.List<java.lang.String>
     **/
    List<String> getPermissionsByUserIdWithCache(String userId,String source);

    /**
     * @description: 清除某一用户权限和菜单缓存
     * @author: JN
     * @date: 2026/1/5 14:45
     * @param: [userId]
     * @return: void
     **/
    void clearUserPermissionCache(String userId);

    /**
     * @description: 清除所有用户和菜单缓存
     * @author: JN
     * @date: 2026/1/5 14:45
     * @param: []
     * @return: void
     **/
    void clearAllPermissionCache();


}