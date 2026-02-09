package com.mok.baseframe.dao;

import com.mok.baseframe.entity.DeliveryOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
/**
 * 发货单 mapper
 * @author: mok
 * @date: 2026/2/4 23:33
**/
@Mapper
public interface DeliveryOrderMapper {
    // 插入发货单
    int insert(DeliveryOrderEntity deliveryOrder);

    // 根据ID查询发货单
    DeliveryOrderEntity selectById(String id);

    // 根据发货单号查询
    DeliveryOrderEntity selectByDeliveryNo(String deliveryNo);

    // 根据订单ID查询
    DeliveryOrderEntity selectByOrderId(String orderId);

    // 更新发货单
    int update(DeliveryOrderEntity deliveryOrder);

    // 分页查询发货单列表
    List<DeliveryOrderEntity> selectByPage(Map<String, Object> params);

    // 查询发货单总数
    long countByPage(Map<String, Object> params);

    // 更新发货状态
    int updateDeliveryStatus(@Param("id") String id,
                             @Param("oldStatus") Integer oldStatus,
                             @Param("newStatus") Integer newStatus,
                             @Param("deliveryCompany") String deliveryCompany,
                             @Param("deliveryNumber") String deliveryNumber,
                             @Param("deliveryTime") java.util.Date deliveryTime);

    // 更新收货状态
    int updateReceiveStatus(@Param("id") String id,
                            @Param("oldStatus") Integer oldStatus,
                            @Param("newStatus") Integer newStatus,
                            @Param("receiveTime") java.util.Date receiveTime);
}
