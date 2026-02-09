package com.mok.baseframe.mq.config;

import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类
 * 这是一个简单的配置，只定义了操作日志相关的队列和交换机
 */
@Configuration
public class RabbitMQConfig {
    // 操作日志-队列名称
    public static final String OPERATION_LOG_QUEUE = "operation.log.queue";
    // 操作日志-交换机名称
    public static final String OPERATION_LOG_EXCHANGE = "operation.log.exchange";
    // 操作日志-路由键
    public static final String OPERATION_LOG_ROUTING_KEY = "operation.log.routing";
    // 库存预警路由键
    public static final String STOCK_UPDATE_WARNING_ROUTING_KEY = "stock.update.warning";
    // 库存同步路由键
    public static final String STOCK_UPDATE_SYNC_ROUTING_KEY = "stock.update.sync";
    private static final Logger log = LogUtils.getLogger(RabbitMQConfig.class);
    // 订单支付成功交换机
    private static final String ORDER_PAY_EXCHANGE = "order.pay.exchange";
    // 订单支付成功队列
    private static final String ORDER_PAY_QUEUE = "order.pay.queue";
    // 订单支付成功路由键
    private static final String ORDER_PAY_ROUTING_KEY = "order.pay";
    // 订单取消交换机
    private static final String ORDER_CANCEL_EXCHANGE = "order.cancel.exchange";
    // 订单取消队列
    private static final String ORDER_CANCEL_QUEUE = "order.cancel.queue";
    // 订单取消路由键
    private static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";
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
     * 创建操作日志队列
     * 使用持久化队列，重启后消息不会丢失
     */
    @Bean
    public Queue orderCancleQueue() {
        // 参数说明：
        // 1. queue: 队列名称
        // 2. durable: 是否持久化（true表示重启后队列还在）
        // 3. exclusive: 是否排他（true表示仅允许当前连接使用）
        // 4. autoDelete: 是否自动删除（没有消费者时自动删除）
        return new Queue(ORDER_CANCEL_QUEUE, true, false, false);
    }

    /**
     * 创建直连交换机
     * 直连交换机：根据路由键精确匹配发送消息
     */
    @Bean
    public DirectExchange orderCancelExchange() {
        // 参数说明：
        // 1. name: 交换机名称
        // 2. durable: 是否持久化
        // 3. autoDelete: 是否自动删除
        return new DirectExchange(ORDER_CANCEL_EXCHANGE, true, false);
    }

    /**
     * 绑定队列到交换机
     * 将队列和交换机通过路由键绑定起来
     */
    @Bean
    public Binding orderCancleBinding() {
        return BindingBuilder.bind(orderPayQueue())
                .to(orderPayExchange())
                .with(ORDER_CANCEL_ROUTING_KEY);
    }

    /**
     * 创建操作日志队列
     * 使用持久化队列，重启后消息不会丢失
     */
    @Bean
    public Queue orderPayQueue() {
        // 参数说明：
        // 1. queue: 队列名称
        // 2. durable: 是否持久化（true表示重启后队列还在）
        // 3. exclusive: 是否排他（true表示仅允许当前连接使用）
        // 4. autoDelete: 是否自动删除（没有消费者时自动删除）
        return new Queue(ORDER_PAY_QUEUE, true, false, false);
    }

    /**
     * 创建直连交换机
     * 直连交换机：根据路由键精确匹配发送消息
     */
    @Bean
    public DirectExchange orderPayExchange() {
        // 参数说明：
        // 1. name: 交换机名称
        // 2. durable: 是否持久化
        // 3. autoDelete: 是否自动删除
        return new DirectExchange(ORDER_PAY_EXCHANGE, true, false);
    }

    /**
     * 绑定队列到交换机
     * 将队列和交换机通过路由键绑定起来
     */
    @Bean
    public Binding orderPayBinding() {
        return BindingBuilder.bind(orderPayQueue())
                .to(orderPayExchange())
                .with(ORDER_PAY_ROUTING_KEY);
    }

    /**
     * 库存更新交换机
     */
    @Bean
    public DirectExchange stockUpdateExchange() {
        return new DirectExchange(STOCK_UPDATE_EXCHANGE);
    }

    /**
     * 库存更新队列
     */
    @Bean
    public Queue stockUpdateQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置死信交换机
        args.put("x-dead-letter-exchange", STOCK_UPDATE_DLX_EXCHANGE);
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", STOCK_UPDATE_DLX_ROUTING_KEY);
        // 设置消息过期时间（30分钟）
        args.put("x-message-ttl", 30 * 60 * 1000);
        // 设置队列最大长度
        args.put("x-max-length", 10000);

        return QueueBuilder.durable(STOCK_UPDATE_QUEUE)
                .withArguments(args)
                .build();
    }

    /**
     * 绑定库存更新队列到交换机
     */
    @Bean
    public Binding stockUpdateBinding() {
        return BindingBuilder.bind(stockUpdateQueue())
                .to(stockUpdateExchange())
                .with(STOCK_UPDATE_ROUTING_KEY);
    }

    /**
     * 库存预警队列
     */
    @Bean
    public Queue stockUpdateWarningQueue() {
        return QueueBuilder.durable("stock.update.warning.queue").build();
    }

    /**
     * 绑定库存预警队列
     */
    @Bean
    public Binding stockUpdateWarningBinding() {
        return BindingBuilder.bind(stockUpdateWarningQueue())
                .to(stockUpdateExchange())
                .with(STOCK_UPDATE_WARNING_ROUTING_KEY);
    }

    /**
     * 库存同步队列
     */
    @Bean
    public Queue stockUpdateSyncQueue() {
        return QueueBuilder.durable("stock.update.sync.queue").build();
    }

    /**
     * 绑定库存同步队列
     */
    @Bean
    public Binding stockUpdateSyncBinding() {
        return BindingBuilder.bind(stockUpdateSyncQueue())
                .to(stockUpdateExchange())
                .with(STOCK_UPDATE_SYNC_ROUTING_KEY);
    }

    /**
     * 库存更新死信交换机
     */
    @Bean
    public DirectExchange stockUpdateDlxExchange() {
        return new DirectExchange(STOCK_UPDATE_DLX_EXCHANGE);
    }

    /**
     * 库存更新死信队列
     */
    @Bean
    public Queue stockUpdateDlxQueue() {
        return QueueBuilder.durable(STOCK_UPDATE_DLX_QUEUE).build();
    }

    /**
     * 绑定库存更新死信队列
     */
    @Bean
    public Binding stockUpdateDlxBinding() {
        return BindingBuilder.bind(stockUpdateDlxQueue())
                .to(stockUpdateDlxExchange())
                .with(STOCK_UPDATE_DLX_ROUTING_KEY);
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
                log.info("消息发送成功");
            } else {
                log.info("消息发送失败:{}", cause);
            }
        });

        return rabbitTemplate;
    }
}