package com.mok.baseframe.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 这是一个简单的配置，只定义了操作日志相关的队列和交换机
 */
@Configuration
public class RabbitMQConfig {
    
    // 队列名称
    public static final String OPERATION_LOG_QUEUE = "operation.log.queue";
    
    // 交换机名称
    public static final String OPERATION_LOG_EXCHANGE = "operation.log.exchange";
    
    // 路由键
    public static final String OPERATION_LOG_ROUTING_KEY = "operation.log.routing";
    
    /**
     * 创建操作日志队列
     * 使用持久化队列，重启后消息不会丢失
     */
    @Bean
    public Queue operationLogQueue() {
        // 参数说明：
        // 1. queue: 队列名称
        // 2. durable: 是否持久化（true表示重启后队列还在）
        // 3. exclusive: 是否排他（true表示仅允许当前连接使用）
        // 4. autoDelete: 是否自动删除（没有消费者时自动删除）
        return new Queue(OPERATION_LOG_QUEUE, true, false, false);
    }
    
    /**
     * 创建直连交换机
     * 直连交换机：根据路由键精确匹配发送消息
     */
    @Bean
    public DirectExchange operationLogExchange() {
        // 参数说明：
        // 1. name: 交换机名称
        // 2. durable: 是否持久化
        // 3. autoDelete: 是否自动删除
        return new DirectExchange(OPERATION_LOG_EXCHANGE, true, false);
    }
    
    /**
     * 绑定队列到交换机
     * 将队列和交换机通过路由键绑定起来
     */
    @Bean
    public Binding operationLogBinding() {
        return BindingBuilder.bind(operationLogQueue())
                .to(operationLogExchange())
                .with(OPERATION_LOG_ROUTING_KEY);
    }
    
    /**
     * JSON消息转换器
     * 让RabbitMQ支持发送和接收JSON格式的消息
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    /**
     * 配置RabbitTemplate
     * RabbitTemplate是发送消息的主要工具类
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        
        // 设置JSON消息转换器
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        
        // 设置消息发送确认回调（可选）
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功");
            } else {
                System.out.println("消息发送失败: " + cause);
            }
        });
        
        return rabbitTemplate;
    }
}