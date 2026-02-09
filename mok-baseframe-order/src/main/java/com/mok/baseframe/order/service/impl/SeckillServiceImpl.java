package com.mok.baseframe.order.service.impl;

import com.mok.baseframe.common.R;
import com.mok.baseframe.dao.ProductMapper;
import com.mok.baseframe.entity.ProductEntity;
import com.mok.baseframe.order.mq.producer.StockUpdateProducer;
import com.mok.baseframe.order.service.OrderService;
import com.mok.baseframe.order.service.SeckillService;
import com.mok.baseframe.order.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {

    private static final Logger logger = LoggerFactory.getLogger(SeckillServiceImpl.class);
    // 秒杀验证码有效期（秒）
    private static final int VERIFY_CODE_EXPIRE = 60;
    // 用户秒杀限流（一段时间内只能秒杀一次）
    // 5秒内只能秒杀一次
    private static final int USER_SECKILL_LIMIT = 5;

    private final ProductMapper productMapper;
    private final OrderService orderService;
    private final StockUpdateProducer stockUpdateProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    public SeckillServiceImpl(ProductMapper productMapper,
                              OrderService orderService,
                              StockUpdateProducer stockUpdateProducer,
                              RedisTemplate<String, Object> redisTemplate) {
        this.productMapper = productMapper;
        this.orderService = orderService;
        this.stockUpdateProducer = stockUpdateProducer;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public R<String> seckillOrder(String userId, String productId, Integer quantity) {
        // 1. 参数校验
        if (quantity == null || quantity <= 0) {
            return R.error("购买数量必须大于0");
        }

        // 2. 分布式锁key
        String lockKey = RedisKeyUtil.getDistributedLockKey("seckill:" + productId);
        String lockValue = UUID.randomUUID().toString();

        try {
            // 3. 尝试获取分布式锁
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, lockValue, 3, TimeUnit.SECONDS);
            if (lockAcquired == null || !lockAcquired) {
                return R.error("秒杀过于火爆，请稍后再试");
            }

            // 4. 用户秒杀限流
            String userLimitKey = RedisKeyUtil.getUserSeckillLimitKey(userId, productId);
            Boolean canSeckill = redisTemplate.opsForValue().setIfAbsent(
                    userLimitKey, "1", USER_SECKILL_LIMIT, TimeUnit.SECONDS);
            if (canSeckill == null || !canSeckill) {
                return R.error("操作过于频繁，请稍后再试");
            }

            // 5. 校验秒杀商品
            ProductEntity product = productMapper.selectById(productId);
            if (product == null) {
                return R.error("商品不存在");
            }

            Date now = new Date();
            if (product.getSeckillStartTime() == null || product.getSeckillEndTime() == null ||
                    now.before(product.getSeckillStartTime()) || now.after(product.getSeckillEndTime())) {
                return R.error("不在秒杀时间内");
            }

            if (product.getSeckillStock() <= 0) {
                return R.error("秒杀商品已售罄");
            }

            // 6. Redis预减秒杀库存（使用Lua脚本保证原子性）
            String seckillStockKey = RedisKeyUtil.getSeckillStockKey(productId);
            String luaScript = "local stock = redis.call('get', KEYS[1]) " +
                    "if stock and tonumber(stock) >= tonumber(ARGV[1]) then " +
                    "   redis.call('decrby', KEYS[1], ARGV[1]) " +
                    "   return 1 " +
                    "else " +
                    "   return 0 " +
                    "end";

            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
            Long result = redisTemplate.execute(redisScript,
                    Collections.singletonList(seckillStockKey), quantity.toString());

            if (result == 0) {
                return R.error("秒杀商品已售罄");
            }

            // 7. 创建秒杀订单（异步处理，放入消息队列）
            // 这里简化处理，直接创建订单
            try {
                String orderNo = orderService.createOrder(userId, productId, quantity, null, "秒杀订单");
                stockUpdateProducer.sendSeckillStockReduceMessage(productId, quantity, null, orderNo);
                return R.ok("秒杀成功", orderNo);
            } catch (Exception e) {
                // 创建订单失败，恢复Redis库存
                redisTemplate.opsForValue().increment(seckillStockKey, quantity);
                logger.error("创建秒杀订单失败，恢复库存，商品ID：{}，用户ID：{}，异常：{}",
                        productId, userId, e.getMessage(), e);
                return R.error("秒杀失败：" + e.getMessage());
            }

        } finally {
            // 8. 释放分布式锁
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
        }
    }

    @Override
    public R<String> getSeckillVerifyCode(String userId, String productId) {
        try {
            // 1. 校验商品是否存在且是秒杀商品
            ProductEntity product = productMapper.selectById(productId);
            if (product == null) {
                return R.error("商品不存在");
            }

            Date now = new Date();
            if (product.getSeckillStartTime() == null || product.getSeckillEndTime() == null) {
                return R.error("该商品不是秒杀商品");
            }

            // 2. 检查秒杀是否开始
            if (now.before(product.getSeckillStartTime())) {
                return R.error("秒杀尚未开始");
            }

            if (now.after(product.getSeckillEndTime())) {
                return R.error("秒杀已结束");
            }

            // 3. 生成验证码（6位随机数字）
            String verifyCode = String.format("%06d", (int) (Math.random() * 1000000));

            // 4. 将验证码存入Redis，设置过期时间
            String verifyKey = "seckill:verify:code:" + userId + ":" + productId;
            redisTemplate.opsForValue().set(verifyKey, verifyCode, VERIFY_CODE_EXPIRE, TimeUnit.SECONDS);

            return R.ok("获取成功", verifyCode);
        } catch (Exception e) {
            logger.error("获取秒杀验证码失败：{}", e.getMessage(), e);
            return R.error("获取验证码失败");
        }
    }

    @Override
    public boolean verifySeckillCode(String userId, String productId, String verifyCode) {
        try {
            String verifyKey = "seckill:verify:code:" + userId + ":" + productId;
            String storedCode = (String) redisTemplate.opsForValue().get(verifyKey);

            if (storedCode == null) {
                return false;
            }

            return storedCode.equals(verifyCode);
        } catch (Exception e) {
            logger.error("校验秒杀验证码失败：{}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void initSeckillStockToRedis() {
        try {
            // 查询所有秒杀商品
            List<ProductEntity> seckillProducts = productMapper.selectSeckillProducts();

            for (ProductEntity product : seckillProducts) {
                String seckillKey = RedisKeyUtil.getSeckillStockKey(product.getId());
                redisTemplate.opsForValue().set(seckillKey, product.getSeckillStock());

                // 设置过期时间为秒杀结束时间
                long expireTime = product.getSeckillEndTime().getTime() - System.currentTimeMillis();
                if (expireTime > 0) {
                    redisTemplate.expire(seckillKey, expireTime, TimeUnit.MILLISECONDS);
                }
            }

            logger.info("初始化秒杀库存到Redis完成，共初始化{}个秒杀商品", seckillProducts.size());
        } catch (Exception e) {
            logger.error("初始化秒杀库存到Redis失败：{}", e.getMessage(), e);
        }
    }
}