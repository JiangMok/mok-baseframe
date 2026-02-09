package com.mok.baseframe.order.util;

/**
 * Redis Key工具类
 */
public class RedisKeyUtil {
    // 商品库存缓存key前缀
    private static final String PRODUCT_STOCK_KEY = "product:stock:%s";

    // 秒杀商品库存缓存key前缀
    private static final String SECKILL_STOCK_KEY = "seckill:stock:%s";

    // 优惠券库存缓存key前缀
    private static final String COUPON_STOCK_KEY = "coupon:stock:%s";

    // 用户秒杀限流key前缀
    private static final String USER_SECKILL_LIMIT_KEY = "seckill:user:%s:product:%s";

    // 分布式锁key前缀
    private static final String DISTRIBUTED_LOCK_KEY = "lock:%s";

    // 订单支付状态key前缀
    private static final String ORDER_PAY_STATUS_KEY = "order:pay:status:%s";

    // 订单确认token key前缀
    private static final String ORDER_CONFIRM_TOKEN_KEY = "order:confirm:token:%s";

    /**
     * 获取商品库存缓存key
     */
    public static String getProductStockKey(String productId) {
        return String.format(PRODUCT_STOCK_KEY, productId);
    }

    /**
     * 获取秒杀商品库存缓存key
     */
    public static String getSeckillStockKey(String productId) {
        return String.format(SECKILL_STOCK_KEY, productId);
    }

    /**
     * 获取优惠券库存缓存key
     */
    public static String getCouponStockKey(String couponId) {
        return String.format(COUPON_STOCK_KEY, couponId);
    }

    /**
     * 获取用户秒杀限流key
     */
    public static String getUserSeckillLimitKey(String userId, String productId) {
        return String.format(USER_SECKILL_LIMIT_KEY, userId, productId);
    }

    /**
     * 获取分布式锁key
     */
    public static String getDistributedLockKey(String lockName) {
        return String.format(DISTRIBUTED_LOCK_KEY, lockName);
    }

    /**
     * 获取订单支付状态key
     */
    public static String getOrderPayStatusKey(String orderNo) {
        return String.format(ORDER_PAY_STATUS_KEY, orderNo);
    }

    /**
     * 获取订单确认token key
     */
    public static String getOrderConfirmTokenKey(String userId) {
        return String.format(ORDER_CONFIRM_TOKEN_KEY, userId);
    }
}