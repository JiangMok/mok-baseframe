package com.mok.baseframe.order.controller;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.entity.OrderInfoEntity;
import com.mok.baseframe.order.service.OrderService;
import com.mok.baseframe.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order")
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
    @PostMapping("/create")
    public R<String> createOrder(@RequestParam String productId,
                                 @RequestParam Integer quantity,
                                 @RequestParam(required = false) String couponIds,
                                 @RequestParam(required = false) String remark) {
        String userId = securityUtils.getCurrentUserId();
        List<String> couponIdList = parseCouponIds(couponIds);

        String orderNo = orderService.createOrder(userId, productId, quantity, couponIdList, remark);
        return R.ok("下单成功", orderNo);
    }

    /**
     * 确认订单（下单但未支付）
     */
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
    @PostMapping("/pay")
    public R<String> payOrder(@RequestParam String orderNo,
                              @RequestParam(defaultValue = "1") Integer payType) {
        boolean success = orderService.payOrder(orderNo, payType);
        if (success) {
            return R.ok("支付成功");
        } else {
            return R.error("支付失败");
        }
    }

    /**
     * 取消订单
     */
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
    @GetMapping("/detail/{orderNo}")
    public R<OrderInfoEntity> getOrderDetail(@PathVariable String orderNo) {
        OrderInfoEntity order = orderService.getOrderByNo(orderNo);
        return R.ok("查询成功", order);
    }

    /**
     * 分页查询订单列表
     */
    @GetMapping("/list")
    public R<PageResult<OrderInfoEntity>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Integer orderStatus,
            @RequestParam(required = false) Integer payStatus) {
        String userId = securityUtils.getCurrentUserId();
        PageParam pageParam = new PageParam(page, limit);
        PageResult<OrderInfoEntity> result = orderService.getOrderList(
                pageParam, userId, orderNo, productName, orderStatus, payStatus);
        return R.ok("查询成功", result);
    }

    /**
     * 计算订单金额
     */
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