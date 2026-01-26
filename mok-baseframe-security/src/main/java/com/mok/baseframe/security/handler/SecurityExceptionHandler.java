package com.mok.baseframe.security.handler;

import com.mok.baseframe.common.R;
import com.mok.baseframe.constant.ResponseCode;
import com.mok.baseframe.utils.LogUtils;
import com.mok.baseframe.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @description: Spring Security的统一异常处理器   创建顺序:5
 * 作用 : 统一处理 Spring Security 的 认证 和 授权 异常
 * @author: JN
 * @date: 2026/1/1 00:04
 * @param:
 * @return:
 **/

@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler  {
       private static final Logger log = LogUtils.getLogger(SecurityExceptionHandler.class);

    /**
     * @description: 认证失败处理
     * 实现接口 AuthenticationEntryPoint 的 commence 方法
     * 这个方法在用户尝试访问需要认证的资源但认证失败时调用
     * @author: JN
     * @date: 2026/1/1 09:41
     * @param: [request, response, authException]
     * @return: void
     **/
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.warn("认证失败，请求地址：{}，异常信息：{}",
                request.getRequestURI(), authException.getMessage());

        // 根据异常类型返回不同的错误码
        //  认证异常处理的逻辑核心
        R<String> result;
        //检查是否是BadCredentialsException --- 凭证错误
        //  通常是用户名和密码错误
        if (authException instanceof org.springframework.security.authentication.BadCredentialsException) {
            //创建密码错误响应
            result = R.passwordError();
        }
        //检查是否是UsernameNotFoundException --- 用户未找到
        //  表示系统中不存在这个用户
        else if (authException instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
            //创建用户不存在响应
            result = R.userNotFound();
        }
        //检查是否是DisabledException --- 用户被禁用
        //  表示用户被禁用
        else if (authException instanceof org.springframework.security.authentication.DisabledException) {
            //创建用户被禁用响应
            result = R.error(ResponseCode.USER_DISABLED, ResponseCode.USER_DISABLED_MSG);
        }
        //检查是否是AccountExpiredException --- 账户过期
        //  表示账户已过期
        else if (authException instanceof org.springframework.security.authentication.AccountExpiredException) {
            //创建账号已过期响应
            result = R.error(ResponseCode.UNAUTHORIZED, "账号已过期");
        }
        //检查是否是LockedException --- 账户被锁定
        else if (authException instanceof org.springframework.security.authentication.LockedException) {
            //创建用好被锁定响应
            result = R.error(ResponseCode.UNAUTHORIZED, "账号已被锁定");
        }
        else {
            //创建通用响应中的未授权响应
            result = R.unauthorized(authException.getMessage());
        }
        //将响应结果写入HTTP响应
        ResponseUtils.writeJson(response, result);
    }

    /**
     * @description: 授权失败处理
     *                  实现 AccessDeniedHandler 接口的 handle 方法
     * @author: JN
     * @date: 2026/1/1 09:49
     * @param: [request, response, accessDeniedException]
     * @return: void
    **/
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.warn("授权失败，请求地址：{}，异常信息：{}",
                request.getRequestURI(), accessDeniedException.getMessage());
        //创建权限不足响应,
        R.forbidden("权限不足");
    }
}