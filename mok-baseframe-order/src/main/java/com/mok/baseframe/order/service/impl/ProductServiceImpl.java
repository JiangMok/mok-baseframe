package com.mok.baseframe.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mok.baseframe.common.BusinessException;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.dao.ProductMapper;
import com.mok.baseframe.entity.ProductEntity;
import com.mok.baseframe.order.service.ProductService;
import com.mok.baseframe.order.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public ProductServiceImpl(ProductMapper productMapper,
                              RedisTemplate<String, Object> redisTemplate) {
        this.productMapper = productMapper;
        this.redisTemplate = redisTemplate;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProduct(ProductEntity product) {
        try {
            // 参数校验
            if (product.getStock() == null || product.getStock() < 0) {
                throw new BusinessException("库存数量不能为负数");
            }
            if (product.getSeckillStock() == null) {
                product.setSeckillStock(0);
            }

            // 插入商品
            product.setId(IdUtil.simpleUUID());
            product.setVersion(0);
            int result = productMapper.insert(product);
            if (result <= 0) {
                throw new BusinessException("添加商品失败");
            }

            // 将库存同步到Redis缓存
            if (product.getStock() > 0) {
                String stockKey = RedisKeyUtil.getProductStockKey(product.getId());
                redisTemplate.opsForValue().set(stockKey, product.getStock());
                redisTemplate.expire(stockKey, 7, TimeUnit.DAYS);
            }

            if (product.getSeckillStock() > 0 && product.getSeckillStartTime() != null
                    && product.getSeckillEndTime() != null) {
                String seckillKey = RedisKeyUtil.getSeckillStockKey(product.getId());
                redisTemplate.opsForValue().set(seckillKey, product.getSeckillStock());
                // 秒杀库存缓存设置过期时间为秒杀结束时间
                long expireTime = product.getSeckillEndTime().getTime() - System.currentTimeMillis();
                if (expireTime > 0) {
                    redisTemplate.expire(seckillKey, expireTime, TimeUnit.MILLISECONDS);
                }
            }

            logger.info("添加商品成功，商品ID：{}，商品名称：{}", product.getId(), product.getProductName());
        } catch (Exception e) {
            logger.error("添加商品失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductEntity product) {
        logger.info("******************************************** updateProduct");
        try {
            ProductEntity oldProduct = productMapper.selectById(product.getId());
            if (oldProduct == null) {
                throw new BusinessException("商品不存在");
            }

            // 更新商品信息
            int result = productMapper.update(product);
            if (result <= 0) {
                throw new BusinessException("更新商品失败");
            }

            // 如果库存有变化，更新Redis缓存
            if (product.getStock() != null && !product.getStock().equals(oldProduct.getStock())) {
                String stockKey = RedisKeyUtil.getProductStockKey(product.getId());
                redisTemplate.opsForValue().set(stockKey, product.getStock());
                redisTemplate.expire(stockKey, 7, TimeUnit.DAYS);
            }

            // 如果秒杀库存有变化，更新Redis缓存
            if (product.getSeckillStock() != null && !product.getSeckillStock().equals(oldProduct.getSeckillStock())) {
                String seckillKey = RedisKeyUtil.getSeckillStockKey(product.getId());
                redisTemplate.opsForValue().set(seckillKey, product.getSeckillStock());
            }

            logger.info("更新商品成功，商品ID：{}", product.getId());
        } catch (Exception e) {
            logger.error("更新商品失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void setSeckill(ProductEntity product) {
        productMapper.update(product);
    }

    @Override
    public void clearSeckill(String id) {
        productMapper.clearSeckill(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(String id) {
        try {
            ProductEntity product = productMapper.selectById(id);
            if (product == null) {
                throw new BusinessException("商品不存在");
            }

            int result = productMapper.deleteById(id);
            if (result <= 0) {
                throw new BusinessException("删除商品失败");
            }

            // 删除Redis缓存
            String stockKey = RedisKeyUtil.getProductStockKey(id);
            String seckillKey = RedisKeyUtil.getSeckillStockKey(id);
            redisTemplate.delete(stockKey);
            redisTemplate.delete(seckillKey);

            logger.info("删除商品成功，商品ID：{}", id);
        } catch (Exception e) {
            logger.error("删除商品失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ProductEntity getProductById(String id) {
        try {
            // 先尝试从Redis获取
            String stockKey = RedisKeyUtil.getProductStockKey(id);
            Object stockObj = redisTemplate.opsForValue().get(stockKey);

            ProductEntity product = productMapper.selectById(id);
            if (product != null && stockObj != null) {
                // 使用Redis中的库存，避免读取旧数据
                product.setStock(Integer.parseInt(stockObj.toString()));
            }

            return product;
        } catch (Exception e) {
            logger.error("查询商品失败：{}", e.getMessage(), e);
            throw new BusinessException("查询商品失败");
        }
    }

    @Override
    public PageResult<ProductEntity> getProductList(PageParam pageParam) {
        try {
            List<ProductEntity> list = productMapper.selectByPage(pageParam);
            long total = productMapper.countByPage(pageParam);

            return PageResult.success(list, total, pageParam.getPageNum(), pageParam.getPageSize());
        } catch (Exception e) {
            logger.error("查询商品列表失败：{}", e.getMessage(), e);
            throw new BusinessException("查询商品列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reduceStock(String productId, Integer quantity) {
        try {
            // 1. 先从Redis预减库存
//            String stockKey = RedisKeyUtil.getProductStockKey(productId);
            String stockKey = RedisKeyUtil.getSeckillStockKey(productId);
            Long stock = redisTemplate.opsForValue().decrement(stockKey, quantity);
            if (stock != null && stock >= 0) {
                // Redis预减库存成功
                try {
                    // 2. 异步更新数据库库存（使用乐观锁）
                    ProductEntity product = productMapper.selectById(productId);
                    if (product != null) {
                        int result = productMapper.reduceStock(productId, quantity, product.getVersion());
                        if (result > 0) {
                            logger.info("扣减库存成功，商品ID：{}，数量：{}", productId, quantity);
                            return true;
                        } else {
                            // 数据库扣减失败，恢复Redis库存
                            redisTemplate.opsForValue().increment(stockKey, quantity);
                            logger.warn("数据库扣减库存失败，恢复Redis库存，商品ID：{}", productId);
                            return false;
                        }
                    }
                } catch (Exception e) {
                    // 数据库操作异常，恢复Redis库存
                    redisTemplate.opsForValue().increment(stockKey, quantity);
                    logger.error("扣减库存异常，恢复Redis库存，商品ID：{}，异常：{}", productId, e.getMessage(), e);
                    throw e;
                }
            } else {

                // Redis库存不足，恢复刚才的扣减
                if (stock != null && stock < 0) {
                    redisTemplate.opsForValue().increment(stockKey, quantity);
                }
                logger.warn("库存不足，商品ID：{}，请求数量：{}", productId, quantity);
                return false;
            }

            return false;
        } catch (Exception e) {
            logger.error("扣减库存失败：{}", e.getMessage(), e);
            throw new BusinessException("扣减库存失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreStock(String productId, Integer quantity) {
        try {
            // 1. 恢复Redis库存
            String stockKey = RedisKeyUtil.getProductStockKey(productId);
            redisTemplate.opsForValue().increment(stockKey, quantity);

            // 2. 恢复数据库库存
            ProductEntity product = productMapper.selectById(productId);
            if (product != null) {
                int result = productMapper.restoreStock(productId, quantity, product.getVersion());
                if (result > 0) {
                    logger.info("恢复库存成功，商品ID：{}，数量：{}", productId, quantity);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            logger.error("恢复库存失败：{}", e.getMessage(), e);
            throw new BusinessException("恢复库存失败");
        }
    }

    @Override
    public List<ProductEntity> getSeckillProducts() {
        try {
            return productMapper.selectSeckillProducts();
        } catch (Exception e) {
            logger.error("查询秒杀商品列表失败：{}", e.getMessage(), e);
            throw new BusinessException("查询秒杀商品列表失败");
        }
    }

    @Override
    public void initProductStockToRedis() {
        try {
            List<ProductEntity> products = productMapper.selectAllUpProduct();
            for (ProductEntity product : products) {
                String stockKey = RedisKeyUtil.getProductStockKey(product.getId());
                redisTemplate.opsForValue().set(stockKey, product.getStock());
                redisTemplate.expire(stockKey, 7, TimeUnit.DAYS);

                // 如果是秒杀商品，初始化秒杀库存
                if (product.getSeckillStock() > 0 && product.getSeckillStartTime() != null
                        && product.getSeckillEndTime() != null) {
                    String seckillKey = RedisKeyUtil.getSeckillStockKey(product.getId());
                    redisTemplate.opsForValue().set(seckillKey, product.getSeckillStock());
                    long expireTime = product.getSeckillEndTime().getTime() - System.currentTimeMillis();
                    if (expireTime > 0) {
                        redisTemplate.expire(seckillKey, expireTime, TimeUnit.MILLISECONDS);
                    }
                }
            }

            logger.info("初始化商品库存到Redis完成，共初始化{}个商品", products.size());
        } catch (Exception e) {
            logger.error("初始化商品库存到Redis失败：{}", e.getMessage(), e);
        }
    }
}