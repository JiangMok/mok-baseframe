package com.mok.baseframe.dao;
import com.mok.baseframe.entity.OrderCouponEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * 订单-优惠券 使用情况 mapper
 * @author: mok
 * @date: 2026/2/4
 */
@Mapper
public interface OrderCouponMapper {
    // 插入订单优惠券使用记录
    int insert(OrderCouponEntity orderCoupon);

    // 根据订单ID查询
    List<OrderCouponEntity> selectByOrderId(@Param("orderId") String orderId);

    // 根据订单号查询
    List<OrderCouponEntity> selectByOrderNo(@Param("orderNo") String orderNo);

    // 根据用户优惠券ID查询
    OrderCouponEntity selectByUserCouponId(@Param("userCouponId") String userCouponId);
}
