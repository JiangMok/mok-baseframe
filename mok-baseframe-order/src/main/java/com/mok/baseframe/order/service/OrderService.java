package com.mok.baseframe.order.service;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.entity.OrderInfoEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:  订单 service 接口
 * @author: mok
 * @date: 2026/2/5 00:12
**/
public interface OrderService {
    /**
     * 创建订单
     */
    String createOrder(String userId, String productId, Integer quantity,
                      List<String> couponIds, String remark,Integer orderType);
    
    /**
     * 确认订单（下单但未支付）
     */
    String confirmOrder(String userId, String productId, Integer quantity,
                       List<String> couponIds, String remark);
    
    /**
     * 支付订单
     */
    boolean payOrder(String orderNo, Integer payType);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(String orderNo, String cancelReason);
    
    /**
     * 根据订单号查询订单
     */
    OrderInfoEntity getOrderByNo(String orderNo);
    
    /**
     * 分页查询订单列表
     */
    PageResult<OrderInfoEntity> getOrderList(PageParam pageParam);
    
    /**
     * 关闭超时未支付订单
     */
    void closeTimeoutOrders();
    
    /**
     * 计算订单金额
     */
    BigDecimal calculateOrderAmount(String productId, Integer quantity, List<String> couponIds);
}