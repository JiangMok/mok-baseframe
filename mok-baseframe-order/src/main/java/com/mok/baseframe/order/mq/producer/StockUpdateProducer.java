package com.mok.baseframe.order.mq.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StockUpdateProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(StockUpdateProducer.class);
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    // 库存更新交换机
    private static final String STOCK_UPDATE_EXCHANGE = "stock.update.exchange";
    // 库存更新队列
    private static final String STOCK_UPDATE_QUEUE = "stock.update.queue";
    // 库存更新路由键
    private static final String STOCK_UPDATE_ROUTING_KEY = "stock.update";
    // 库存更新死信交换机
    private static final String STOCK_UPDATE_DLX_EXCHANGE = "stock.update.dlx.exchange";
    // 库存更新死信队列
    private static final String STOCK_UPDATE_DLX_QUEUE = "stock.update.dlx.queue";
    // 库存更新死信路由键
    private static final String STOCK_UPDATE_DLX_ROUTING_KEY = "stock.update.dlx";
    
    /**
     * 发送库存扣减消息
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @param orderId 订单ID
     * @param orderNo 订单号
     */
    public void sendStockReduceMessage(String productId, Integer quantity, String orderId, String orderNo) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("productId", productId);
            message.put("quantity", quantity);
            message.put("changeType", 1); // 1表示扣减库存
            message.put("orderId", orderId);
            message.put("orderNo", orderNo);
            message.put("timestamp", System.currentTimeMillis());
            
            // 设置消息持久化
            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    // 设置消息过期时间（30分钟）
                    message.getMessageProperties().setExpiration("1800000");
                    return message;
                }
            };
            
            rabbitTemplate.convertAndSend(
                STOCK_UPDATE_EXCHANGE,
                STOCK_UPDATE_ROUTING_KEY,
                message,
                messagePostProcessor
            );
            
            logger.info("发送库存扣减消息成功，商品ID：{}，数量：{}，订单号：{}", 
                       productId, quantity, orderNo);
        } catch (Exception e) {
            logger.error("发送库存扣减消息失败，商品ID：{}，数量：{}，订单号：{}，异常：{}", 
                       productId, quantity, orderNo, e.getMessage(), e);
            // 这里可以添加重试逻辑或降级处理
            throw new RuntimeException("发送库存扣减消息失败", e);
        }
    }
    
    /**
     * 发送库存恢复消息
     * 
     * @param productId 商品ID
     * @param quantity 恢复数量
     * @param orderId 订单ID
     * @param orderNo 订单号
     */
    public void sendStockRestoreMessage(String productId, Integer quantity, String orderId, String orderNo) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("productId", productId);
            message.put("quantity", quantity);
            message.put("changeType", 2); // 2表示恢复库存
            message.put("orderId", orderId);
            message.put("orderNo", orderNo);
            message.put("timestamp", System.currentTimeMillis());
            
            // 设置消息持久化
            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    // 设置消息过期时间（30分钟）
                    message.getMessageProperties().setExpiration("1800000");
                    return message;
                }
            };
            
            rabbitTemplate.convertAndSend(
                STOCK_UPDATE_EXCHANGE,
                STOCK_UPDATE_ROUTING_KEY,
                message,
                messagePostProcessor
            );
            
            logger.info("发送库存恢复消息成功，商品ID：{}，数量：{}，订单号：{}", 
                       productId, quantity, orderNo);
        } catch (Exception e) {
            logger.error("发送库存恢复消息失败，商品ID：{}，数量：{}，订单号：{}，异常：{}", 
                       productId, quantity, orderNo, e.getMessage(), e);
            throw new RuntimeException("发送库存恢复消息失败", e);
        }
    }
    
    /**
     * 发送库存锁定消息
     * 
     * @param productId 商品ID
     * @param quantity 锁定数量
     * @param orderId 订单ID
     * @param orderNo 订单号
     */
    public void sendStockLockMessage(String productId, Integer quantity, String orderId, String orderNo) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("productId", productId);
            message.put("quantity", quantity);
            message.put("changeType", 3); // 3表示锁定库存
            message.put("orderId", orderId);
            message.put("orderNo", orderNo);
            message.put("timestamp", System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                STOCK_UPDATE_EXCHANGE,
                STOCK_UPDATE_ROUTING_KEY,
                message
            );
            
            logger.info("发送库存锁定消息成功，商品ID：{}，数量：{}，订单号：{}", 
                       productId, quantity, orderNo);
        } catch (Exception e) {
            logger.error("发送库存锁定消息失败，商品ID：{}，数量：{}，订单号：{}，异常：{}", 
                       productId, quantity, orderNo, e.getMessage(), e);
        }
    }
    
    /**
     * 发送库存释放消息
     * 
     * @param productId 商品ID
     * @param quantity 释放数量
     * @param orderId 订单ID
     * @param orderNo 订单号
     */
    public void sendStockReleaseMessage(Long productId, Integer quantity, Long orderId, String orderNo) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("productId", productId);
            message.put("quantity", quantity);
            message.put("changeType", 4); // 4表示释放库存
            message.put("orderId", orderId);
            message.put("orderNo", orderNo);
            message.put("timestamp", System.currentTimeMillis());
            
            rabbitTemplate.convertAndSend(
                STOCK_UPDATE_EXCHANGE,
                STOCK_UPDATE_ROUTING_KEY,
                message
            );
            
            logger.info("发送库存释放消息成功，商品ID：{}，数量：{}，订单号：{}", 
                       productId, quantity, orderNo);
        } catch (Exception e) {
            logger.error("发送库存释放消息失败，商品ID：{}，数量：{}，订单号：{}，异常：{}", 
                       productId, quantity, orderNo, e.getMessage(), e);
        }
    }
    
    /**
     * 发送秒杀库存扣减消息
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @param orderId 订单ID
     * @param orderNo 订单号
     */
    public void sendSeckillStockReduceMessage(String productId, Integer quantity, String orderId, String orderNo) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("productId", productId);
            message.put("quantity", quantity);
            message.put("changeType", 1); // 1表示扣减库存
            message.put("orderId", orderId);
            message.put("orderNo", orderNo);
            message.put("isSeckill", true); // 标记为秒杀库存
            message.put("timestamp", System.currentTimeMillis());
            
            // 秒杀库存消息设置更高的优先级
            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setPriority(5); // 设置优先级
                    return message;
                }
            };
            
            rabbitTemplate.convertAndSend(
                STOCK_UPDATE_EXCHANGE,
                STOCK_UPDATE_ROUTING_KEY,
                message,
                messagePostProcessor
            );
            
            logger.info("发送秒杀库存扣减消息成功，商品ID：{}，数量：{}，订单号：{}", 
                       productId, quantity, orderNo);
        } catch (Exception e) {
            logger.error("发送秒杀库存扣减消息失败，商品ID：{}，数量：{}，订单号：{}，异常：{}", 
                       productId, quantity, orderNo, e.getMessage(), e);
            throw new RuntimeException("发送秒杀库存扣减消息失败", e);
        }
    }
    
    /**
     * 批量发送库存更新消息
     * 
     * @param messages 库存更新消息列表
     */
    public void batchSendStockUpdateMessages(java.util.List<Map<String, Object>> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Map<String, Object> message : messages) {
            try {
                rabbitTemplate.convertAndSend(
                    STOCK_UPDATE_EXCHANGE,
                    STOCK_UPDATE_ROUTING_KEY,
                    message
                );
                successCount++;
            } catch (Exception e) {
                failCount++;
                logger.error("批量发送库存更新消息失败，消息：{}，异常：{}", message, e.getMessage());
            }
        }
        
        logger.info("批量发送库存更新消息完成，总数：{}，成功：{}，失败：{}", 
                   messages.size(), successCount, failCount);
    }
    
    /**
     * 发送库存预警消息
     * 
     * @param productId 商品ID
     * @param currentStock 当前库存
     * @param warningThreshold 预警阈值
     */
    public void sendStockWarningMessage(Long productId, Integer currentStock, Integer warningThreshold) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("productId", productId);
            message.put("currentStock", currentStock);
            message.put("warningThreshold", warningThreshold);
            message.put("timestamp", System.currentTimeMillis());
            message.put("messageType", "STOCK_WARNING");
            
            rabbitTemplate.convertAndSend(
                STOCK_UPDATE_EXCHANGE,
                STOCK_UPDATE_ROUTING_KEY + ".warning",
                message
            );
            
            logger.warn("发送库存预警消息，商品ID：{}，当前库存：{}，预警阈值：{}", 
                       productId, currentStock, warningThreshold);
        } catch (Exception e) {
            logger.error("发送库存预警消息失败，商品ID：{}，异常：{}", productId, e.getMessage(), e);
        }
    }
    
    /**
     * 发送库存同步消息（用于Redis和数据库库存同步）
     * 
     * @param productId 商品ID
     * @param redisStock Redis库存
     * @param dbStock 数据库库存
     */
    public void sendStockSyncMessage(Long productId, Integer redisStock, Integer dbStock) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("productId", productId);
            message.put("redisStock", redisStock);
            message.put("dbStock", dbStock);
            message.put("timestamp", System.currentTimeMillis());
            message.put("messageType", "STOCK_SYNC");
            
            rabbitTemplate.convertAndSend(
                STOCK_UPDATE_EXCHANGE,
                STOCK_UPDATE_ROUTING_KEY + ".sync",
                message
            );
            
            logger.info("发送库存同步消息，商品ID：{}，Redis库存：{}，数据库库存：{}", 
                       productId, redisStock, dbStock);
        } catch (Exception e) {
            logger.error("发送库存同步消息失败，商品ID：{}，异常：{}", productId, e.getMessage(), e);
        }
    }
}