package com.mok.baseframe.order.service;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.entity.DeliveryOrderEntity;

public interface DeliveryService {
    /**
     * 发货
     */
    boolean shipDelivery(String deliveryId, String deliveryCompany, String deliveryNumber);
    
    /**
     * 确认收货
     */
    boolean receiveDelivery(String deliveryId, String userId);
    
    /**
     * 根据ID查询发货单
     */
    DeliveryOrderEntity getDeliveryById(String id);
    
    /**
     * 根据订单ID查询发货单
     */
    DeliveryOrderEntity getDeliveryByOrderId(String orderId);
    
    /**
     * 分页查询发货单列表（用户）
     */
    PageResult<DeliveryOrderEntity> getDeliveryList(PageParam pageParam, String userId,
                                                   String deliveryNo, String orderNo,
                                                   Integer deliveryStatus, String startTime,
                                                   String endTime);
    
    /**
     * 分页查询发货单列表（管理员）
     */
    PageResult<DeliveryOrderEntity> getAdminDeliveryList(PageParam pageParam, String userId,
                                                        String deliveryNo, String orderNo,
                                                        Integer deliveryStatus, String startTime,
                                                        String endTime);
    
    /**
     * 创建发货单
     */
    void createDelivery(DeliveryOrderEntity deliveryOrder);
}