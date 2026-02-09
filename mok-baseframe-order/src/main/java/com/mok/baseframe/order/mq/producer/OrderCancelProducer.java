package com.mok.baseframe.order.mq.producer;

import com.mok.baseframe.dto.OrderCancelMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderCancelProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderCancelProducer.class);
    // 订单取消交换机
    private static final String ORDER_CANCEL_EXCHANGE = "order.cancel.exchange";
    // 订单取消队列
    private static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    // 订单取消路由键
    private static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";
    private final RabbitTemplate rabbitTemplate;

    public OrderCancelProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送订单取消延迟消息
     *
     * @param orderNo      订单号
     * @param delayMinutes 延迟时间（分钟）
     */
    public void sendOrderCancelMessage(String orderNo, int delayMinutes) {
        try {
            OrderCancelMessage message = new OrderCancelMessage();
            message.setOrderNo(orderNo);
            message.setCreateTime(System.currentTimeMillis());

            // 设置延迟时间（毫秒）
            int delayMillis = delayMinutes * 60 * 1000;

            // 发送延迟消息
            rabbitTemplate.convertAndSend(
                    ORDER_CANCEL_EXCHANGE,
                    ORDER_CANCEL_ROUTING_KEY,
                    message,
                    msg -> {
                        // 设置消息过期时间
                        msg.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                        return msg;
                    }
            );

            logger.info("发送订单取消延迟消息成功，订单号：{}，延迟时间：{}分钟", orderNo, delayMinutes);
        } catch (Exception e) {
            logger.error("发送订单取消延迟消息失败，订单号：{}，异常：{}", orderNo, e.getMessage(), e);
        }
    }
}