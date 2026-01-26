package com.mok.baseframe.security.userDetailsServiceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.baseframe.dao.UserMapper;
import com.mok.baseframe.entity.UserEntity;
import com.mok.baseframe.security.SecurityUser;
import com.mok.baseframe.service.PermissionCacheService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description: Spring Security 认证类 >>> 用户信息加载器
 * @author: JN
 * @date: 2026/1/6
 */

@Service
public class CustomUserDetailsServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements UserDetailsService {

    //    private final SecurityUserService securityUserService;
    private final PermissionCacheService permissionCacheService;

    public CustomUserDetailsServiceImpl(
//            SecurityUserService securityUserService,
            PermissionCacheService permissionCacheService) {
//        this.securityUserService = securityUserService;
        this.permissionCacheService = permissionCacheService;
    }

    /**
     * @description: 通过User.username查询USER
     * 实现 Spring Security 的 UserDetailsService 接口方法
     * 作用: 根据用户名加载用户详情,用于 Spring Security 认证
     * @author: JN
     * @date: 2025/12/31 21:15
     * @param: [username]
     * @return: org.springframework.security.core.userdetails.UserDetails
     **/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //查询用户信息
        //  lambdaQuery()：MyBatis Plus提供的链式查询方法
        UserEntity userEntity = lambdaQuery()
                //查询用户名等于username的记录
                .eq(UserEntity::getUsername, username)
                //查询未删除的用户
                .eq(UserEntity::getIsDeleted, 0)
                //查询单条记录
                .one();
        if (userEntity == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        if (userEntity.getStatus() == 0) {
            //可根据相对应的异常情况,自定义异常:继承AuthenticationException类
            throw new UsernameNotFoundException("用户已被禁用");
        }
        // 构建SecurityUser
        SecurityUser securityUser = new SecurityUser();
        securityUser.setUserId(userEntity.getId());
        securityUser.setUsername(userEntity.getUsername());
        securityUser.setNickname(userEntity.getNickname());
        securityUser.setPassword(userEntity.getPassword());
        securityUser.setStatus(userEntity.getStatus());
        securityUser.setUserEntity(userEntity);
        return securityUser;
    }
}
