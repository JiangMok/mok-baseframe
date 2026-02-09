package com.mok.baseframe.order.controller;

import com.mok.baseframe.common.R;
import com.mok.baseframe.order.service.SeckillService;
import com.mok.baseframe.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
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
    @PostMapping("/order")
    public R<String> seckillOrder(@RequestParam String productId,
                                  @RequestParam Integer quantity,
                                  @RequestParam(required = false) String verifyCode) {
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
    @GetMapping("/verify/code")
    public R<String> getSeckillVerifyCode(@RequestParam String productId) {
        String userId = securityUtils.getCurrentUserId();
        return seckillService.getSeckillVerifyCode(userId, productId);
    }

    /**
     * 初始化秒杀库存（管理员操作）
     */
    @PostMapping("/init/stock")
    public R<String> initSeckillStock() {
        seckillService.initSeckillStockToRedis();
        return R.ok("初始化秒杀库存成功");
    }
}