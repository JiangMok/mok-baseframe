package com.mok.baseframe.mq.consumer;

import com.mok.baseframe.core.service.OperationLogService;
import com.mok.baseframe.dto.OperationLogMessage;
import com.mok.baseframe.entity.OperationLogEntity;
import com.mok.baseframe.mq.config.RabbitMQConfig;
import com.mok.baseframe.mq.service.ConsumerService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 操作日志消费者
 * 负责从RabbitMQ接收操作日志消息并保存到数据库
 */
@Component
public class OperationLogConsumer {

    // 日志记录器
    private static final Logger logger = LoggerFactory.getLogger(OperationLogConsumer.class);

    // 操作日志服务，用于保存日志到数据库
    private final OperationLogService operationLogService;
    private final ConsumerService consumerService;

    public OperationLogConsumer(OperationLogService operationLogService,
                                ConsumerService consumerService) {
        this.operationLogService = operationLogService;
        this.consumerService = consumerService;
    }

    /**
     * 监听操作日志队列
     * 当队列中有新消息时，这个方法会自动执行
     *
     * @RabbitListener 注解用于监听指定队列
     * queues参数指定要监听的队列名称
     */
    @RabbitListener(queues = RabbitMQConfig.OPERATION_LOG_QUEUE)
    public void handleOperationLog(OperationLogMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            logger.info("接收到操作日志消息: {}", message.getTitle());

            // 检查是否已存在（防止重复）
            if (consumerService.checkOperationLogExistsById(message.getId())) {
                channel.basicAck(deliveryTag, false);  // 确认已处理的消息
                logger.info("操作日志已存在，跳过处理: {}", message.getId());
                return;
            }

            // 将消息对象转换为实体对象
            OperationLogEntity entity = convertToEntity(message);
            // 调用Service保存到数据库
            operationLogService.recordLog(entity);

            // ✅ 手动确认消息
            channel.basicAck(deliveryTag, false);

            logger.info("操作日志保存成功: {}", message.getTitle());

        } catch (Exception e) {
            logger.error("处理操作日志消息失败: {}", e.getMessage(), e);

            // ❌ 处理失败，拒绝消息并重新入队
            // channel.basicNack(deliveryTag, false, true);
            // ✅ 或者处理失败，拒绝消息并不重新入队（防止无限循环）
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                logger.error("处理操作日志消息失败>>>拒绝消息并不重新入队: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 将消息DTO转换为数据库实体
     * 这是一个简单的转换方法
     */
    private OperationLogEntity convertToEntity(OperationLogMessage message) {
        OperationLogEntity entity = new OperationLogEntity();

        // 设置基本字段
        entity.setId(message.getId());
        entity.setTitle(message.getTitle());
        entity.setBusinessType(message.getBusinessType());
        entity.setMethod(message.getMethod());
        entity.setRequestMethod(message.getRequestMethod());
        entity.setOperUrl(message.getOperUrl());
        entity.setOperIp(message.getOperIp());
        entity.setOperatorName(message.getOperatorName());
        entity.setOperatorType(message.getOperatorType());
        entity.setOperParam(message.getOperParam());
        entity.setJsonResult(message.getJsonResult());
        entity.setStatus(message.getStatus());
        entity.setErrorMsg(message.getErrorMsg());

        // 设置时间字段
        if (message.getOperTime() != null) {
            entity.setCreateTime(message.getOperTime());
        } else {
            entity.setCreateTime(LocalDateTime.now());
        }

        return entity;
    }
}