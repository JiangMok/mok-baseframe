package com.mok.baseframe.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * 订单优惠券使用情况实体类
 *
 * @author: mok
 * @date: 2026/2/4
 */
public class OrderCouponEntity {
    private String id;
    private String orderId;
    private String orderNo;
    private String userCouponId;
    private String couponId;
    private String couponName;
    private Integer couponType;
    private BigDecimal discountAmount;
    private Date createTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderCouponEntity that = (OrderCouponEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(orderId, that.orderId) && Objects.equals(orderNo, that.orderNo) && Objects.equals(userCouponId, that.userCouponId) && Objects.equals(couponId, that.couponId) && Objects.equals(couponName, that.couponName) && Objects.equals(couponType, that.couponType) && Objects.equals(discountAmount, that.discountAmount) && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderId, orderNo, userCouponId, couponId, couponName, couponType, discountAmount, createTime);
    }

    @Override
    public String toString() {
        return "OrderCouponEntity{" +
                "id='" + id + '\'' +
                ", orderId='" + orderId + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", userCouponId='" + userCouponId + '\'' +
                ", couponId='" + couponId + '\'' +
                ", couponName='" + couponName + '\'' +
                ", couponType=" + couponType +
                ", discountAmount=" + discountAmount +
                ", createTime=" + createTime +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getUserCouponId() {
        return userCouponId;
    }

    public void setUserCouponId(String userCouponId) {
        this.userCouponId = userCouponId;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public Integer getCouponType() {
        return couponType;
    }

    public void setCouponType(Integer couponType) {
        this.couponType = couponType;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
