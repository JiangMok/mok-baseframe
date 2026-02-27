package com.mok.baseframe.security.controller;

import com.mok.baseframe.captcha.service.CaptchaService;
import com.mok.baseframe.common.R;
import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.dto.LoginRequest;
import com.mok.baseframe.dto.LoginResponse;
import com.mok.baseframe.entity.UserEntity;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.ratelimiter.annotation.RateLimit;
import com.mok.baseframe.ratelimiter.enums.RateLimitScope;
import com.mok.baseframe.ratelimiter.enums.RateLimitType;
import com.mok.baseframe.security.jwt.JwtProperties;
import com.mok.baseframe.security.jwt.JwtTokenProvider;
import com.mok.baseframe.security.jwt.TokenBlacklistService;
import com.mok.baseframe.service.PermissionCacheService;
import com.mok.baseframe.service.SecurityUserService;
import com.mok.baseframe.utils.LogUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 登录授权控制器
 * @author: JN
 * @date: 2026/1/1
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "认证管理相关接口")
public class AuthController {
    private static final Logger log = LogUtils.getLogger(AuthController.class);

    //注入认证管理器
    //  作用 : 用于认证用户名和密码
    //  AuthenticationManager,spring security 核心组件
    private final AuthenticationManager authenticationManager;

    //注入 JWT 令牌提供者
    //  作用 : 生成、验证、解析 JWT 令牌
    private final JwtTokenProvider jwtTokenProvider;

    // 在 AuthController 中注入 CaptchaService
    private final CaptchaService captchaService;

    private final SecurityUserService securityUserService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PermissionCacheService permissionCacheService;
    private final JwtProperties jwtProperties;

