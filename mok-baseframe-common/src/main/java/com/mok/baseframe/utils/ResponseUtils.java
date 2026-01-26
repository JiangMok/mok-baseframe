package com.mok.baseframe.utils;

import com.alibaba.fastjson2.JSON;
import com.mok.baseframe.common.R;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @description: 响应工具类 >>> 返回统一响应结果
 * 用于处理HTTP响应，将结果以JSON格式写入HttpServletResponse中。
 * 使用范围 : 非 spring MVC 管理范围使用
 * @author: JN
 * @date: 2025/12/31 23:45
 * @param:
 * @return:
 **/

public class ResponseUtils  {
       private static final Logger log = LogUtils.getLogger(ResponseUtils.class);

    /**
     * @description: 写入 JSON 响应
     * @author: JN
     * @date: 2026/1/1 09:33
     * @param: [response, result]
     * @return: void
     **/
    public static void writeJson(HttpServletResponse response, R<?> result) {
        //设置响应的字符编码为UTF-8
        //  作用：确保响应中的中文等非ASCII字符正确显示
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        //设置响应的内容类型为application/json
        //  作用：告诉客户端返回的是JSON格式的数据
        //  浏览器和其他HTTP客户端根据这个头信息解析响应体
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        //根据业务码设置HTTP响应的状态码
        //  作用：将自定义的业务状态码映射为标准的HTTP状态码
        //  这是RESTful API设计中的重要部分
        response.setStatus(getHttpStatus(result.getCode()));
        //尝试将JSON写入响应输出流
        //  因为网络IO操作可能失败，所以需要异常处理
        try {
            //将R对象序列化为JSON字符串并写入响应
            //  JSON.toJSONString(result): 使用Fastjson2将R对象转换为JSON字符串
            //  response.getWriter(): 获取响应的字符输出流
            //  write(): 将JSON字符串写入输出流
            response.getWriter().write(JSON.toJSONString(result));
        } catch (IOException e) {
            //捕获并记录IO异常
            //  网络连接问题、客户端断开连接等情况可能抛出IOException
            //  这里记录错误但不重新抛出，因为无法处理连接已关闭的情况
            log.error("写入响应失败", e);
        }
    }

    /**
     * @description: 根据业务码获取 HTTP 状态码
     * @author: JN
     * @date: 2026/1/1 09:33
     * @param: [code]
     * @return: int
     **/
    private static int getHttpStatus(Integer code) {
        if (code == null) {
            return 500;
        }

        // 业务码 -> HTTP状态码映射
        switch (code) {
            //业务码400对应HTTP状态码400
            // 400表示客户端请求有语法错误
            case 400:
                return 400;
            //业务码401对应HTTP状态码401
            // 401表示需要身份验证
            case 401:
                return 401;
            //业务码403对应HTTP状态码403
            // 403表示服务器理解请求但拒绝执行
            case 403:
                return 403;
            //业务码404对应HTTP状态码404
            // 404表示请求的资源不存在
            case 404:
                return 404;
            //业务码405对应HTTP状态码405
            // 405表示请求方法不被允许
            case 405:
                return 405;
            //业务码500对应HTTP状态码500
            // 500表示服务器内部错误
            case 500:
                return 500;
            //业务码503对应HTTP状态码503
            // 503表示服务暂时不可用
            case 503:
                return 503;
            default:
                //业务错误码（1000+）统一返回200，将错误信息放在响应体中
                //  这是一个重要的设计决策
                //  好处 : 前端可以统一处理成功或失败,不需要区分HTTP状态码
                if (code >= 1000) {
                    return 200;
                }
                return 500;
        }
    }

    /**
     * 快速构建响应并写入
     */
    public static void writeSuccess(HttpServletResponse response, Object data) {
        writeJson(response, R.ok(data));
    }

    public static void writeError(HttpServletResponse response, Integer code, String msg) {
        writeJson(response, R.error(code, msg));
    }

    public static void writeBadRequest(HttpServletResponse response, String msg) {
        writeJson(response, R.badRequest(msg));
    }

    public static void writeUnauthorized(HttpServletResponse response, String msg) {
        writeJson(response, R.unauthorized(msg));
    }

    public static void writeForbidden(HttpServletResponse response, String msg) {
        writeJson(response, R.forbidden(msg));
    }
}