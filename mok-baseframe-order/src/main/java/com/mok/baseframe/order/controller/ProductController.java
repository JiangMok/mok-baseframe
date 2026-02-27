package com.mok.baseframe.order.controller;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.entity.ProductEntity;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.order.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@Tag(name = "商品管理", description = "商品相关接口")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 添加商品
     */
    @Operation(summary = "添加商品")
    @OperationLog(title = "添加商品", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:add')")
    public R<String> addProduct(@RequestBody ProductEntity product) {
        productService.addProduct(product);
        return R.ok("添加商品成功");
    }

    /**
     * 更新商品
     */
    @Operation(summary = "更新商品")
    @OperationLog(title = "更新商品", businessType = BusinessType.INSERT)
    @PostMapping("/update")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:edit')")
    public R<String> updateProduct(@RequestBody ProductEntity product) {
        productService.updateProduct(product);
        return R.ok("更新商品成功");
    }

    /**
     * 设置秒杀信息
     */
    @Operation(summary = "设置秒杀信息(更新)")
    @OperationLog(title = "设置秒杀信息", businessType = BusinessType.UPDATE)
    @PostMapping("/setSeckill")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:seckill')")
    public R<String> setSeckill(@RequestBody ProductEntity product) {
        productService.setSeckill(product);
        return R.ok("秒杀信息设置成功");
    }

    /**
     * 清除秒杀信息
     */
    @Operation(summary = "清除秒杀信息")
    @OperationLog(title = "清除秒杀信息", businessType = BusinessType.UPDATE)
    @PostMapping("/clearSeckill/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:seckill')")
    public R<String> clearSeckill(@PathVariable("id") String id) {
        productService.clearSeckill(id);
        return R.ok("秒杀信息清除成功");
    }

    /**
     * 删除商品
     */
    @Operation(summary = "删除商品")
    @OperationLog(title = "删除商品", businessType = BusinessType.DELETE)
    @PostMapping("/delete/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:delete')")
    public R<String> deleteProduct(@PathVariable("id") String id) {
        productService.deleteProduct(id);
        return R.ok("删除商品成功");
    }

    /**
     * 查询商品详情
     */
    @Operation(summary = "常看商品详情")
    @OperationLog(title = "查看商品想详情", businessType = BusinessType.QUERY)
    @PreAuthorize("@permissionChecker.hasPermission('order:product:query')")
    @GetMapping("/detail/{id}")
    public R<ProductEntity> getProductDetail(@PathVariable("id") String id) {
        ProductEntity product = productService.getProductById(id);
        return R.ok("查询成功", product);
    }

    /**
     * 分页查询商品列表
     */
    @Operation(summary = "分页查询商品列表")
    @OperationLog(title = "分页查询商品列表", businessType = BusinessType.QUERY)
    @PreAuthorize("@permissionChecker.hasPermission('order:product:query')")
    @PostMapping("/list")
    public R<PageResult<ProductEntity>> getProductList(@RequestBody @Valid PageParam param) {
        PageResult<ProductEntity> result = productService.getProductList(param);
        return R.ok("查询成功", result);
    }

    /**
     * 获取秒杀商品列表
     */
    @Operation(summary = "获取秒杀商品列表")
    @OperationLog(title = "获取秒杀商品列表", businessType = BusinessType.QUERY)
    @PreAuthorize("@permissionChecker.hasPermission('order:product:query')")
    @GetMapping("/seckill/list")
    public R<List<ProductEntity>> getSeckillProducts() {
        List<ProductEntity> products = productService.getSeckillProducts();
        return R.ok("查询成功", products);
    }

    /**
     * 初始化商品库存到Redis（管理员操作）
     */
    @Operation(summary = "初始化商品库存到redis")
    @OperationLog(title = "初始化商品库存到redis", businessType = BusinessType.QUERY)
    @PostMapping("/init/stock")
    @PreAuthorize("@permissionChecker.hasPermission('order:product:init')")
    public R<String> initProductStock() {
        productService.initProductStockToRedis();
        return R.ok("初始化商品库存成功");
    }
}