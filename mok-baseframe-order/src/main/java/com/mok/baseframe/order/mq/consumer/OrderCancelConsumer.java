package com.mok.baseframe.order.mq.consumer;

import com.mok.baseframe.dto.OrderCancelMessage;
import com.mok.baseframe.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderCancelConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderCancelConsumer.class);

    private final OrderService orderService;

    public OrderCancelConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 监听订单取消队列
     */
    @RabbitListener(queues = "order.cancel.queue")
    public void handleOrderCancel(OrderCancelMessage message, Channel channel, Message mqMessage) throws IOException {
        String orderNo = message.getOrderNo();

        try {
            logger.info("收到订单取消消息，订单号：{}", orderNo);

            // 检查订单是否已支付
            // 如果未支付，则取消订单
            boolean canceled = orderService.cancelOrder(orderNo, "超时未支付，系统自动取消");

            if (canceled) {
                logger.info("自动取消订单成功，订单号：{}", orderNo);
            } else {
                logger.info("订单已支付或已取消，订单号：{}", orderNo);
            }

            // 手动确认消息
            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("处理订单取消消息失败，订单号：{}，异常：{}", orderNo, e.getMessage(), e);

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
                logger.error("订单取消消息处理失败超过重试次数，订单号：{}，转入死信队列", orderNo);
            }
        }
    }
}