package com.mok.baseframe.dao;

import com.mok.baseframe.entity.UserCouponEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户-优惠券 关联信息 mapper
 *
 * @author: mok
 * @date: 2026/2/4 23:23
 **/
@Mapper
public interface UserCouponMapper {
    // 插入用户优惠券
    int insert(UserCouponEntity userCoupon);

    // 根据ID查询用户优惠券
    UserCouponEntity selectById(String id);

    // 更新用户优惠券
    int update(UserCouponEntity userCoupon);

    // 根据用户和优惠券ID查询
    List<UserCouponEntity> selectByUserAndCoupon(@Param("userId") String userId, @Param("couponId") String couponId);

    // 查询用户可用优惠券
    List<UserCouponEntity> selectAvailableByUser(@Param("userId") String userId);

    // 查询用户优惠券列表
    List<UserCouponEntity> selectByUser(@Param("userId") String userId, @Param("status") Integer status);

    // 批量插入用户优惠券
    int batchInsert(List<UserCouponEntity> userCoupons);

    // 更新过期优惠券状态
    int updateExpiredCoupons();
}
