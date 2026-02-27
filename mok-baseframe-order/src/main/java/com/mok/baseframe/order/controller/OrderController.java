package com.mok.baseframe.order.controller;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.dto.OrderPayDTO;
import com.mok.baseframe.entity.OrderInfoEntity;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.order.service.OrderService;
import com.mok.baseframe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Tag(name = "订单管理", description = "订单相关接口")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    public OrderController(OrderService orderService,
                           SecurityUtils securityUtils) {
        this.orderService = orderService;
        this.securityUtils = securityUtils;
    }

    /**
     * 创建订单（直接支付）
     */
    @Operation(summary = "创建订单-直接支付")
    @OperationLog(title = "创建订单-直接支付", businessType = BusinessType.INSERT)
    @PreAuthorize("@permissionChecker.hasPermission('order:order:create')")
    @PostMapping("/create")
    public R<String> createOrder(@RequestParam String productId,
                                 @RequestParam Integer quantity,
                                 @RequestParam(required = false) String couponIds,
                                 @RequestParam(required = false) String remark) {
        String userId = securityUtils.getCurrentUserId();
        List<String> couponIdList = parseCouponIds(couponIds);

        String orderNo = orderService.createOrder(userId, productId, quantity, couponIdList, remark, 0);
        return R.ok("下单成功", orderNo);
    }

    /**
     * 确认订单（下单但未支付）
     */
    @Operation(summary = "创建订单-未支付")
    @OperationLog(title = "创建订单-未支付", businessType = BusinessType.INSERT)
    @PreAuthorize("@permissionChecker.hasPermission('order:order:confirm')")
    @PostMapping("/confirm")
    public R<String> confirmOrder(@RequestParam String productId,
                                  @RequestParam Integer quantity,
                                  @RequestParam(required = false) String couponIds,
                                  @RequestParam(required = false) String remark) {
        String userId = securityUtils.getCurrentUserId();
        List<String> couponIdList = parseCouponIds(couponIds);

        String orderNo = orderService.confirmOrder(userId, productId, quantity, couponIdList, remark);
        return R.ok("确认订单成功", orderNo);
    }

    /**
     * 支付订单
     */
    @Operation(summary = "订单支付")
    @OperationLog(title = "订单支付", businessType = BusinessType.INSERT)
    @PreAuthorize("@permissionChecker.hasPermission('order:order:pay')")
    @PostMapping("/pay")
    public R<String> payOrder(@RequestBody OrderPayDTO orderPayDTO) {
        boolean success = orderService.payOrder(orderPayDTO.getOrderNo(), orderPayDTO.getPayType());
        if (success) {
            return R.ok("支付成功");
        } else {
            return R.error("支付失败");
        }
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单")
    @OperationLog(title = "取消订单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@permissionChecker.hasPermission('order:order:cancel')")
    @PostMapping("/cancel")
    public R<String> cancelOrder(@RequestParam String orderNo,
                                 @RequestParam String cancelReason) {
        boolean success = orderService.cancelOrder(orderNo, cancelReason);
        if (success) {
            return R.ok("取消订单成功");
        } else {
            return R.error("取消订单失败");
        }
    }

    /**
     * 查询订单详情
     */
    @Operation(summary = "订单详情")
    @GetMapping("/detail/{orderNo}")
    public R<OrderInfoEntity> getOrderDetail(@PathVariable String orderNo) {
        OrderInfoEntity order = orderService.getOrderByNo(orderNo);
        return R.ok("查询成功", order);
    }

    /**
     * 分页查询订单列表
     */
    @Operation(summary = "分页查询订单")
    @PostMapping("/list")
    public R<PageResult<OrderInfoEntity>> getOrderList(@RequestBody @Valid PageParam param) {
        String userId = securityUtils.getCurrentUserId();
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        param.setParams(params);
        PageResult<OrderInfoEntity> result = orderService.getOrderList(param);
        return R.ok("查询成功", result);
    }

    /**
     * 计算订单金额
     */
    @Operation(summary = "计算订单金额-单个商品-多个优惠券")
    @OperationLog(title = "计算订单金额-单个商品-多个优惠券", businessType = BusinessType.INSERT)
    @PreAuthorize("@permissionChecker.hasPermission('order:order:calculate')")
    @GetMapping("/calculate")
    public R<BigDecimal> calculateOrderAmount(@RequestParam String productId,
                                              @RequestParam Integer quantity,
                                              @RequestParam(required = false) String couponIds) {
        List<String> couponIdList = parseCouponIds(couponIds);
        BigDecimal amount = orderService.calculateOrderAmount(productId, quantity, couponIdList);
        return R.ok("计算成功", amount);
    }

    /**
     * 解析优惠券ID字符串
     */
    private List<String> parseCouponIds(String couponIds) {
        if (couponIds == null || couponIds.trim().isEmpty()) {
            return null;
        }

        return Arrays.stream(couponIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
    }
}