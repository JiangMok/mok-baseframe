package com.mok.baseframe.captcha.controller;

import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.common.R;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.captcha.service.CaptchaService;
import com.mok.baseframe.ratelimiter.annotation.RateLimit;
import com.mok.baseframe.ratelimiter.enums.RateLimitScope;
import com.mok.baseframe.ratelimiter.enums.RateLimitType;
import com.mok.baseframe.utils.LogUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @description:验证码控制器
 * @author: JN
 * @date: 2026/1/1
 */

@RestController
@RequestMapping("/captcha")

@Tag(name = "验证码", description = "验证码相关接口")
public class CaptchaController {
    private static final Logger log = LogUtils.getLogger(CaptchaController.class);

    //注入验证码服务
    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    /**
     * @description: 生成验证码
     * @author: JN
     * @date: 2026/1/1 16:53
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @RateLimit(
            type = RateLimitType.SLIDING_WINDOW,
            scope = RateLimitScope.IP,
            window = 60,
            limit = 5,
            message = "您操作过于频繁"
    )
    @Operation(summary = "生成验证码")
    @OperationLog(title = "生成验证码", businessType = BusinessType.LOGIN)
    @GetMapping("/generate")
    public R<Map<String, Object>> generateCaptcha() {
        try {
            Map<String, Object> captcha = captchaService.generateCaptcha();
            return R.ok(captcha);
        } catch (Exception e) {
            log.error("验证码生成失败", e);
            return R.error("验证码生成失败");
        }
    }

    /**
     * @description: 验证验证码
     * @author: JN
     * @date: 2026/1/1 19:25
     * @param: [key, code]
     * @return: com.mok.securityframework.common.R<java.lang.Boolean>
     **/
    @Operation(summary = "生成验证码")
    @OperationLog(title = "生成验证码", businessType = BusinessType.QUERY)
    @PostMapping("/verify")
    public R<Boolean> verifyCaptcha(@RequestParam String key,
                                    @RequestParam String code) {
        boolean isValid = captchaService.validateCaptcha(key, code);
        return R.ok(isValid);
    }

}
