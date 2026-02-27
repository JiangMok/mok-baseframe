package com.mok.baseframe.order.service;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.entity.CouponEntity;
import com.mok.baseframe.entity.ProductCouponEntity;
import com.mok.baseframe.entity.ProductEntity;
import com.mok.baseframe.entity.UserCouponEntity;

import java.util.List;

public interface CouponService {
    /**
     * 添加优惠券
     */
    void addCoupon(CouponEntity coupon);
    
    /**
     * 更新优惠券
     */
    void updateCoupon(CouponEntity coupon);
    
    /**
     * 删除优惠券
     */
    void deleteCoupon(String id);
    
    /**
     * 根据ID查询优惠券
     */
    CouponEntity getCouponById(String id);
    
    /**
     * 分页查询优惠券列表
     */
    PageResult<CouponEntity> getCouponList(PageParam pageParam);
    
    /**
     * 抢优惠券
     */
    boolean grabCoupon(String userId, String couponId);
    
    /**
     * 查询用户优惠券
     */
    List<UserCouponEntity> getUserCoupons(String userId, Integer status);
    
    /**
     * 获取可用优惠券列表
     */
    List<CouponEntity> getAvailableCoupons();
    
    /**
     * 初始化优惠券库存到Redis
     */
    void initCouponStockToRedis();
    
    /**
     * 清理过期优惠券
     */
    void cleanExpiredCoupons();

    /**
     * 通过商品ID查询该商品拥有的优惠券
     */
    List<CouponEntity> getByProductId(String productId);

    void saveProductCoupons(ProductCouponEntity productCouponEntity);
}