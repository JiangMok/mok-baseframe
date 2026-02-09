package com.mok.baseframe.dao;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.entity.CouponEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
/**
 * 优惠券 mapper
 *
 * @author: mok
 * @date: 2026/2/4
 */
@Mapper
public interface CouponMapper {
    // 插入优惠券
    int insert(CouponEntity coupon);

    // 根据ID查询优惠券
    CouponEntity selectById(String id);

    // 更新优惠券
    int update(CouponEntity coupon);

    // 删除优惠券
    int deleteById(String id);

    // 分页查询优惠券列表 >>> 手动分页
    List<CouponEntity> selectByPage(PageParam params);

    // 查询优惠券总数
    Long countByPage(PageParam params);

    // 扣减优惠券库存（带乐观锁）
    int reduceCouponStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    // 恢复优惠券库存
    int restoreCouponStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    // 查询可用的优惠券列表
    List<CouponEntity> selectAvailableCoupons();
}
