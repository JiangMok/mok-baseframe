package com.mok.baseframe.order.mq.consumer;

import cn.hutool.core.util.IdUtil;
import com.mok.baseframe.dao.DeliveryOrderMapper;
import com.mok.baseframe.dao.OrderInfoMapper;
import com.mok.baseframe.entity.DeliveryOrderEntity;
import com.mok.baseframe.entity.OrderInfoEntity;
import com.mok.baseframe.order.util.OrderNoGenerator;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
public class OrderPayConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderPayConsumer.class);

    private final OrderInfoMapper orderInfoMapper;
    private final DeliveryOrderMapper deliveryOrderMapper;

    public OrderPayConsumer(OrderInfoMapper orderInfoMapper,
                            DeliveryOrderMapper deliveryOrderMapper) {
        this.orderInfoMapper = orderInfoMapper;
        this.deliveryOrderMapper = deliveryOrderMapper;
    }

    /**
     * 监听订单支付成功队列
     */
    @RabbitListener(queues = "order.pay.queue")
    public void handleOrderPay(String orderNo, Channel channel, Message mqMessage) throws IOException {
        try {
            logger.info("收到订单支付成功消息，订单号：{}", orderNo);

            // 1. 查询订单信息
            OrderInfoEntity order = orderInfoMapper.selectByOrderNo(orderNo);
            if (order == null) {
                logger.error("订单不存在，订单号：{}", orderNo);
                channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            // 2. 更新订单状态为已发货（这里简化处理，实际可能需要仓库系统处理）
            // 已支付
            OrderInfoEntity orderToUpdateOrderStatus = new OrderInfoEntity();
            orderToUpdateOrderStatus.setId(order.getId());
            orderToUpdateOrderStatus.setOrderStatus(1);
            orderToUpdateOrderStatus.setDeliveryTime(new Date());
            orderInfoMapper.update(orderToUpdateOrderStatus);

            // 3. 创建发货单
            DeliveryOrderEntity deliveryOrder = new DeliveryOrderEntity();
            deliveryOrder.setId(IdUtil.simpleUUID());
            deliveryOrder.setDeliveryNo(OrderNoGenerator.generateDeliveryNo());
            deliveryOrder.setOrderId(order.getId());
            deliveryOrder.setOrderNo(orderNo);
            deliveryOrder.setUserId(order.getUserId());
            deliveryOrder.setProductId(order.getProductId());
            deliveryOrder.setProductName(order.getProductName());
            deliveryOrder.setQuantity(order.getQuantity());
            // 实际应从用户地址表获取
            deliveryOrder.setReceiverName("默认收货人");
            deliveryOrder.setReceiverPhone("13800138000");
            deliveryOrder.setReceiverAddress("默认收货地址");
            // 未发货
            deliveryOrder.setDeliveryStatus(0);

            deliveryOrderMapper.insert(deliveryOrder);

            logger.info("处理订单支付成功消息完成，订单号：{}，发货单号：{}", orderNo, deliveryOrder.getDeliveryNo());

            // 4. 发送发货通知（这里可以调用其他服务）

            // 手动确认消息
            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("处理订单支付成功消息失败，订单号：{}，异常：{}", orderNo, e.getMessage(), e);

            // 处理失败，重试3次
            Integer retryCount = mqMessage.getMessageProperties().getHeader("retry-count");
            if (retryCount == null) {
                retryCount = 0;
            }

            if (retryCount < 3) {
                // 重试
                channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, true);
            } else {
                // 超过重试次数，放入死信队列
                channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, false);
                logger.error("订单支付消息处理失败超过重试次数，订单号：{}，转入死信队列", orderNo);
            }
        }
    }
}