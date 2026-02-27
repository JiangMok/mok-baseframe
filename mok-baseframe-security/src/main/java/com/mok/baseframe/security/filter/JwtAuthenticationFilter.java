package com.mok.baseframe.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mok.baseframe.constant.ResponseCode;
import com.mok.baseframe.security.jwt.JwtTokenProvider;
import com.mok.baseframe.security.jwt.TokenBlacklistService;
import com.mok.baseframe.utils.LogUtils;
import com.mok.baseframe.utils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @description: JWT认证过滤器 >>> Token验证门户  创建顺序:4
 * @author: JN
 * @date: 2026/1/6 11:08
 * @param:
 * @return:
 **/

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LogUtils.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   TokenBlacklistService tokenBlacklistService,
                                   AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.authenticationEntryPoint = authenticationEntryPoint;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null) {
            try {
                // 1. 检查Token是否在黑名单中
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    log.warn("Token已被加入黑名单");
//                    ResponseUtils.writeError(response, ResponseCode.UNAUTHORIZED, "请登录后重试");
//                    sendErrorResponse(response,R.tokenInvalid());
//                    return;
                    authenticationEntryPoint.commence(request, response, new AuthenticationException("请登录后重试") {});
                    return;
                }
                // 2. 验证Token的签名和过期时间
                if (jwtTokenProvider.validateToken(token)) {
                    // 3. 设置认证信息
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("用户 {} 认证成功", authentication.getName());
                } else {
                    // Token验证失败（过期或无效）
                    log.warn("Token验证失败");
//                    ResponseUtils.writeError(response, ResponseCode.UNAUTHORIZED, "登录认证失败,请重试");
//                    return;
                    authenticationEntryPoint.commence(request, response, new AuthenticationException("登录认证失败,请重试") {});
                    return;
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.error("Token验证异常", e);
//                ResponseUtils.writeError(response, ResponseCode.UNAUTHORIZED, "登录信息已过期,请重新登录");
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(request, response, new AuthenticationException("登录信息已过期,请重新登录") {});
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


}