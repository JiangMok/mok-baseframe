package com.mok.baseframe.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mok.baseframe.common.BusinessException;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.dao.DeliveryOrderMapper;
import com.mok.baseframe.dao.OrderInfoMapper;
import com.mok.baseframe.entity.DeliveryOrderEntity;
import com.mok.baseframe.entity.OrderInfoEntity;
import com.mok.baseframe.order.service.DeliveryService;
import com.mok.baseframe.order.util.OrderNoGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryServiceImpl.class);
    // 日期格式化器
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final DeliveryOrderMapper deliveryOrderMapper;
    private final OrderInfoMapper orderInfoMapper;

    public DeliveryServiceImpl(DeliveryOrderMapper deliveryOrderMapper,
                               OrderInfoMapper orderInfoMapper) {
        this.deliveryOrderMapper = deliveryOrderMapper;
        this.orderInfoMapper = orderInfoMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipDelivery(String deliveryId, String deliveryCompany, String deliveryNumber) {
        try {
            // 1. 查询发货单
            DeliveryOrderEntity deliveryOrder = deliveryOrderMapper.selectById(deliveryId);
            if (deliveryOrder == null) {
                throw new BusinessException("发货单不存在");
            }

            // 2. 校验发货单状态（只能对"待发货"状态进行发货操作）
            if (deliveryOrder.getDeliveryStatus() != 0) {
                throw new BusinessException("发货单状态异常，无法发货");
            }

            // 3. 更新发货状态（使用乐观锁防止并发操作）
            Date deliveryTime = new Date();
            int updateResult = deliveryOrderMapper.updateDeliveryStatus(
                    deliveryId,
                    0, // 旧状态：待发货
                    1, // 新状态：已发货
                    deliveryCompany,
                    deliveryNumber,
                    deliveryTime
            );

            if (updateResult <= 0) {
                throw new BusinessException("发货失败，发货单状态已变更");
            }

            // 4. 更新订单的发货时间
            OrderInfoEntity order = orderInfoMapper.selectById(deliveryOrder.getOrderId());
            if (order != null) {
                order.setDeliveryTime(deliveryTime);
                order.setOrderStatus(2);
                orderInfoMapper.update(order);
            }

            logger.info("发货成功，发货单ID：{}，订单ID：{}，物流公司：{}，物流单号：{}",
                    deliveryId, deliveryOrder.getOrderId(), deliveryCompany, deliveryNumber);

            return true;
        } catch (Exception e) {
            logger.error("发货失败，发货单ID：{}，异常：{}", deliveryId, e.getMessage(), e);
            throw new BusinessException("发货失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean receiveDelivery(String deliveryId, String userId) {
        try {
            // 1. 查询发货单
            DeliveryOrderEntity deliveryOrder = deliveryOrderMapper.selectById(deliveryId);
            if (deliveryOrder == null) {
                throw new BusinessException("发货单不存在");
            }

            // 2. 验证用户权限（只能收货自己的订单）
            if (!deliveryOrder.getUserId().equals(userId)) {
                throw new BusinessException("无权操作此发货单");
            }

            // 3. 校验发货单状态（只能对"已发货"状态进行确认收货）
            if (deliveryOrder.getDeliveryStatus() != 1) {
                throw new BusinessException("发货单状态异常，无法确认收货");
            }

            // 4. 更新收货状态（使用乐观锁防止并发操作）
            Date receiveTime = new Date();
            int updateResult = deliveryOrderMapper.updateReceiveStatus(
                    deliveryId,
                    1, // 旧状态：已发货
                    2, // 新状态：已收货
                    receiveTime
            );

            if (updateResult <= 0) {
                throw new BusinessException("确认收货失败，发货单状态已变更");
            }

            // 5. 更新订单的收货时间
            OrderInfoEntity order = orderInfoMapper.selectById(deliveryOrder.getOrderId());
            if (order != null) {
                order.setReceiveTime(receiveTime);
                // 如果订单未完成，更新为已完成状态
                if (order.getOrderStatus() == 1) { // 已支付状态
                    order.setOrderStatus(3); // 已完成
                }
                orderInfoMapper.update(order);
            }

            logger.info("确认收货成功，发货单ID：{}，订单ID：{}，用户ID：{}",
                    deliveryId, deliveryOrder.getOrderId(), userId);

            return true;
        } catch (Exception e) {
            logger.error("确认收货失败，发货单ID：{}，用户ID：{}，异常：{}",
                    deliveryId, userId, e.getMessage(), e);
            throw new BusinessException("确认收货失败：" + e.getMessage());
        }
    }

    @Override
    public DeliveryOrderEntity getDeliveryById(String id) {
        try {
            return deliveryOrderMapper.selectById(id);
        } catch (Exception e) {
            logger.error("查询发货单失败，ID：{}，异常：{}", id, e.getMessage(), e);
            throw new BusinessException("查询发货单失败");
        }
    }

    @Override
    public DeliveryOrderEntity getDeliveryByOrderId(String orderId) {
        try {
            return deliveryOrderMapper.selectByOrderId(orderId);
        } catch (Exception e) {
            logger.error("根据订单ID查询发货单失败，订单ID：{}，异常：{}", orderId, e.getMessage(), e);
            throw new BusinessException("查询发货单失败");
        }
    }

    @Override
    public PageResult<DeliveryOrderEntity> getDeliveryList(PageParam pageParam,String userId) {
        try {

            List<DeliveryOrderEntity> list = deliveryOrderMapper.selectByPage(pageParam);
            long total = deliveryOrderMapper.countByPage(pageParam);

            return PageResult.success(list, total, pageParam.getPageNum(), pageParam.getPageSize());
        } catch (Exception e) {
            logger.error("查询发货单列表失败，用户ID：{}，异常：{}", userId, e.getMessage(), e);
            throw new BusinessException("查询发货单列表失败");
        }
    }

    @Override
    public PageResult<DeliveryOrderEntity> getAdminDeliveryList(PageParam pageParam) {
        try {

            List<DeliveryOrderEntity> list = deliveryOrderMapper.selectByPage(pageParam);
            long total = deliveryOrderMapper.countByPage(pageParam);

            return PageResult.success(list, total, pageParam.getPageNum(), pageParam.getPageSize());
        } catch (Exception e) {
            logger.error("管理员查询发货单列表失败，异常：{}", e.getMessage(), e);
            throw new BusinessException("查询发货单列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDelivery(DeliveryOrderEntity deliveryOrder) {
        try {
            // 1. 校验订单是否存在且已支付
            OrderInfoEntity order = orderInfoMapper.selectById(deliveryOrder.getOrderId());
            if (order == null) {
                throw new BusinessException("订单不存在");
            }

            if (order.getOrderStatus() != 1 || order.getPayStatus() != 2) {
                throw new BusinessException("订单未支付，无法创建发货单");
            }

            // 2. 检查是否已存在发货单
            DeliveryOrderEntity existingDelivery = deliveryOrderMapper.selectByOrderId(deliveryOrder.getOrderId());
            if (existingDelivery != null) {
                throw new BusinessException("该订单已存在发货单");
            }

            // 3. 填充发货单信息
            deliveryOrder.setDeliveryNo(OrderNoGenerator.generateDeliveryNo());
            deliveryOrder.setId(IdUtil.simpleUUID());
            deliveryOrder.setOrderNo(order.getOrderNo());
            deliveryOrder.setUserId(order.getUserId());
            deliveryOrder.setProductId(order.getProductId());
            deliveryOrder.setProductName(order.getProductName());
            deliveryOrder.setQuantity(order.getQuantity());

            // 4. 设置默认收货信息（这里应该从用户地址表获取，这里使用默认值）
            if (deliveryOrder.getReceiverName() == null) {
                deliveryOrder.setReceiverName("默认收货人");
            }
            if (deliveryOrder.getReceiverPhone() == null) {
                deliveryOrder.setReceiverPhone("13800138000");
            }
            if (deliveryOrder.getReceiverAddress() == null) {
                deliveryOrder.setReceiverAddress("默认收货地址");
            }

            // 5. 设置发货状态
            deliveryOrder.setDeliveryStatus(0); // 待发货

            // 6. 插入发货单
            int result = deliveryOrderMapper.insert(deliveryOrder);
            if (result <= 0) {
                throw new BusinessException("创建发货单失败");
            }

            logger.info("创建发货单成功，发货单号：{}，订单ID：{}，订单号：{}",
                    deliveryOrder.getDeliveryNo(), deliveryOrder.getOrderId(), deliveryOrder.getOrderNo());

        } catch (Exception e) {
            logger.error("创建发货单失败，订单ID：{}，异常：{}", deliveryOrder.getOrderId(), e.getMessage(), e);
            throw new BusinessException("创建发货单失败：" + e.getMessage());
        }
    }

    /**
     * 构建发货单查询参数
     */
    private Map<String, Object> buildDeliveryQueryParams(String userId, String deliveryNo, String orderNo,
                                                         Integer deliveryStatus, String startTime,
                                                         String endTime, PageParam pageParam) {
        Map<String, Object> params = new HashMap<>();

        if (userId != null) {
            params.put("userId", userId);
        }
        if (deliveryNo != null && !deliveryNo.trim().isEmpty()) {
            params.put("deliveryNo", deliveryNo.trim());
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            params.put("orderNo", orderNo.trim());
        }
        if (deliveryStatus != null) {
            params.put("deliveryStatus", deliveryStatus);
        }

        // 处理时间范围
        try {
            if (startTime != null && !startTime.trim().isEmpty()) {
                if (startTime.length() <= 10) {
                    // 只有日期，没有时间
                    params.put("startTime", DATE_ONLY_FORMAT.parse(startTime.trim()));
                } else {
                    // 包含时间
                    params.put("startTime", DATE_FORMAT.parse(startTime.trim()));
                }
            }

            if (endTime != null && !endTime.trim().isEmpty()) {
                if (endTime.length() <= 10) {
                    // 只有日期，没有时间
                    Date endDate = DATE_ONLY_FORMAT.parse(endTime.trim());
                    // 设置为当天的23:59:59
                    params.put("endTime", new Date(endDate.getTime() + 24 * 60 * 60 * 1000 - 1000));
                } else {
                    // 包含时间
                    params.put("endTime", DATE_FORMAT.parse(endTime.trim()));
                }
            }
        } catch (ParseException e) {
            logger.warn("日期格式解析失败，开始时间：{}，结束时间：{}", startTime, endTime);
        }

        // 分页参数
        params.put("offset", pageParam.getOffset());
        params.put("limit", pageParam.getPageSize());

        return params;
    }

    /**
     * 批量发货（管理员功能）
     */
    public boolean batchShipDelivery(List<String> deliveryIds, String deliveryCompany, String deliveryNumber) {
        try {
            int successCount = 0;
            for (String deliveryId : deliveryIds) {
                try {
                    boolean success = shipDelivery(deliveryId, deliveryCompany, deliveryNumber);
                    if (success) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.error("批量发货失败，发货单ID：{}，异常：{}", deliveryId, e.getMessage());
                    // 继续处理下一个
                }
            }

            logger.info("批量发货完成，共处理{}个，成功{}个，失败{}个",
                    deliveryIds.size(), successCount, deliveryIds.size() - successCount);

            return successCount > 0;
        } catch (Exception e) {
            logger.error("批量发货失败，异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 更新发货单备注
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDeliveryRemark(String deliveryId, String remark) {
        try {
            DeliveryOrderEntity deliveryOrder = deliveryOrderMapper.selectById(deliveryId);
            if (deliveryOrder == null) {
                throw new BusinessException("发货单不存在");
            }

            deliveryOrder.setRemark(remark);
            int result = deliveryOrderMapper.update(deliveryOrder);

            if (result > 0) {
                logger.info("更新发货单备注成功，发货单ID：{}，备注：{}", deliveryId, remark);
                return true;
            } else {
                throw new BusinessException("更新发货单备注失败");
            }
        } catch (Exception e) {
            logger.error("更新发货单备注失败，发货单ID：{}，异常：{}", deliveryId, e.getMessage(), e);
            throw new BusinessException("更新发货单备注失败：" + e.getMessage());
        }
    }

    /**
     * 根据发货单号查询发货单
     */
    public DeliveryOrderEntity getDeliveryByDeliveryNo(String deliveryNo) {
        try {
            return deliveryOrderMapper.selectByDeliveryNo(deliveryNo);
        } catch (Exception e) {
            logger.error("根据发货单号查询发货单失败，发货单号：{}，异常：{}", deliveryNo, e.getMessage(), e);
            throw new BusinessException("查询发货单失败");
        }
    }

    /**
     * 获取待发货的发货单数量（管理员仪表板使用）
     */
    public int getPendingDeliveryCount() {
        try {
            Map<String, Object> params = new HashMap<>();
            // 待发货
            params.put("deliveryStatus", 0);
            PageParam pageParam = new PageParam();
            pageParam.setParams(params);
            pageParam.setPageNum(1);
            pageParam.setPageSize(Integer.MAX_VALUE);
            List<DeliveryOrderEntity> list = deliveryOrderMapper.selectByPage(pageParam);
            return list != null ? list.size() : 0;
        } catch (Exception e) {
            logger.error("获取待发货数量失败，异常：{}", e.getMessage(), e);
            return 0;
        }
    }
}