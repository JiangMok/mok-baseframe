package com.mok.baseframe.dao;

import com.mok.baseframe.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
/**
 * 商品信息 mapper
 *
 * @author: mok
 * @date: 2026/2/4
 */
@Mapper
public interface ProductMapper {
    // 插入商品
    int insert(ProductEntity product);

    // 根据ID查询商品
    ProductEntity selectById(String id);

    // 更新商品信息
    int update(ProductEntity product);

    // 删除商品（逻辑删除）
    int deleteById(String id);

    // 分页查询商品列表
    List<ProductEntity> selectByPage(Map<String, Object> params);

    // 查询商品总数
    long countByPage(Map<String, Object> params);

    // 扣减库存（带乐观锁）
    int reduceStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    // 恢复库存（带乐观锁）
    int restoreStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    // 扣减秒杀库存
    int reduceSeckillStock(@Param("id") String id, @Param("quantity") Integer quantity, @Param("version") Integer version);

    // 查询秒杀商品列表
    List<ProductEntity> selectSeckillProducts();
}

