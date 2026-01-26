package com.mok.baseframe.mq.producer;

import com.mok.baseframe.entity.OperationLogEntity;
import com.mok.baseframe.mq.config.RabbitMQConfig;
import com.mok.baseframe.dto.OperationLogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志生产者
 * 负责发送操作日志消息到RabbitMQ
 */
@Component
public class OperationLogProducer {

    // 日志记录器
    private static final Logger log = LoggerFactory.getLogger(OperationLogProducer.class);

    // RabbitTemplate 是 Spring 提供的 RabbitMQ 操作工具
    private final RabbitTemplate rabbitTemplate;

    public OperationLogProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送操作日志消息
     * 这是一个简单的发送方法
     */
    public void sendOperationLog(OperationLogMessage message) {
        try {
            // 打印日志，方便调试
            log.info("准备发送操作日志消息: {}", message.getTitle());

            // 发送消息到RabbitMQ
            // 参数说明：
            // 1. 交换机名称
            // 2. 路由键
            // 3. 消息内容
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.OPERATION_LOG_EXCHANGE,
                    RabbitMQConfig.OPERATION_LOG_ROUTING_KEY,
                    message
            );

            log.info("操作日志消息发送成功: {}", message.getTitle());

        } catch (Exception e) {
            // 捕获异常，防止因为消息发送失败影响主业务流程
            log.error("操作日志消息发送失败: {}", e.getMessage(), e);

            // 这里可以添加降级处理，比如：
            // 1. 重试几次
            // 2. 保存到本地文件
            // 3. 保存到数据库失败记录表

        }
    }

    /**
     * 简化的发送方法（用于快速发送）
     */
    public void sendSimpleLog(String title, String businessType, String method,
                              String requestMethod, String operUrl, String operIp,
                              String operatorName, Integer status, String errorMsg) {

        // 创建消息对象
        OperationLogMessage message = new OperationLogMessage();
        message.setTitle(title);
        message.setBusinessType(businessType);
        message.setMethod(method);
        message.setRequestMethod(requestMethod);
        message.setOperUrl(operUrl);
        message.setOperIp(operIp);
        message.setOperatorName(operatorName);
        message.setStatus(status);
        message.setErrorMsg(errorMsg);

        // 发送消息
        sendOperationLog(message);
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