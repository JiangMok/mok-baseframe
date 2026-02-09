package com.mok.baseframe.order.controller;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.entity.CouponEntity;
import com.mok.baseframe.entity.UserCouponEntity;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.order.service.CouponService;
import com.mok.baseframe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    private final CouponService couponService;
    private final SecurityUtils securityUtils;

    public CouponController(CouponService couponService,
                            SecurityUtils securityUtils) {
        this.couponService = couponService;
        this.securityUtils = securityUtils;
    }

    /**
     * 添加优惠券
     */
    @Operation(summary = "添加优惠券")
    @OperationLog(title = "添加优惠券", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @PreAuthorize("@permissionChecker.hasPermission('order:coupon:add')")
    public R<String> addCoupon(@RequestBody CouponEntity coupon) {
        couponService.addCoupon(coupon);
        return R.ok("添加优惠券成功");
    }

    /**
     * 更新优惠券
     */
    @Operation(summary = "更新优惠券")
    @OperationLog(title = "更新优惠券", businessType = BusinessType.UPDATE)
    @PostMapping("/update")
    @PreAuthorize("@permissionChecker.hasPermission('order:coupon:update')")
    public R<String> updateCoupon(@RequestBody CouponEntity coupon) {
        couponService.updateCoupon(coupon);
        return R.ok("更新优惠券成功");
    }

    /**
     * 删除优惠券
     */
    @Operation(summary = "删除优惠券")
    @OperationLog(title = "删除优惠券", businessType = BusinessType.INSERT)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('order:coupon:delete')")
    public R<String> deleteCoupon(@PathVariable String id) {
        couponService.deleteCoupon(id);
        return R.ok("删除优惠券成功");
    }

    /**
     * 查询优惠券详情
     */
    @Operation(summary = "查询优惠券详情")
    @OperationLog(title = "查询优惠券详情", businessType = BusinessType.QUERY)
    @GetMapping("/detail/{id}")
    public R<CouponEntity> getCouponDetail(@PathVariable String id) {
        CouponEntity coupon = couponService.getCouponById(id);
        return R.ok("查询成功", coupon);
    }

    /**
     * 分页查询优惠券列表
     */
    @Operation(summary = "分页查询优惠券")
    @OperationLog(title = "分页查询优惠券列表", businessType = BusinessType.QUERY)
    @GetMapping("/list")
    public R<PageResult<CouponEntity>> getCouponList(@RequestBody @Valid PageParam pageParam) {
        PageResult<CouponEntity> result =
                couponService.getCouponList(pageParam);
        return R.ok("查询成功", result);
    }

    /**
     * 抢优惠券
     */
    @Operation(summary = "抢优惠券")
    @OperationLog(title = "抢优惠券", businessType = BusinessType.INSERT)
    @PostMapping("/grab/{couponId}")
    public R<String> grabCoupon(@PathVariable String couponId) {
        String userId = securityUtils.getCurrentUserId();
        boolean success = couponService.grabCoupon(userId, couponId);
        if (success) {
            return R.ok("抢券成功");
        } else {
            return R.error("抢券失败");
        }
    }

    /**
     * 查询用户优惠券
     */
    @GetMapping("/user/list")
    public R<List<UserCouponEntity>> getUserCoupons(
            @RequestParam(required = false) Integer status) {
        String userId = securityUtils.getCurrentUserId();
        List<UserCouponEntity> coupons = couponService.getUserCoupons(userId, status);
        return R.ok("查询成功", coupons);
    }

    /**
     * 获取可用优惠券列表
     */
    @GetMapping("/available/list")
    public R<List<CouponEntity>> getAvailableCoupons() {
        List<CouponEntity> coupons = couponService.getAvailableCoupons();
        return R.ok("查询成功", coupons);
    }

    /**
     * 初始化优惠券库存到Redis（管理员操作）
     */
    @Operation(summary = "初始化优惠券库存到redis")
    @OperationLog(title = "初始化优惠券库存到redis", businessType = BusinessType.INSERT)
    @PostMapping("/init/stock")
    @PreAuthorize("@permissionChecker.hasPermission('order:coupon:init')")
    public R<String> initCouponStock() {
        couponService.initCouponStockToRedis();
        return R.ok("初始化优惠券库存成功");
    }

    /**
     * 清理过期优惠券（管理员操作）
     */
    @Operation(summary = "清理过期优惠券")
    @OperationLog(title = "清理过期优惠券", businessType = BusinessType.UPDATE)
    @PostMapping("/clean/expired")
    @PreAuthorize("@permissionChecker.hasPermission('order:coupon:clean')")
    public R<String> cleanExpiredCoupons() {
        couponService.cleanExpiredCoupons();
        return R.ok("清理过期优惠券成功");
    }
}