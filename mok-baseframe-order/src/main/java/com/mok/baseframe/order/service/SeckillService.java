package com.mok.baseframe.order.service;

import com.mok.baseframe.common.R;

public interface SeckillService {
    /**
     * 秒杀下单
     */
    R<String> seckillOrder(String userId, String productId, Integer quantity);
    
    /**
     * 获取秒杀验证码
     */
    R<String> getSeckillVerifyCode(String userId, String productId);
    
    /**
     * 校验秒杀验证码
     */
    boolean verifySeckillCode(String userId, String productId, String verifyCode);
    
    /**
     * 初始化秒杀库存到Redis
     */
    void initSeckillStockToRedis();
}