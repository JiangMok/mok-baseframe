package com.mok.baseframe.security.config;

import com.mok.baseframe.security.filter.JwtAuthenticationFilter;
import com.mok.baseframe.security.handler.SecurityExceptionHandler;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * @description: Security配置类 >>> 安全配置中心  创建顺序:3
 * @author: JN
 * @date: 2025/12/31
 */
//声明这是一个spring配置类
@Configuration
//启用spring Security的web安全支持
@EnableWebSecurity
//启用方法级别的安全控制
@EnableMethodSecurity
//lombok注解,为final字段生成构造器

public class SecurityConfig {

    //自定义JWT认证过滤器
    //  作用:这个过滤器会验证每个请求中的JWT令牌
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityExceptionHandler securityExceptionHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          SecurityExceptionHandler securityExceptionHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityExceptionHandler = securityExceptionHandler;

    }

    /**
     * @description: 核心配置:安全过滤器链
     * @author: JN
     * @date: 2025/12/31 20:12
     * @param: [http, corsConfigurationSource]
     * @return: org.springframework.security.web.SecurityFilterChain
     **/
    //@Bean注解:声明这是一个spring bean
    //使用@Bean注解可以非常方便地注册自定义的Bean到Spring的IoC容器中
    //SecurityFilterChain : 安全过滤器链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        //开始配置HttpSecurity对象
        //  这个对象使用了建造者模式,支持链式调用
        http
                //禁用CSFR(跨站请求伪造)保护
                // 为什么:RESTful API使用无状态认证,不需要CDFR保护
                //      如果使用session-cookie认证,则不能禁用CSFR
                .csrf(AbstractHttpConfigurer::disable)
                //配置CORS(跨域资源共享)
                //  方法:cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //  参数:lambda表达式,指定CORS配置源
                //  为什么:前后端分离时,前端和后端通常不在同一域名下、端口下,需要CROS
                .cors(cord -> cord.configurationSource(corsConfigurationSource()))
                //配置URL访问权限
                //  作用:配置哪些URL需要认证,哪些些不需要
                .authorizeHttpRequests(auth -> auth
                        //公开接口:不需要认证就可以访问
                        //  方法:requestMatchers().permitAll()
                        //  作用:指定某些URL模式为公开访问
                        .requestMatchers(
                                "/test/**",           // 测试接口
                                "/debug/**",          // 调试接口
                                "/auth/login",        // 登录接口
                                "/auth/logout",       // 退出登录
                                "/auth/refresh",      // 刷新token
                                "/captcha/**",        // 验证码接口
                                "/captcha/generate",        // 验证码接口

                                // Swagger 相关路径 - 全部放行
                                "/swagger-ui/**",     // Swagger UI
                                "/v3/api-docs/**",    // OpenAPI 文档
                                "/swagger-ui.html",   // Swagger UI HTML
                                "/swagger-resources/**",  // Swagger 资源
                                "/webjars/**",        // WebJars
                                "/swagger/**",        // Swagger
                                "/doc.html",          // Knife4j
                                "/favicon.ico",       // 网站图标

                                // 静态资源
                                "/uploads/**",         // 静态资源
                                "/static/**",         // 静态资源
                                "/resources/**",      // 资源文件
                                "/css/**",            // CSS
                                "/js/**",             // JavaScript
                                "/images/**",         // 图片

                                // 错误页面
                                "/error",             // 错误处理
                                "/error/**"           // 错误处理
                        ).permitAll()//允许所有用户访问(不需要认证)
                        //其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                // 配置异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(securityExceptionHandler)   // 认证失败处理器
                        .accessDeniedHandler(securityExceptionHandler)        // 授权失败处理器
                )
                //配置会话管理为无状态
                .sessionManagement(session -> session
                        //设置会话创建策略为无状态
                        //  作用:Spring Security不会创建和使用HttpSession
                        //  为什么:RESTful API通常是无状态的,使用JWT进行认证
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //添加JWT认证过滤器
                //  作用:在指定的过滤器之前添加自定义过滤器
                //  参数1:我们自定义的JWT过滤器:jwtAuthenticationFilter
                //  参数2:Spring Security默认的用户名和密码认证过滤器:UsernamePasswordAuthenticationFilter.class
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        //构建并返回SecurityFilterChain
        return http.build();
    }

    /**
     * @description: 跨域资源共享配置
     * 作用:定义CROS策略,允许那些 域、方法、头 等
     * @author: JN
     * @date: 2025/12/31 20:28
     * @param: []
     * @return: org.springframework.web.cors.CorsConfigurationSource
     **/
    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        //创建CROS配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        //设置允许的来源(域名)
        //  Arrays.asList("*"):代表允许所有域名
        //  为什么:开发阶段方便,生产环境应该至指定具体域名
        configuration.setAllowedOrigins(Arrays.asList("*"));
        //设置允许的HTTP方法
        //  参数：GET, POST, PUT, DELETE, OPTIONS
        //  为什么:覆盖RESTful api的常用方法,OPTIONS方法用于预检请求
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        //设置允许的请求头
        //  Arrays.asList("*"):代表允许所有头
        //  为什么:简化开发,生产环境可以限制为必要的头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        //设置暴露的响应头
        //  作用:允许前端访问请求头中的Authorization字段
        //  为什么:前端可能需要从响应头中获取新的token
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        //创建基于URL的CORS配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //注册CORS配置,应用于所有路径
        //  参数1:"/**"   匹配所有URL路径
        //  参数2:configuration   上面配置的CORS规则
        source.registerCorsConfiguration("/**", configuration);
        //返回配置源
        return source;
    }

    /**
     * @description: 配置密码编码器Bean
     * 作用:用于加密用户密码
     * @author: JN
     * @date: 2025/12/31 20:37
     * @param: []
     * @return: org.springframework.security.crypto.password.PasswordEncoder
     **/
    @Bean
    public PasswordEncoder passwordEncoder() {
        //返回BCrypt密码编码器实例
        //  为什么:BCrypt是目前最安全的密码编码器之一,自动加盐,不可逆
        return new BCryptPasswordEncoder();
//        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * @description: 配置认证管理器
     * 作用: Spring Security的核心,负责处理认证请求
     * 参数: AuthenticationConfiguration,Spring自动注入的认证配置
     * @author: JN
     * @date: 2025/12/31 20:38
     * @param: [authenticationConfiguration]
     * @return: org.springframework.security.authentication.AuthenticationManager
     **/
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        //从认证配置中获取认证管理器
        return authenticationConfiguration.getAuthenticationManager();
    }


}














