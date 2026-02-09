package com.mok.baseframe.order.task;

import com.mok.baseframe.order.service.CouponService;
import com.mok.baseframe.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderCloseTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderCloseTask.class);

    private final OrderService orderService;
    private final CouponService couponService;

    public OrderCloseTask(OrderService orderService,
                          CouponService couponService) {
        this.couponService = couponService;
        this.orderService = orderService;
    }

    /**
     * 每5分钟执行一次，关闭超时未支付订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void closeTimeoutOrders() {
        try {
            logger.info("开始执行关闭超时未支付订单任务");

            orderService.closeTimeoutOrders();

            logger.info("关闭超时未支付订单任务执行完成");
        } catch (Exception e) {
            logger.error("关闭超时未支付订单任务执行异常：{}", e.getMessage(), e);
        }
    }

    /**
     * 每天凌晨1点执行，清理过期优惠券
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanExpiredCoupons() {
        try {
            logger.info("开始执行清理过期优惠券任务");

            // 这里调用优惠券服务的清理方法
            couponService.cleanExpiredCoupons();

            logger.info("清理过期优惠券任务执行完成");
        } catch (Exception e) {
            logger.error("清理过期优惠券任务执行异常：{}", e.getMessage(), e);
        }
    }
}