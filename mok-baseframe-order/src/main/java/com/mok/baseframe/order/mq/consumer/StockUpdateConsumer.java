package com.mok.baseframe.order.mq.consumer;

import com.mok.baseframe.dao.InventoryLogMapper;
import com.mok.baseframe.dao.ProductMapper;
import com.mok.baseframe.entity.InventoryLogEntity;
import com.mok.baseframe.entity.ProductEntity;
import com.mok.baseframe.order.util.RedisKeyUtil;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class StockUpdateConsumer {

    private static final Logger logger = LoggerFactory.getLogger(StockUpdateConsumer.class);

    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public StockUpdateConsumer(ProductMapper productMapper,
                               InventoryLogMapper inventoryLogMapper,
                               RedisTemplate<String, Object> redisTemplate) {
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 监听库存更新队列
     */
    @RabbitListener(queues = "stock.update.queue")
    public void handleStockUpdate(Map<String, Object> message, Channel channel, Message mqMessage) throws IOException {
        String messageType = (String) message.get("messageType");

        try {
            if ("STOCK_WARNING".equals(messageType)) {
                handleStockWarning(message);
            } else if ("STOCK_SYNC".equals(messageType)) {
                handleStockSync(message);
            } else {
                handleStockChange(message);
            }

            // 手动确认消息
            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("处理库存更新消息失败：{}，异常：{}", message, e.getMessage(), e);

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
                logger.error("库存更新消息处理失败超过重试次数，消息：{}，转入死信队列", message);
            }
        }
    }

    /**
     * 处理库存变更消息
     */
    private void handleStockChange(Map<String, Object> message) {
        try {
            logger.info("收到库存变更消息：{}", message);

            String productId = String.valueOf(message.get("productId"));
            Integer changeQuantity = Integer.valueOf(message.get("quantity").toString());
            Integer changeType = Integer.valueOf(message.get("changeType").toString());
            String orderNo = (String) message.get("orderNo");
            String orderId = message.get("orderId") != null ? message.get("orderId").toString() : null;
            Boolean isSeckill = (Boolean) message.get("isSeckill");

            if (isSeckill == null) {
                isSeckill = false;
            }

            // 查询商品当前库存
            ProductEntity product = productMapper.selectById(productId);
            if (product == null) {
                logger.error("商品不存在，商品ID：{}", productId);
                return;
            }

            int beforeQuantity = isSeckill ? product.getSeckillStock() : product.getStock();
            int afterQuantity = beforeQuantity;

            // 根据变更类型处理库存
            boolean updateSuccess = false;

            if (changeType == 1) { // 扣减库存
                if (isSeckill) {
                    // 扣减秒杀库存
                    afterQuantity = beforeQuantity - changeQuantity;
                    if (afterQuantity < 0) {
                        logger.warn("秒杀库存不足，商品ID：{}，当前库存：{}，扣减数量：{}",
                                productId, beforeQuantity, changeQuantity);
                        return;
                    }
                    product.setSeckillStock(afterQuantity);
                } else {
                    // 扣减普通库存
                    afterQuantity = beforeQuantity - changeQuantity;
                    if (afterQuantity < 0) {
                        logger.warn("库存不足，商品ID：{}，当前库存：{}，扣减数量：{}",
                                productId, beforeQuantity, changeQuantity);
                        return;
                    }
                    product.setStock(afterQuantity);
                }
                updateSuccess = true;

            } else if (changeType == 2) { // 恢复库存
                if (isSeckill) {
                    afterQuantity = beforeQuantity + changeQuantity;
                    product.setSeckillStock(afterQuantity);
                } else {
                    afterQuantity = beforeQuantity + changeQuantity;
                    product.setStock(afterQuantity);
                }
                updateSuccess = true;

            } else if (changeType == 3) { // 锁定库存
                // 锁定库存逻辑（这里简化为记录日志）
                logger.info("锁定库存，商品ID：{}，数量：{}", productId, changeQuantity);
                updateSuccess = true;

            } else if (changeType == 4) { // 释放库存
                // 释放库存逻辑（这里简化为记录日志）
                logger.info("释放库存，商品ID：{}，数量：{}", productId, changeQuantity);
                updateSuccess = true;
            }

            if (updateSuccess) {
                // 更新商品库存
                int updateResult = productMapper.update(product);
                if (updateResult > 0) {
                    // 更新Redis缓存
                    updateRedisStock(productId, isSeckill, afterQuantity);

                    // 记录库存流水
                    InventoryLogEntity inventoryLog = new InventoryLogEntity();
                    inventoryLog.setProductId(productId);
                    inventoryLog.setOrderId(orderId);
                    inventoryLog.setOrderNo(orderNo);
                    inventoryLog.setChangeType(changeType);
                    inventoryLog.setChangeQuantity(changeQuantity);
                    inventoryLog.setBeforeQuantity(beforeQuantity);
                    inventoryLog.setAfterQuantity(afterQuantity);
                    inventoryLog.setRemark(isSeckill ? "秒杀库存变更" : "普通库存变更");

                    inventoryLogMapper.insert(inventoryLog);

                    logger.info("库存更新成功，商品ID：{}，变更类型：{}，数量：{}，前库存：{}，后库存：{}，是否秒杀：{}",
                            productId, changeType, changeQuantity, beforeQuantity, afterQuantity, isSeckill);

                    // 检查库存是否低于预警阈值
                    checkStockWarning(productId, afterQuantity);
                } else {
                    logger.error("更新数据库库存失败，商品ID：{}", productId);
                }
            }

        } catch (Exception e) {
            logger.error("处理库存变更消息失败：{}，异常：{}", message, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 处理库存预警消息
     */
    private void handleStockWarning(Map<String, Object> message) {
        try {
            Long productId = Long.valueOf(message.get("productId").toString());
            Integer currentStock = Integer.valueOf(message.get("currentStock").toString());
            Integer warningThreshold = Integer.valueOf(message.get("warningThreshold").toString());

            logger.warn("库存预警，商品ID：{}，当前库存：{}，预警阈值：{}",
                    productId, currentStock, warningThreshold);

            // 这里可以发送通知（邮件、短信、钉钉等）
            // sendWarningNotification(productId, currentStock, warningThreshold);

        } catch (Exception e) {
            logger.error("处理库存预警消息失败：{}，异常：{}", message, e.getMessage(), e);
        }
    }

    /**
     * 处理库存同步消息
     */
    private void handleStockSync(Map<String, Object> message) {
        try {
            String productId = message.get("productId").toString();
            Integer redisStock = Integer.valueOf(message.get("redisStock").toString());
            Integer dbStock = Integer.valueOf(message.get("dbStock").toString());

            logger.info("库存同步检查，商品ID：{}，Redis库存：{}，数据库库存：{}",
                    productId, redisStock, dbStock);

            // 如果Redis和数据库库存不一致，进行同步
            if (!redisStock.equals(dbStock)) {
                logger.warn("库存不一致，商品ID：{}，Redis库存：{}，数据库库存：{}，以数据库为准",
                        productId, redisStock, dbStock);

                // 更新Redis库存为数据库库存
                updateRedisStock(productId, false, dbStock);

                // 记录同步日志
                InventoryLogEntity inventoryLog = new InventoryLogEntity();
                inventoryLog.setProductId(productId);
                inventoryLog.setChangeType(5); // 5表示库存同步
                inventoryLog.setChangeQuantity(dbStock - redisStock);
                inventoryLog.setBeforeQuantity(redisStock);
                inventoryLog.setAfterQuantity(dbStock);
                inventoryLog.setRemark("Redis与数据库库存不一致，进行同步");

                inventoryLogMapper.insert(inventoryLog);
            }

        } catch (Exception e) {
            logger.error("处理库存同步消息失败：{}，异常：{}", message, e.getMessage(), e);
        }
    }

    /**
     * 更新Redis库存
     */
    private void updateRedisStock(String productId, boolean isSeckill, Integer stock) {
        try {
            if (isSeckill) {
                String seckillKey = RedisKeyUtil.getSeckillStockKey(productId);
                redisTemplate.opsForValue().set(seckillKey, stock);
            } else {
                String stockKey = RedisKeyUtil.getProductStockKey(productId);
                redisTemplate.opsForValue().set(stockKey, stock);
            }
        } catch (Exception e) {
            logger.error("更新Redis库存失败，商品ID：{}，异常：{}", productId, e.getMessage(), e);
        }
    }

    /**
     * 检查库存预警
     */
    private void checkStockWarning(String productId, Integer currentStock) {
        try {
            // 预警阈值（可以从配置读取，这里硬编码为10）
            int warningThreshold = 10;

            if (currentStock <= warningThreshold) {
                logger.warn("商品库存低于预警阈值，商品ID：{}，当前库存：{}，预警阈值：{}",
                        productId, currentStock, warningThreshold);

                // 这里可以触发预警通知
                // 也可以发送消息到预警队列，由专门的预警处理器处理
            }
        } catch (Exception e) {
            logger.error("检查库存预警失败，商品ID：{}，异常：{}", productId, e.getMessage(), e);
        }
    }

    /**
     * 监听库存预警队列
     */
    @RabbitListener(queues = "stock.update.warning.queue")
    public void handleStockWarningQueue(Map<String, Object> message, Channel channel, Message mqMessage) throws IOException {
        try {
            logger.warn("处理库存预警消息：{}", message);

            // 这里可以实现具体的预警处理逻辑
            // 比如发送邮件、短信、钉钉通知等

            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("处理库存预警队列消息失败：{}", e.getMessage(), e);
            channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    /**
     * 监听库存同步队列
     */
    @RabbitListener(queues = "stock.update.sync.queue")
    public void handleStockSyncQueue(Map<String, Object> message, Channel channel, Message mqMessage) throws IOException {
        try {
            logger.info("处理库存同步消息：{}", message);

            // 这里可以实现具体的库存同步逻辑

            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("处理库存同步队列消息失败：{}", e.getMessage(), e);
            channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    /**
     * 监听库存更新死信队列
     */
    @RabbitListener(queues = "stock.update.dlx.queue")
    public void handleStockUpdateDlxQueue(Map<String, Object> message, Channel channel, Message mqMessage) throws IOException {
        try {
            logger.error("收到库存更新死信队列消息：{}", message);

            // 死信队列处理逻辑
            // 1. 记录错误日志
            // 2. 发送告警通知
            // 3. 尝试恢复或补偿

            String productId = message.get("productId").toString();
            Integer changeType = Integer.valueOf(message.get("changeType").toString());
            Integer changeQuantity = Integer.valueOf(message.get("quantity").toString());

            // 如果是扣减库存失败，尝试恢复Redis库存
            if (changeType == 1) {
                String stockKey = RedisKeyUtil.getProductStockKey(productId);
                redisTemplate.opsForValue().increment(stockKey, changeQuantity);
                logger.info("死信队列：恢复Redis库存，商品ID：{}，数量：{}", productId, changeQuantity);
            }

            channel.basicAck(mqMessage.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("处理库存更新死信队列消息失败：{}", e.getMessage(), e);
            channel.basicNack(mqMessage.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}