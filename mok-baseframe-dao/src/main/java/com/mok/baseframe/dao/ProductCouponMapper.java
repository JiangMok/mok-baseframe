package com.mok.baseframe.dao;

import com.mok.baseframe.entity.ProductCouponEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductCouponMapper {

    void insertBatch(List<ProductCouponEntity> productCouponList);

    void deleteByProductId(@Param("productId") String productId);
}
