package com.mok.baseframe.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mok.baseframe.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @description: 安全用户实体类 自定义的UserDetails实现   创建顺序:6
 * 作用 : 实现 Spring Security 的 UserDetails 接口,封装用户信息和权限
 * 这个类会被 Spring Security 用于认证和授权
 * @author: JN
 * @date: 2026/1/2
 */
public class SecurityUser implements UserDetails {

    //用户 id 字段,用户唯一标识
    private String userId;
    //用户名
    private String username;
    //用户昵称
    private String nickname;
    //密码字段
    private String password;
    //状态字段
    private Integer status;
    //权限列表字段
    //  存储用户的权限列表,如"user:add","user:delete"等
    private List<String> permissions;

    private UserEntity userEntity;

    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    // 构造方法
    public SecurityUser() {
    }

    public SecurityUser(String userId, String username, String nickname,
                        String password, Integer status, List<String> permissions,UserEntity userEntity) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.status = status;
        this.permissions = permissions;
        this.userEntity = userEntity;
    }

    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String username;
        private String nickname;
        private String password;
        private Integer status;
        private UserEntity userEntity;
        private List<String> permissions;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        public Builder userEntity(UserEntity userEntity) {
            this.userEntity = userEntity;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder permissions(List<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public SecurityUser build() {
            return new SecurityUser(userId, username, nickname, password, status, permissions,userEntity);
        }
    }

    // Getter 和 Setter 方法
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    /**
     * @description: 获取用户权限的方法
     * --作用 : 实现 UserDetails 接口的 getAuthorities 方法
     * --返回 : Collection<? extends GrantedAuthority> - 权限合集
     * @author: JN
     * @date: 2026/1/2 12:52
     * @param: []
     * @return: java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>
     **/
    //@JsonIgnore注解 : 序列化时忽略此方法
    //  因为权限信息不应该在 JSON 响应中返回
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 确保不返回null
        if (permissions == null) {
            return Collections.emptyList();
        }

        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    /**
     * @description: 账户是否过期
     * --作用 : 实现 UserDetails 接口的 isAccountNonExpired方法
     * --默认返回 UserDetails.super.isAccountNonExpired() : true >>> 表示用户永不过期
     * --如果需要账户过期功能,可以在数据库中增加过期时间字段
     * @author: JN
     * @date: 2026/1/2 12:58
     * @param: []
     * @return: boolean
     **/
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * @description: 账户是否锁定
     * --作用 : 实现 UserDetails 接口的 isAccountNonLocked 方法
     * --返回 UserDetails.super.isAccountNonLocked():true >>> 表示账户永不锁定
     * --如果需要账户锁定功能,可以在数据库中增加锁定状态字段
     * @author: JN
     * @date: 2026/1/2 13:00
     * @param: []
     * @return: boolean
     **/
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * @description: 凭证是否未过期的方法
     * --作用 : 实现 UserDetails 接口的 isCredentialsNonExpired 方法
     * --返回 UserDetails.super.isCredentialsNonExpired():true >>> 表示凭证永不过期
     * --如果需要账密码过期功能,可以在数据库中增加密码修改时间字段
     * @author: JN
     * @date: 2026/1/2 13:03
     * @param: []
     * @return: boolean
     **/
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * @description: 账户是否启用的方法
     * --作用 : 实现UserDetails接口的isEnabled方法
     * @author: JN
     * @date: 2026/1/2 13:05
     * @param: []
     * @return: boolean
     **/
    @Override
    public boolean isEnabled() {
        //这里根据 status 字段判断,1=启用,其他表示禁用
        return status != null && status == 1;
    }

    // equals 方法
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SecurityUser that = (SecurityUser) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(username, that.username) &&
                Objects.equals(nickname, that.nickname) &&
                Objects.equals(password, that.password) &&
                Objects.equals(status, that.status) &&
                Objects.equals(userEntity, that.userEntity) &&
                Objects.equals(permissions, that.permissions);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return Objects.hash(userId, username, nickname, password, status, permissions,userEntity);
    }

    // toString 方法（注意：这里不打印password，因为它是敏感信息）
    @Override
    public String toString() {
        return "SecurityUser{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", userEntity='" + userEntity + '\'' +
                ", password='[PROTECTED]'" +
                ", status=" + status +
                ", permissions=" + permissions +
                '}';
    }
}