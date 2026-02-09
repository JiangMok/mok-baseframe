package com.mok.baseframe.order.mq.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderPayProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderPayProducer.class);
    
    private final RabbitTemplate rabbitTemplate;

    public OrderPayProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    
    // 订单支付成功交换机
    private static final String ORDER_PAY_EXCHANGE = "order.pay.exchange";
    
    // 订单支付成功队列
    private static final String ORDER_PAY_QUEUE = "order.pay.queue";
    
    // 订单支付成功路由键
    private static final String ORDER_PAY_ROUTING_KEY = "order.pay";
    
    /**
     * 发送订单支付成功消息
     * @param orderNo 订单号
     */
    public void sendOrderPayMessage(String orderNo) {
        try {
            rabbitTemplate.convertAndSend(
                ORDER_PAY_EXCHANGE,
                ORDER_PAY_ROUTING_KEY,
                orderNo
            );
            
            logger.info("发送订单支付成功消息成功，订单号：{}", orderNo);
        } catch (Exception e) {
            logger.error("发送订单支付成功消息失败，订单号：{}，异常：{}", orderNo, e.getMessage(), e);
        }
    }
}