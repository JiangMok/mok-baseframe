package com.mok.baseframe.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.BusinessException;
import com.mok.baseframe.dao.*;
import com.mok.baseframe.entity.*;
import com.mok.baseframe.order.mq.producer.OrderCancelProducer;
import com.mok.baseframe.order.mq.producer.OrderPayProducer;
import com.mok.baseframe.order.mq.producer.StockUpdateProducer;
import com.mok.baseframe.order.service.OrderService;
import com.mok.baseframe.order.service.ProductService;
import com.mok.baseframe.order.util.OrderNoGenerator;
import com.mok.baseframe.order.util.RedisKeyUtil;
import com.mok.baseframe.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    private final OrderInfoMapper orderInfoMapper;
    private final ProductMapper productMapper;
    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final OrderCouponMapper orderCouponMapper;
    private final ProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderCancelProducer orderCancelProducer;
    private final StockUpdateProducer stockUpdateProducer;
    private final OrderPayProducer orderPayProducer;
    private final SecurityUtils securityUtils;

    public OrderServiceImpl(OrderInfoMapper orderInfoMapper,
                            ProductMapper productMapper,
                            CouponMapper couponMapper,
                            UserCouponMapper userCouponMapper,
                            OrderCouponMapper orderCouponMapper,
                            ProductService productService,
                            RedisTemplate<String, Object> redisTemplate,
                            SecurityUtils securityUtils,
                            StockUpdateProducer stockUpdateProducer,
                            OrderCancelProducer orderCancelProducer,
                            OrderPayProducer orderPayProducer){
        this.orderInfoMapper = orderInfoMapper;
        this.productMapper = productMapper;
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
        this.orderCouponMapper = orderCouponMapper;
        this.productService = productService;
        this.redisTemplate = redisTemplate;
        this.securityUtils = securityUtils;
        this.stockUpdateProducer = stockUpdateProducer;
        this.orderCancelProducer = orderCancelProducer;
        this.orderPayProducer = orderPayProducer;
    }
    
    // 订单超时时间（分钟）
    private static final int ORDER_TIMEOUT_MINUTES = 30;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(String userId, String productId, Integer quantity,
                            List<String> couponIds, String remark) {
        // 使用分布式锁防止重复下单
        String lockKey = RedisKeyUtil.getDistributedLockKey("create_order:" + userId);
        String lockValue = UUID.randomUUID().toString();
        
        try {
            // 尝试获取分布式锁，超时时间5秒
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 5, TimeUnit.SECONDS);
            if (lockAcquired == null || !lockAcquired) {
                throw new BusinessException("操作过于频繁，请稍后再试");
            }
            
            // 1. 参数校验
            if (quantity == null || quantity <= 0) {
                throw new BusinessException("购买数量必须大于0");
            }
            
            // 2. 校验商品
            ProductEntity product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            if (product.getStatus() != 1) {
                throw new BusinessException("商品已下架");
            }
            
            // 3. 校验库存（使用Redis预减库存）
            boolean stockReduced = productService.reduceStock(productId, quantity);
            if (!stockReduced) {
                throw new BusinessException("库存不足");
            }
            
            // 4. 计算订单金额
            BigDecimal orderAmount = calculateOrderAmount(productId, quantity, couponIds);
            
            // 5. 生成订单号
            String orderNo = OrderNoGenerator.generateOrderNo();
            
            // 6. 创建订单
            OrderInfoEntity order = new OrderInfoEntity();
            order.setId(IdUtil.simpleUUID());
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setProductId(productId);
            order.setProductName(product.getProductName());
            order.setProductPrice(product.getPrice());
            order.setQuantity(quantity);
            order.setOriginalAmount(product.getPrice().multiply(new BigDecimal(quantity)));
            order.setDiscountAmount(order.getOriginalAmount().subtract(orderAmount));
            order.setPayAmount(orderAmount);
            order.setOrderStatus(1); // 已支付
            order.setPayStatus(2); // 已支付
            order.setPayTime(new Date());
            order.setPayType(1); // 模拟支付方式
            order.setTransactionId("SIM_" + System.currentTimeMillis());
            order.setOrderType(0); // 普通订单
            order.setRemark(remark);
            
            int result = orderInfoMapper.insert(order);
            if (result <= 0) {
                throw new BusinessException("创建订单失败");
            }
            
            // 7. 使用优惠券
            if (couponIds != null && !couponIds.isEmpty()) {
                useCoupons(userId, order.getId(), orderNo, couponIds);
            }
            
            // 8. 发送支付成功消息到MQ，用于后续处理（发货等）
            orderPayProducer.sendOrderPayMessage(orderNo);
            stockUpdateProducer.sendStockReduceMessage(productId, quantity, order.getId(), orderNo);
            
            // 9. 记录操作日志
            logger.info("创建订单成功，订单号：{}，用户ID：{}，商品：{}，数量：{}，金额：{}", 
                       orderNo, userId, product.getProductName(), quantity, orderAmount);
            
            return orderNo;
        } finally {
            // 释放分布式锁（使用Lua脚本保证原子性）
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                              "return redis.call('del', KEYS[1]) " +
                              "else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String confirmOrder(String userId, String productId, Integer quantity,
                             List<String> couponIds, String remark) {
        // 使用分布式锁防止重复确认
        String lockKey = RedisKeyUtil.getDistributedLockKey("confirm_order:" + userId);
        String lockValue = UUID.randomUUID().toString();
        
        try {
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 5, TimeUnit.SECONDS);
            if (lockAcquired == null || !lockAcquired) {
                throw new BusinessException("操作过于频繁，请稍后再试");
            }
            
            // 1. 参数校验
            if (quantity == null || quantity <= 0) {
                throw new BusinessException("购买数量必须大于0");
            }
            
            // 2. 校验商品
            ProductEntity product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            if (product.getStatus() != 1) {
                throw new BusinessException("商品已下架");
            }
            
            // 3. 校验库存（只检查，不扣减）
            String stockKey = RedisKeyUtil.getProductStockKey(productId);
            Object stockObj = redisTemplate.opsForValue().get(stockKey);
            if (stockObj == null || Integer.parseInt(stockObj.toString()) < quantity) {
                throw new BusinessException("库存不足");
            }
            
            // 4. 计算订单金额
            BigDecimal orderAmount = calculateOrderAmount(productId, quantity, couponIds);
            
            // 5. 生成订单号
            String orderNo = OrderNoGenerator.generateOrderNo();
            
            // 6. 创建待支付订单
            OrderInfoEntity order = new OrderInfoEntity();
            order.setId(IdUtil.simpleUUID());
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setProductId(productId);
            order.setProductName(product.getProductName());
            order.setProductPrice(product.getPrice());
            order.setQuantity(quantity);
            order.setOriginalAmount(product.getPrice().multiply(new BigDecimal(quantity)));
            order.setDiscountAmount(order.getOriginalAmount().subtract(orderAmount));
            order.setPayAmount(orderAmount);
            order.setOrderStatus(0); // 待支付
            order.setPayStatus(0); // 未支付
            order.setOrderType(0); // 普通订单
            order.setRemark(remark);
            
            int result = orderInfoMapper.insert(order);
            if (result <= 0) {
                throw new BusinessException("确认订单失败");
            }
            
            // 7. 锁定库存（Redis中预扣）
            redisTemplate.opsForValue().decrement(stockKey, quantity);
            stockUpdateProducer.sendStockLockMessage(productId, quantity, order.getId(), orderNo);
            // 8. 生成订单确认token（用于支付时验证）
            String confirmToken = UUID.randomUUID().toString();
            String tokenKey = RedisKeyUtil.getOrderConfirmTokenKey(userId);
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("orderNo", orderNo);
            tokenData.put("productId", productId);
            tokenData.put("quantity", quantity);
            tokenData.put("couponIds", couponIds);
            tokenData.put("expireTime", System.currentTimeMillis() + ORDER_TIMEOUT_MINUTES * 60 * 1000);
            redisTemplate.opsForHash().putAll(tokenKey + ":" + orderNo, tokenData);
            redisTemplate.expire(tokenKey + ":" + orderNo, ORDER_TIMEOUT_MINUTES, TimeUnit.MINUTES);
            
            // 9. 发送延迟消息，用于订单超时关闭
            orderCancelProducer.sendOrderCancelMessage(orderNo, ORDER_TIMEOUT_MINUTES);

            logger.info("确认订单成功，订单号：{}，用户ID：{}，商品：{}，数量：{}，金额：{}", 
                       orderNo, userId, product.getProductName(), quantity, orderAmount);
            
            return orderNo;
        } finally {
            // 释放分布式锁
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                              "return redis.call('del', KEYS[1]) " +
                              "else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(String orderNo, Integer payType) {
        // 使用分布式锁防止重复支付
        String lockKey = RedisKeyUtil.getDistributedLockKey("pay_order:" + orderNo);
        String lockValue = UUID.randomUUID().toString();
        
        try {
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 5, TimeUnit.SECONDS);
            if (lockAcquired == null || !lockAcquired) {
                throw new BusinessException("支付处理中，请勿重复操作");
            }
            
            // 1. 查询订单
            OrderInfoEntity order = orderInfoMapper.selectByOrderNo(orderNo);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            
            // 2. 检查订单状态（使用乐观锁防止并发支付）
            if (order.getOrderStatus() != 0) { // 不是待支付状态
                throw new BusinessException("订单状态异常，无法支付");
            }
            
            if (order.getPayStatus() != 0) { // 不是未支付状态
                throw new BusinessException("订单支付状态异常");
            }
            
            // 3. 检查订单是否超时
            long orderCreateTime = order.getCreateTime().getTime();
            long currentTime = System.currentTimeMillis();
            long timeoutMillis = ORDER_TIMEOUT_MINUTES * 60 * 1000;
            
            if (currentTime - orderCreateTime > timeoutMillis) {
                // 订单已超时，自动取消
                cancelOrder(orderNo, "支付超时，系统自动取消");
                throw new BusinessException("订单已超时，请重新下单");
            }
            
            // 4. 检查Redis中是否有库存（防止库存不足）
            String stockKey = RedisKeyUtil.getProductStockKey(order.getProductId());
            Object stockObj = redisTemplate.opsForValue().get(stockKey);
            if (stockObj == null || Integer.parseInt(stockObj.toString()) < 0) {
                // 库存不足，取消订单
                cancelOrder(orderNo, "库存不足，支付失败");
                throw new BusinessException("库存不足，支付失败");
            }
            
            // 5. 更新订单状态为已支付（使用乐观锁）
            Date payTime = new Date();
            int updateResult = orderInfoMapper.updatePayStatus(
                order.getId(), 
                0, // 旧支付状态：未支付
                2, // 新支付状态：已支付
                payTime,
                payType,
                "SIM_" + System.currentTimeMillis()
            );
            
            if (updateResult <= 0) {
                throw new BusinessException("支付失败，订单状态已变更");
            }
            
            // 6. 更新订单状态为已支付
            orderInfoMapper.updateOrderStatus(
                order.getId(),
                0, // 旧订单状态：待支付
                1, // 新订单状态：已支付
                null,
                null
            );
            
            // 7. 实际扣减数据库库存
            ProductEntity product = productMapper.selectById(order.getProductId());
            if (product != null) {
                int stockResult = productMapper.reduceStock(
                    order.getProductId(), 
                    order.getQuantity(), 
                    product.getVersion()
                );
                
                if (stockResult <= 0) {
                    // 库存扣减失败，需要回滚
                    throw new BusinessException("库存扣减失败");
                }
            }
            stockUpdateProducer.sendStockReduceMessage(
                    order.getProductId(), order.getQuantity(), order.getId(), orderNo);
            
            // 8. 使用优惠券
            String userId = order.getUserId();
            String tokenKey = RedisKeyUtil.getOrderConfirmTokenKey(userId) + ":" + orderNo;
            Object couponIdsObj = redisTemplate.opsForHash().get(tokenKey, "couponIds");
            if (couponIdsObj != null) {
                @SuppressWarnings("unchecked")
                List<String> couponIds = (List<String>) couponIdsObj;
                if (couponIds != null && !couponIds.isEmpty()) {
                    useCoupons(userId, order.getId(), orderNo, couponIds);
                }
            }
            
            // 9. 删除确认token
            redisTemplate.delete(tokenKey);
            
            // 10. 发送支付成功消息到MQ
            orderPayProducer.sendOrderPayMessage(orderNo);
            
            // 11. 设置支付状态缓存，防止重复支付
            String payStatusKey = RedisKeyUtil.getOrderPayStatusKey(orderNo);
            redisTemplate.opsForValue().set(payStatusKey, "PAID", 1, TimeUnit.HOURS);
            
            logger.info("订单支付成功，订单号：{}，用户ID：{}，支付方式：{}", 
                       orderNo, userId, payType);
            
            return true;
        } finally {
            // 释放分布式锁
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                              "return redis.call('del', KEYS[1]) " +
                              "else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String orderNo, String cancelReason) {
        try {
            // 1. 查询订单
            OrderInfoEntity order = orderInfoMapper.selectByOrderNo(orderNo);
            if (order == null) {
                throw new BusinessException("订单不存在");
            }
            
            // 2. 只能取消待支付的订单
            if (order.getOrderStatus() != 0) {
                throw new BusinessException("订单无法取消，当前状态：" + order.getOrderStatus());
            }
            
            // 3. 更新订单状态为已取消（使用乐观锁）
            Date cancelTime = new Date();
            int result = orderInfoMapper.updateOrderStatus(
                order.getId(),
                0, // 旧订单状态：待支付
                2, // 新订单状态：已取消
                cancelReason,
                cancelTime
            );
            
            if (result <= 0) {
                throw new BusinessException("取消订单失败，订单状态已变更");
            }
            
            // 4. 恢复库存（Redis）
            String stockKey = RedisKeyUtil.getProductStockKey(order.getProductId());
            redisTemplate.opsForValue().increment(stockKey, order.getQuantity());
            stockUpdateProducer.sendStockRestoreMessage(order.getProductId(), order.getQuantity(), order.getId(), orderNo);
            
            // 5. 恢复优惠券（如果有）
            recoverCoupons(order.getUserId(), orderNo);
            
            // 6. 删除确认token
            String tokenKey = RedisKeyUtil.getOrderConfirmTokenKey(order.getUserId()) + ":" + orderNo;
            redisTemplate.delete(tokenKey);
            
            logger.info("订单取消成功，订单号：{}，原因：{}", orderNo, cancelReason);
            
            return true;
        } catch (Exception e) {
            logger.error("取消订单失败，订单号：{}，原因：{}", orderNo, e.getMessage(), e);
            throw new BusinessException("取消订单失败：" + e.getMessage());
        }
    }
    
    @Override
    public OrderInfoEntity getOrderByNo(String orderNo) {
        try {
            return orderInfoMapper.selectByOrderNo(orderNo);
        } catch (Exception e) {
            logger.error("查询订单失败，订单号：{}，异常：{}", orderNo, e.getMessage(), e);
            throw new BusinessException("查询订单失败");
        }
    }
    
    @Override
    public PageResult<OrderInfoEntity> getOrderList(PageParam pageParam, String userId,
                                                   String orderNo, String productName, 
                                                   Integer orderStatus, Integer payStatus) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("orderNo", orderNo);
            params.put("productName", productName);
            params.put("orderStatus", orderStatus);
            params.put("payStatus", payStatus);
            params.put("offset", pageParam.getOffset());
            params.put("limit", pageParam.getPageSize());
            
            List<OrderInfoEntity> list = orderInfoMapper.selectByPage(params);
            Long total = orderInfoMapper.countByPage(params);
            
            return PageResult.success(list, total,pageParam.getPageNum(),pageParam.getPageSize());
        } catch (Exception e) {
            logger.error("查询订单列表失败：{}", e.getMessage(), e);
            throw new BusinessException("查询订单列表失败");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeTimeoutOrders() {
        try {
            // 查询超时未支付订单
            List<OrderInfoEntity> timeoutOrders = orderInfoMapper.selectTimeoutOrders(
                ORDER_TIMEOUT_MINUTES, 0 // 待支付状态
            );
            
            if (timeoutOrders != null && !timeoutOrders.isEmpty()) {
                for (OrderInfoEntity order : timeoutOrders) {
                    try {
                        // 关闭订单
                        cancelOrder(order.getOrderNo(), "超时未支付，系统自动关闭");
                        logger.info("关闭超时订单成功，订单号：{}", order.getOrderNo());
                    } catch (Exception e) {
                        logger.error("关闭超时订单失败，订单号：{}，异常：{}", 
                                   order.getOrderNo(), e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("关闭超时订单任务执行失败：{}", e.getMessage(), e);
        }
    }
    
    @Override
    public BigDecimal calculateOrderAmount(String productId, Integer quantity, List<String> couponIds) {
        try {
            // 1. 获取商品信息
            ProductEntity product = productMapper.selectById(productId);
            if (product == null) {
                throw new BusinessException("商品不存在");
            }
            
            // 2. 计算原始金额
            BigDecimal originalAmount = product.getPrice().multiply(new BigDecimal(quantity));
            BigDecimal finalAmount = originalAmount;
            
            // 3. 计算优惠券折扣
            if (couponIds != null && !couponIds.isEmpty()) {
                BigDecimal totalDiscount = BigDecimal.ZERO;
                String currentUserId = securityUtils.getCurrentUserId();
                
                for (String couponId : couponIds) {
                    // 检查优惠券是否可用
                    CouponEntity coupon = couponMapper.selectById(couponId);
                    if (coupon == null || coupon.getStatus() != 1) {
                        continue;
                    }
                    
                    // 检查优惠券有效期
                    Date now = new Date();
                    if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
                        continue;
                    }
                    
                    // 检查用户是否有该优惠券
                    List<UserCouponEntity> userCoupons = userCouponMapper.selectByUserAndCoupon(
                        currentUserId, couponId);
                    boolean hasUsableCoupon = false;
                    for (UserCouponEntity userCoupon : userCoupons) {
                        if (userCoupon.getStatus() == 0 && 
                            now.after(userCoupon.getStartTime()) && 
                            now.before(userCoupon.getEndTime())) {
                            hasUsableCoupon = true;
                            break;
                        }
                    }
                    
                    if (!hasUsableCoupon) {
                        continue;
                    }
                    
                    // 计算优惠金额
                    BigDecimal discount = calculateCouponDiscount(coupon, originalAmount);
                    if (discount.compareTo(BigDecimal.ZERO) > 0) {
                        totalDiscount = totalDiscount.add(discount);
                    }
                }
                
                // 应用总折扣
                finalAmount = originalAmount.subtract(totalDiscount);
                if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                    finalAmount = BigDecimal.ZERO;
                }
            }
            
            return finalAmount.setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            logger.error("计算订单金额失败：{}", e.getMessage(), e);
            throw new BusinessException("计算订单金额失败");
        }
    }
    
    /**
     * 计算单个优惠券的折扣金额
     */
    private BigDecimal calculateCouponDiscount(CouponEntity coupon, BigDecimal orderAmount) {
        if (coupon.getCouponType() == 1) { // 满减
            if (coupon.getThresholdAmount() != null && 
                orderAmount.compareTo(coupon.getThresholdAmount()) >= 0) {
                return coupon.getDiscountAmount();
            }
        } else if (coupon.getCouponType() == 2) { // 折扣
            if (coupon.getDiscountRate() != null) {
                return orderAmount.multiply(
                    coupon.getDiscountRate().divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                );
            }
        } else if (coupon.getCouponType() == 3) { // 立减
            return coupon.getDiscountAmount();
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * 使用优惠券
     */
    private void useCoupons(String userId, String orderId, String orderNo, List<String> couponIds) {
        try {
            for (String couponId : couponIds) {
                // 1. 查询用户优惠券
                List<UserCouponEntity> userCoupons = userCouponMapper.selectByUserAndCoupon(userId, couponId);
                UserCouponEntity usableCoupon = null;
                
                for (UserCouponEntity userCoupon : userCoupons) {
                    if (userCoupon.getStatus() == 0) { // 未使用
                        Date now = new Date();
                        if (now.after(userCoupon.getStartTime()) && now.before(userCoupon.getEndTime())) {
                            usableCoupon = userCoupon;
                            break;
                        }
                    }
                }
                
                if (usableCoupon == null) {
                    continue;
                }
                
                // 2. 更新用户优惠券状态
                usableCoupon.setStatus(1); // 已使用
                usableCoupon.setOrderId(orderId);
                usableCoupon.setUseTime(new Date());
                userCouponMapper.update(usableCoupon);
                
                // 3. 查询优惠券信息
                CouponEntity coupon = couponMapper.selectById(couponId);
                if (coupon != null) {
                    // 4. 扣减优惠券库存
                    couponMapper.reduceCouponStock(couponId, 1, coupon.getVersion());
                    
                    // 5. 记录订单优惠券使用记录
                    OrderCouponEntity orderCoupon = new OrderCouponEntity();
                    orderCoupon.setId(IdUtil.simpleUUID());
                    orderCoupon.setOrderId(orderId);
                    orderCoupon.setOrderNo(orderNo);
                    orderCoupon.setUserCouponId(usableCoupon.getId());
                    orderCoupon.setCouponId(couponId);
                    orderCoupon.setCouponName(coupon.getCouponName());
                    orderCoupon.setCouponType(coupon.getCouponType());
                    
                    // 计算该优惠券的折扣金额
                    BigDecimal discount = calculateCouponDiscount(coupon, 
                        usableCoupon.getOrderId() != null ? 
                        orderInfoMapper.selectById(usableCoupon.getOrderId()).getOriginalAmount() : 
                        BigDecimal.ZERO);
                    orderCoupon.setDiscountAmount(discount);
                    
                    orderCouponMapper.insert(orderCoupon);
                }
            }
        } catch (Exception e) {
            logger.error("使用优惠券失败：{}", e.getMessage(), e);
            throw new BusinessException("使用优惠券失败");
        }
    }
    
    /**
     * 恢复优惠券
     */
    private void recoverCoupons(String userId, String orderNo) {
        try {
            // 1. 查询订单使用的优惠券
            List<OrderCouponEntity> orderCoupons = orderCouponMapper.selectByOrderNo(orderNo);
            if (orderCoupons == null || orderCoupons.isEmpty()) {
                return;
            }
            
            for (OrderCouponEntity orderCoupon : orderCoupons) {
                // 2. 恢复用户优惠券状态
                UserCouponEntity userCoupon = userCouponMapper.selectById(orderCoupon.getUserCouponId());
                if (userCoupon != null && userCoupon.getStatus() == 1) { // 已使用
                    userCoupon.setStatus(0); // 恢复为未使用
                    userCoupon.setOrderId(null);
                    userCoupon.setUseTime(null);
                    userCouponMapper.update(userCoupon);
                }
                
                // 3. 恢复优惠券库存
                CouponEntity coupon = couponMapper.selectById(orderCoupon.getCouponId());
                if (coupon != null) {
                    couponMapper.restoreCouponStock(coupon.getId(), 1, coupon.getVersion());
                }
            }
        } catch (Exception e) {
            logger.error("恢复优惠券失败：{}", e.getMessage(), e);
        }
    }
}