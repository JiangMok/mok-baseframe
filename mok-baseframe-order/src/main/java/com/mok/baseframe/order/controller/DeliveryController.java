package com.mok.baseframe.order.controller;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.entity.DeliveryOrderEntity;
import com.mok.baseframe.order.service.DeliveryService;
import com.mok.baseframe.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delivery")
@Tag(name = "发货管理", description = "发货管理相关接口")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final SecurityUtils securityUtils;

    public DeliveryController(DeliveryService deliveryService,
                              SecurityUtils securityUtils) {
        this.deliveryService = deliveryService;
        this.securityUtils = securityUtils;
    }

    /**
     * 发货
     */
    @PostMapping("/ship")
    @PreAuthorize("@permissionChecker.hasPermission('order:delivery:ship')")
    public R<String> shipDelivery(@RequestParam("deliveryId") String deliveryId,
                                  @RequestParam("deliveryCompany") String deliveryCompany,
                                  @RequestParam("deliveryNumber") String deliveryNumber) {
        boolean success = deliveryService.shipDelivery(deliveryId, deliveryCompany, deliveryNumber);
        if (success) {
            return R.ok("发货成功");
        } else {
            return R.error("发货失败");
        }
    }

    /**
     * 确认收货
     */
    @PostMapping("/receive/{deliveryId}")
    public R<String> receiveDelivery(@PathVariable("deliveryId") String deliveryId) {
        String userId = securityUtils.getCurrentUserId();
        boolean success = deliveryService.receiveDelivery(deliveryId, userId);
        if (success) {
            return R.ok("确认收货成功");
        } else {
            return R.error("确认收货失败");
        }
    }

    /**
     * 查询发货单详情
     */
    @GetMapping("/detail/{id}")
    public R<DeliveryOrderEntity> getDeliveryDetail(@PathVariable String id) {
        DeliveryOrderEntity delivery = deliveryService.getDeliveryById(id);
        return R.ok("查询成功", delivery);
    }

    /**
     * 根据订单ID查询发货单
     */
    @GetMapping("/order/{orderId}")
    public R<DeliveryOrderEntity> getDeliveryByOrderId(@PathVariable String orderId) {
        DeliveryOrderEntity delivery = deliveryService.getDeliveryByOrderId(orderId);
        return R.ok("查询成功", delivery);
    }

    /**
     * 分页查询发货单列表
     */
    @PostMapping("/list")
    public R<PageResult<DeliveryOrderEntity>> getDeliveryList(@RequestBody @Valid PageParam pageParam) {
        String userId = securityUtils.getCurrentUserId();
        PageResult<DeliveryOrderEntity> result = deliveryService.getDeliveryList(
                pageParam, userId);

        return R.ok("查询成功", result);
    }

    /**
     * 管理员分页查询发货单列表
     */
    @PostMapping("/admin/list")
    @PreAuthorize("@permissionChecker.hasPermission('order:delivery:query')")
    public R<PageResult<DeliveryOrderEntity>> getAdminDeliveryList(@RequestBody @Valid PageParam pageParam) {
        PageResult<DeliveryOrderEntity> result = deliveryService.getAdminDeliveryList(pageParam);
        return R.ok("查询成功", result);
    }
}