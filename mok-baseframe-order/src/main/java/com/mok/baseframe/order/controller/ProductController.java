package com.mok.baseframe.order.controller;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.entity.ProductEntity;
import com.mok.baseframe.order.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 添加商品
     */
    @PostMapping("/add")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:add')")
    public R<String> addProduct(@RequestBody ProductEntity product) {
        productService.addProduct(product);
        return R.ok("添加商品成功");
    }

    /**
     * 更新商品
     */
    @PostMapping("/update")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:update')")
    public R<String> updateProduct(@RequestBody ProductEntity product) {
        productService.updateProduct(product);
        return R.ok("更新商品成功");
    }

    /**
     * 删除商品
     */
    @PostMapping("/delete/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:delete')")
    public R<String> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return R.ok("删除商品成功");
    }

    /**
     * 查询商品详情
     */
    @GetMapping("/detail/{id}")
    public R<ProductEntity> getProductDetail(@PathVariable String id) {
        ProductEntity product = productService.getProductById(id);
        return R.ok("查询成功", product);
    }

    /**
     * 分页查询商品列表
     */
    @GetMapping("/list")
    public R<PageResult<ProductEntity>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer status) {
        PageParam pageParam = new PageParam(page, limit);
        PageResult<ProductEntity> result = productService.getProductList(pageParam, productName, status);
        return R.ok("查询成功", result);
    }

    /**
     * 获取秒杀商品列表
     */
    @GetMapping("/seckill/list")
    public R<List<ProductEntity>> getSeckillProducts() {
        List<ProductEntity> products = productService.getSeckillProducts();
        return R.ok("查询成功", products);
    }

    /**
     * 初始化商品库存到Redis（管理员操作）
     */
    @PostMapping("/init/stock")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:init')")
    public R<String> initProductStock() {
        productService.initProductStockToRedis();
        return R.ok("初始化商品库存成功");
    }
}