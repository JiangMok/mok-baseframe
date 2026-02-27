package com.mok.baseframe.order.controller;

import com.mok.baseframe.common.R;
import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.order.service.SeckillService;
import com.mok.baseframe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seckill")
@Tag(name = "秒杀管理", description = "秒杀相关接口")
public class SeckillController {

    private final SeckillService seckillService;
    private final SecurityUtils securityUtils;

    public SeckillController(SeckillService seckillService,
                             SecurityUtils securityUtils) {
        this.seckillService = seckillService;
        this.securityUtils = securityUtils;
    }

    /**
     * 秒杀下单
     */
    @Operation(summary = "秒杀下单")
    @OperationLog(title = "秒杀下单", businessType = BusinessType.INSERT)
    @PostMapping("/order")
    @PreAuthorize("@permissionChecker.hasPermission('order:seckill:order')")
    public R<String> seckillOrder(@RequestParam("productId") String productId,
                                  @RequestParam("quantity") Integer quantity,
                                  @RequestParam(name = "verifyCode", required = false) String verifyCode) {
        String userId = securityUtils.getCurrentUserId();

        // 验证码校验（如果提供了验证码）
        if (verifyCode != null && !verifyCode.isEmpty()) {
            boolean valid = seckillService.verifySeckillCode(userId, productId, verifyCode);
            if (!valid) {
                return R.error("验证码错误");
            }
        }

        return seckillService.seckillOrder(userId, productId, quantity);
    }

    /**
     * 获取秒杀验证码
     */
    @Operation(summary = "获取秒杀验证码")
    @OperationLog(title = "获取秒杀验证码", businessType = BusinessType.INSERT)
    @PreAuthorize("@permissionChecker.hasPermission('order:seckill:code')")
    @GetMapping("/verify/code")
    public R<String> getSeckillVerifyCode(@RequestParam("productId") String productId) {
        String userId = securityUtils.getCurrentUserId();
        return seckillService.getSeckillVerifyCode(userId, productId);
    }

    /**
     * 初始化秒杀库存（管理员操作）
     */
    @Operation(summary = "初始化秒杀库存")
    @OperationLog(title = "初始化秒杀库存", businessType = BusinessType.INSERT)
    @PreAuthorize("@permissionChecker.hasPermission('order:seckill:order')")
    @PostMapping("/init/stock")
    public R<String> initSeckillStock() {
        seckillService.initSeckillStockToRedis();
        return R.ok("初始化秒杀库存成功");
    }
}