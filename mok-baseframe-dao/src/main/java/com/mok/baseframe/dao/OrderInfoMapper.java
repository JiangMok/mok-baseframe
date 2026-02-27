package com.mok.baseframe.dao;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.entity.OrderInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
/**
 * 订单信息 mapper
 *
 * @author: mok
 * @date: 2026/2/4 23:21
 **/
@Mapper
public interface OrderInfoMapper {
    // 插入订单
    int insert(OrderInfoEntity order);

    // 根据ID查询订单
    OrderInfoEntity selectById(String id);

    // 根据订单号查询订单
    OrderInfoEntity selectByOrderNo(String orderNo);

    // 更新订单
    int update(OrderInfoEntity order);

    // 分页查询订单列表
    List<OrderInfoEntity> selectByPage(PageParam params);

    // 查询订单总数
    Long countByPage(PageParam params);

    // 更新订单状态
    int updateOrderStatus(@Param("id") String id,
                          @Param("oldStatus") Integer oldStatus,
                          @Param("newStatus") Integer newStatus,
                          @Param("cancelReason") String cancelReason,
                          @Param("cancelTime") Date cancelTime);

    // 更新支付状态
    int updatePayStatus(@Param("id") String id,
                        @Param("oldPayStatus") Integer oldPayStatus,
                        @Param("newPayStatus") Integer newPayStatus,
                        @Param("payTime") Date payTime,
                        @Param("payType") Integer payType,
                        @Param("transactionId") String transactionId);

    // 查询超时未支付订单
    List<OrderInfoEntity> selectTimeoutOrders(@Param("timeoutMinutes") Integer timeoutMinutes,
                                              @Param("status") Integer status);

    // 关闭超时订单
    int closeTimeoutOrders(@Param("timeoutMinutes") Integer timeoutMinutes,
                           @Param("oldStatus") Integer oldStatus,
                           @Param("newStatus") Integer newStatus);
}
