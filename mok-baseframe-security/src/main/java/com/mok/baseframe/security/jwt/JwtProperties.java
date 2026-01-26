package com.mok.baseframe.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import com.mok.baseframe.utils.LogUtils;

/**
 * @description: JWT配置属性类   创建顺序:1
 * @author: JN
 * @date: 2025/12/31
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private static final Logger log = LogUtils.getLogger(JwtProperties.class);

    // JWT密钥:用于签名和验证JWT.注意:默认值是示例密钥,根据情况修改
    private String secret = "security-framework-secret-key-2024-01-01-change-in-production";

    // 普通token过期时间
    private Long tokenExpire = 7200000L;

    // 刷新token过期时间
    private Long refreshTokenExpire = 604800000L;

    // HTTP请求中携带token的字段名,默认是"Authorization"
    private String header = "Authorization";

    // token前缀,用于Bearer Token认证模式,默认是"Bearer",客户端发送的格式应为:"Bearer{token}"
    private String prefix = "Bearer";

    // Getter 和 Setter 方法
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getTokenExpire() {
        return tokenExpire;
    }

    public void setTokenExpire(Long tokenExpire) {
        this.tokenExpire = tokenExpire;
    }

    public Long getRefreshTokenExpire() {
        return refreshTokenExpire;
    }

    public void setRefreshTokenExpire(Long refreshTokenExpire) {
        this.refreshTokenExpire = refreshTokenExpire;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
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
        JwtProperties that = (JwtProperties) o;
        return java.util.Objects.equals(secret, that.secret) &&
                java.util.Objects.equals(tokenExpire, that.tokenExpire) &&
                java.util.Objects.equals(refreshTokenExpire, that.refreshTokenExpire) &&
                java.util.Objects.equals(header, that.header) &&
                java.util.Objects.equals(prefix, that.prefix);
    }

    // hashCode 方法
    @Override
    public int hashCode() {
        return java.util.Objects.hash(secret, tokenExpire, refreshTokenExpire, header, prefix);
    }

    // toString 方法（注意：这里不打印secret，因为它是敏感信息）
    @Override
    public String toString() {
        return "JwtProperties{" +
                "secret='[PROTECTED]'" +
                ", tokenExpire=" + tokenExpire +
                ", refreshTokenExpire=" + refreshTokenExpire +
                ", header='" + header + '\'' +
                ", prefix='" + prefix + '\'' +
                '}';
    }
}