package com.mok.baseframe.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mok.baseframe.common.BusinessException;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.dao.CouponMapper;
import com.mok.baseframe.dao.UserCouponMapper;
import com.mok.baseframe.entity.CouponEntity;
import com.mok.baseframe.entity.UserCouponEntity;
import com.mok.baseframe.order.service.CouponService;
import com.mok.baseframe.order.util.OrderNoGenerator;
import com.mok.baseframe.order.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CouponServiceImpl implements CouponService {

    private static final Logger logger = LoggerFactory.getLogger(CouponServiceImpl.class);

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    public CouponServiceImpl(CouponMapper couponMapper,
                             UserCouponMapper userCouponMapper,
                             RedisTemplate<String, Object> redisTemplate){
        this.couponMapper = couponMapper;
        this.userCouponMapper = userCouponMapper;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addCoupon(CouponEntity coupon) {
        try {
            // 参数校验
            if (coupon.getTotalQuantity() == null || coupon.getTotalQuantity() <= 0) {
                throw new BusinessException("发行数量必须大于0");
            }

            if (coupon.getRemainingQuantity() == null) {
                coupon.setRemainingQuantity(coupon.getTotalQuantity());
            }

            if (coupon.getStartTime() == null || coupon.getEndTime() == null) {
                throw new BusinessException("必须设置生效时间");
            }

            if (coupon.getEndTime().before(coupon.getStartTime())) {
                throw new BusinessException("结束时间不能早于开始时间");
            }

            // 插入优惠券
            coupon.setVersion(0);
            coupon.setStatus(1);
            int result = couponMapper.insert(coupon);
            if (result <= 0) {
                throw new BusinessException("添加优惠券失败");
            }

            // 将库存同步到Redis缓存
            String stockKey = RedisKeyUtil.getCouponStockKey(coupon.getId());
            redisTemplate.opsForValue().set(stockKey, coupon.getRemainingQuantity());

            // 设置过期时间为优惠券结束时间
            long expireTime = coupon.getEndTime().getTime() - System.currentTimeMillis();
            if (expireTime > 0) {
                redisTemplate.expire(stockKey, expireTime, TimeUnit.MILLISECONDS);
            }

            logger.info("添加优惠券成功，优惠券ID：{}，名称：{}", coupon.getId(), coupon.getCouponName());
        } catch (Exception e) {
            logger.error("添加优惠券失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCoupon(CouponEntity coupon) {
        try {
            CouponEntity oldCoupon = couponMapper.selectById(coupon.getId());
            if (oldCoupon == null) {
                throw new BusinessException("优惠券不存在");
            }

            // 更新优惠券
            int result = couponMapper.update(coupon);
            if (result <= 0) {
                throw new BusinessException("更新优惠券失败");
            }

            // 如果库存有变化，更新Redis缓存
            if (coupon.getRemainingQuantity() != null &&
                    !coupon.getRemainingQuantity().equals(oldCoupon.getRemainingQuantity())) {
                String stockKey = RedisKeyUtil.getCouponStockKey(coupon.getId());
                redisTemplate.opsForValue().set(stockKey, coupon.getRemainingQuantity());

                // 更新过期时间
                long expireTime = coupon.getEndTime().getTime() - System.currentTimeMillis();
                if (expireTime > 0) {
                    redisTemplate.expire(stockKey, expireTime, TimeUnit.MILLISECONDS);
                }
            }

            logger.info("更新优惠券成功，优惠券ID：{}", coupon.getId());
        } catch (Exception e) {
            logger.error("更新优惠券失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCoupon(String id) {
        try {
            CouponEntity coupon = couponMapper.selectById(id);
            if (coupon == null) {
                throw new BusinessException("优惠券不存在");
            }

            int result = couponMapper.deleteById(id);
            if (result <= 0) {
                throw new BusinessException("删除优惠券失败");
            }

            // 删除Redis缓存
            String stockKey = RedisKeyUtil.getCouponStockKey(id);
            redisTemplate.delete(stockKey);

            logger.info("删除优惠券成功，优惠券ID：{}", id);
        } catch (Exception e) {
            logger.error("删除优惠券失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public CouponEntity getCouponById(String id) {
        try {
            return couponMapper.selectById(id);
        } catch (Exception e) {
            logger.error("查询优惠券失败：{}", e.getMessage(), e);
            throw new BusinessException("查询优惠券失败");
        }
    }

    @Override
    public PageResult<CouponEntity> getCouponList(PageParam pageParam) {
        try {
//            Map<String, Object> params = new HashMap<>();
//            params.put("couponName", couponName);
//            params.put("couponType", couponType);
//            params.put("status", status);
//            params.put("available", available);
//            params.put("offset", (pageParam.getPageNum() - 1) * pageParam.getPageSize());
//            params.put("limit", pageParam.getPageSize());

            List<CouponEntity> list = couponMapper.selectByPage(pageParam);
            Long total = couponMapper.countByPage(pageParam);

            return PageResult.success(list, total, pageParam.getPageNum(), pageParam.getPageSize());
        } catch (Exception e) {
            logger.error("查询优惠券列表失败：{}", e.getMessage(), e);
            throw new BusinessException("查询优惠券列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean grabCoupon(String userId, String couponId) {
        String lockKey = RedisKeyUtil.getDistributedLockKey("grab_coupon:" + couponId);
        String lockValue = UUID.randomUUID().toString();

        try {
            // 获取分布式锁
            Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, lockValue, 3, TimeUnit.SECONDS);
            if (lockAcquired == null || !lockAcquired) {
                throw new BusinessException("抢券过于火爆，请稍后再试");
            }

            // 1. 检查优惠券
            CouponEntity coupon = couponMapper.selectById(couponId);
            if (coupon == null || coupon.getStatus() != 1) {
                throw new BusinessException("优惠券不存在或已失效");
            }

            Date now = new Date();
            if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
                throw new BusinessException("不在优惠券有效期内");
            }

            // 2. 检查用户领取数量限制
            List<UserCouponEntity> userCoupons = userCouponMapper.selectByUserAndCoupon(userId, couponId);
            int userGrabCount = 0;
            for (UserCouponEntity userCoupon : userCoupons) {
                if (userCoupon.getStatus() == 0 || userCoupon.getStatus() == 1) { // 未使用或已使用
                    userGrabCount++;
                }
            }

            if (userGrabCount >= coupon.getPerLimit()) {
                throw new BusinessException("已达到领取上限");
            }

            // 3. Redis预减库存
            String stockKey = RedisKeyUtil.getCouponStockKey(couponId);
            Long remaining = redisTemplate.opsForValue().decrement(stockKey, 1);

            if (remaining != null && remaining >= 0) {
                // Redis预减成功
                try {
                    // 4. 扣减数据库库存（使用乐观锁）
                    int result = couponMapper.reduceCouponStock(couponId, 1, coupon.getVersion());
                    if (result > 0) {
                        // 5. 创建用户优惠券记录
                        UserCouponEntity userCoupon = new UserCouponEntity();
                        userCoupon.setId(IdUtil.simpleUUID());
                        userCoupon.setUserId(userId);
                        userCoupon.setCouponId(couponId);
                        userCoupon.setCouponCode(OrderNoGenerator.generateCouponCode());
                        userCoupon.setStatus(0); // 未使用
                        userCoupon.setStartTime(coupon.getStartTime());
                        userCoupon.setEndTime(coupon.getEndTime());

                        userCouponMapper.insert(userCoupon);

                        logger.info("抢券成功，用户ID：{}，优惠券ID：{}，优惠券码：{}",
                                userId, couponId, userCoupon.getCouponCode());
                        return true;
                    } else {
                        // 数据库扣减失败，恢复Redis库存
                        redisTemplate.opsForValue().increment(stockKey, 1);
                        throw new BusinessException("优惠券已抢完");
                    }
                } catch (Exception e) {
                    // 数据库操作异常，恢复Redis库存
                    redisTemplate.opsForValue().increment(stockKey, 1);
                    logger.error("抢券异常，恢复Redis库存，优惠券ID：{}，用户ID：{}，异常：{}",
                            couponId, userId, e.getMessage(), e);
                    throw e;
                }
            } else {
                // Redis库存不足，恢复刚才的扣减
                if (remaining != null && remaining < 0) {
                    redisTemplate.opsForValue().increment(stockKey, 1);
                }
                throw new BusinessException("优惠券已抢完");
            }
        } finally {
            // 释放分布式锁
            String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end";
            org.springframework.data.redis.core.script.DefaultRedisScript<Long> redisScript =
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>(luaScript, Long.class);
            redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
        }
    }

    @Override
    public List<UserCouponEntity> getUserCoupons(String userId, Integer status) {
        try {
            return userCouponMapper.selectByUser(userId, status);
        } catch (Exception e) {
            logger.error("查询用户优惠券失败：{}", e.getMessage(), e);
            throw new BusinessException("查询用户优惠券失败");
        }
    }

    @Override
    public List<CouponEntity> getAvailableCoupons() {
        try {
            return couponMapper.selectAvailableCoupons();
        } catch (Exception e) {
            logger.error("查询可用优惠券列表失败：{}", e.getMessage(), e);
            throw new BusinessException("查询可用优惠券列表失败");
        }
    }

    @Override
    public void initCouponStockToRedis() {
        try {
            List<CouponEntity> coupons = couponMapper.selectAvailableCoupons();
            for (CouponEntity coupon : coupons) {
                String stockKey = RedisKeyUtil.getCouponStockKey(coupon.getId());
                redisTemplate.opsForValue().set(stockKey, coupon.getRemainingQuantity());

                // 设置过期时间为优惠券结束时间
                long expireTime = coupon.getEndTime().getTime() - System.currentTimeMillis();
                if (expireTime > 0) {
                    redisTemplate.expire(stockKey, expireTime, TimeUnit.MILLISECONDS);
                }
            }

            logger.info("初始化优惠券库存到Redis完成，共初始化{}个优惠券", coupons.size());
        } catch (Exception e) {
            logger.error("初始化优惠券库存到Redis失败：{}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredCoupons() {
        try {
            // 更新过期优惠券状态
            int updated = userCouponMapper.updateExpiredCoupons();
            logger.info("清理过期优惠券完成，共清理{}个", updated);
        } catch (Exception e) {
            logger.error("清理过期优惠券失败：{}", e.getMessage(), e);
        }
    }
}