    // 构造函数注入
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          CaptchaService captchaService,
                          SecurityUserService securityUserService,
                          TokenBlacklistService tokenBlacklistService,
                          PermissionCacheService permissionCacheService,
                          JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.captchaService = captchaService;
        this.securityUserService = securityUserService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.permissionCacheService = permissionCacheService;
        this.jwtProperties = jwtProperties;
    }

    /**
     * @description: 用户登录接口
     * @author: JN
     * @date: 2026/1/1 14:21
     * @param: [loginRequest]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.dto.LoginResponse>
     **/
    @RateLimit(
            type = RateLimitType.SLIDING_WINDOW,
            scope = RateLimitScope.IP,
            window = 60,
            limit = 5,
            message = "您操作过于频繁"
    )
    @Operation(summary = "登录")
    @PostMapping("/login")
    @OperationLog(title = "用户登录", businessType = BusinessType.LOGIN)
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        boolean isValid = captchaService.validateCaptcha(loginRequest.getCaptchaKey(), loginRequest.getCaptcha());
        if (!isValid) {
            return R.error(1002, "验证码错误或已过期");
        }
        //认证用户名和密码
        //  使用 Spring Security 的 AuthenticationManager 进行认证
        //  如果认证失败,会抛出 AuthenticationException
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        //设置认证信息到安全上下文
        //  作用 : 将认证信息存储到当前线程的安全上下文中
        //  后续代码可以通过 SecurityContextHolder 获取当前用户
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //通过mybatis-plus的lambdaQyery()方法,构建出查询条件,在查询并返回
        UserEntity userEntity = securityUserService.lambdaQuery().
                eq(UserEntity::getUsername, loginRequest.getUsername())
                .one();

        //生成token
        //  创建 claims(声明),可以存储额外的用户信息
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userEntity.getId());
        claims.put("nickname", userEntity.getNickname());

        //生成访问令牌
        //  使用 JwtTokenProvider 生成 JWT 令牌
        String token = jwtTokenProvider.generateToken(loginRequest.getUsername(), claims);

        //刷新访问令牌
        //  刷新令牌用于访问令牌过期后获取新的访问令牌
        String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getUsername());

        //构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        loginResponse.setAvatar(userEntity.getAvatar());
        loginResponse.setRefreshToken(refreshToken);
        //设定过期时间为两个小时(毫秒) >>> 7200000L
        loginResponse.setExpiresIn(jwtProperties.getTokenExpire());
        loginResponse.setUsername(userEntity.getUsername());
        loginResponse.setNickname(userEntity.getNickname());
        loginResponse.setUserId(userEntity.getId());
        //加载权限
        permissionCacheService.getPermissionsByUserIdWithCache(userEntity.getId(), "登录接口");
        log.info("用户 {} 登陆成功", userEntity.getNickname());
        return R.ok(loginResponse);
    }

    /**
     * @description: 退出登录
     * @author: JN
     * @date: 2026/1/6 14:13
     * @param: [request]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "退出登录")
    @OperationLog(title = "退出登录", businessType = BusinessType.LOGOUT)
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        // 从请求头获取Token
        String token = getTokenFromRequest(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 1. 将Token加入黑名单
            tokenBlacklistService.addToBlacklist(token);

            // 2. 记录日志（可选）
            String username = jwtTokenProvider.getUsernameFromToken(token);
            log.info("用户 {} 退出登录，Token已加入黑名单", username);
        }

        // 3. 清除安全上下文
        SecurityContextHolder.clearContext();

        return R.ok("退出登录成功");
    }

    /**
     * 从请求头提取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * @description: 刷新token
     * @author: JN
     * @date: 2026/1/6 14:15
     * @param: [refreshToken]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.dto.LoginResponse>
     **/
    @RateLimit(
            type = RateLimitType.SLIDING_WINDOW,
            scope = RateLimitScope.IP,
            window = 60,
            limit = 5,
            message = "您操作过于频繁"
    )
    @Operation(summary = "刷新token")
    @OperationLog(title = "刷新token", businessType = BusinessType.LOGIN)
    @PostMapping("/refresh")
    public R<LoginResponse> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        // 验证 refreshToken
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return R.error(401, "刷新令牌无效或已过期");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // 获取用户信息
        UserEntity userEntity = securityUserService.lambdaQuery()
                .eq(UserEntity::getUsername, username)
                .one();

        if (userEntity == null) {
            return R.error(401, "用户不存在");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userEntity.getId());
        claims.put("nickname", userEntity.getNickname());

        String newToken = jwtTokenProvider.generateToken(username, claims);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        LoginResponse response = new LoginResponse();
        response.setToken(newToken);
        response.setRefreshToken(newRefreshToken);
        response.setExpiresIn(jwtProperties.getTokenExpire());
        response.setUsername(userEntity.getUsername());
        response.setNickname(userEntity.getNickname());
        response.setUserId(userEntity.getId());

        return R.ok(response);
    }

    /**
     * @description: 获取当前用户信息
     * @author: JN
     * @date: 2026/1/1 14:54
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Operation(summary = "获取当前用户信息")
    @OperationLog(title = "获取当前用户信息", businessType = BusinessType.QUERY)
    @GetMapping("/me")
    public R<Map<String, Object>> getCurrentUser() {
        //从安全上下文中获取认证信息
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        //检查是否已认证
        if (authentication == null || !authentication.isAuthenticated()) {
            //未认证.返回401未授权错误
            return R.error(401, "请登录后查看");
        }

        //从安全上下文中获取用户名
        String username = authentication.getName();

        //通过 username 查询用户信息
        UserEntity userEntity = securityUserService.lambdaQuery()
                .eq(UserEntity::getUsername, username)
                .one();

        // 检查用户是否存在
        if (userEntity == null) {
            return R.error(401, "用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", userEntity.getId());
        userInfo.put("username", userEntity.getUsername());
        userInfo.put("nickname", userEntity.getNickname());
        userInfo.put("phone", userEntity.getPhone());
        userInfo.put("email", userEntity.getEmail());
        userInfo.put("avatar", userEntity.getAvatar());
        userInfo.put("createTime", userEntity.getCreateTime());

        return R.ok(userInfo);

    }
}
