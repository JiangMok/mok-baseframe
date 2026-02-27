package com.mok.baseframe.order.service;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.entity.ProductEntity;

import java.util.List;

public interface ProductService {
    /**
     * 添加商品
     */
    void addProduct(ProductEntity product);
    
    /**
     * 更新商品
     */
    void updateProduct(ProductEntity product);

    /**
     * 设置秒杀信息
     */
    void setSeckill(ProductEntity product);

    /**
     * 清空秒杀信息
     */
    void clearSeckill(String id);

    /**
     * 删除商品
     */
    void deleteProduct(String id);
    
    /**
     * 根据ID查询商品
     */
    ProductEntity getProductById(String id);
    
    /**
     * 分页查询商品列表
     */
    PageResult<ProductEntity> getProductList(PageParam pageParam);
    
    /**
     * 扣减商品库存
     */
    boolean reduceStock(String productId, Integer quantity);
    
    /**
     * 恢复商品库存
     */
    boolean restoreStock(String productId, Integer quantity);
    
    /**
     * 获取秒杀商品列表
     */
    List<ProductEntity> getSeckillProducts();
    
    /**
     * 初始化商品库存到Redis
     */
    void initProductStockToRedis();
}