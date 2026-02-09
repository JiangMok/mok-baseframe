package com.mok.baseframe.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * @description: 订单信息实体类
 * @author: mok
 * @date: 2026/2/4 23:02
 **/
public class OrderInfoEntity {
    private String id;
    private String orderNo;
    private String userId;
    private String productId;
    private String productName;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private Integer payStatus;
    private Date payTime;
    private Integer payType;
    private String transactionId;
    private Integer orderType;
    private String cancelReason;
    private Date cancelTime;
    private Date deliveryTime;
    private Date receiveTime;
    private Date closeTime;
    private String remark;
    private Date createTime;
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderInfoEntity that = (OrderInfoEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(orderNo, that.orderNo) && Objects.equals(userId, that.userId) && Objects.equals(productId, that.productId) && Objects.equals(productName, that.productName) && Objects.equals(productPrice, that.productPrice) && Objects.equals(quantity, that.quantity) && Objects.equals(originalAmount, that.originalAmount) && Objects.equals(discountAmount, that.discountAmount) && Objects.equals(payAmount, that.payAmount) && Objects.equals(orderStatus, that.orderStatus) && Objects.equals(payStatus, that.payStatus) && Objects.equals(payTime, that.payTime) && Objects.equals(payType, that.payType) && Objects.equals(transactionId, that.transactionId) && Objects.equals(orderType, that.orderType) && Objects.equals(cancelReason, that.cancelReason) && Objects.equals(cancelTime, that.cancelTime) && Objects.equals(deliveryTime, that.deliveryTime) && Objects.equals(receiveTime, that.receiveTime) && Objects.equals(closeTime, that.closeTime) && Objects.equals(remark, that.remark) && Objects.equals(createTime, that.createTime) && Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, orderNo, userId, productId, productName, productPrice, quantity, originalAmount, discountAmount, payAmount, orderStatus, payStatus, payTime, payType, transactionId, orderType, cancelReason, cancelTime, deliveryTime, receiveTime, closeTime, remark, createTime, updateTime);
    }

    @Override
    public String toString() {
        return "OrderInfoEntity{" +
                "id='" + id + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", productPrice=" + productPrice +
                ", quantity=" + quantity +
                ", originalAmount=" + originalAmount +
                ", discountAmount=" + discountAmount +
                ", payAmount=" + payAmount +
                ", orderStatus=" + orderStatus +
                ", payStatus=" + payStatus +
                ", payTime=" + payTime +
                ", payType=" + payType +
                ", transactionId='" + transactionId + '\'' +
                ", orderType=" + orderType +
                ", cancelReason='" + cancelReason + '\'' +
                ", cancelTime=" + cancelTime +
                ", deliveryTime=" + deliveryTime +
                ", receiveTime=" + receiveTime +
                ", closeTime=" + closeTime +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getPayAmount() {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount) {
        this.payAmount = payAmount;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Integer getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(Integer payStatus) {
        this.payStatus = payStatus;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Integer getOrderType() {
        return orderType;
    }

    public void setOrderType(Integer orderType) {
        this.orderType = orderType;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Date getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime) {
        this.cancelTime = cancelTime;
    }

    public Date getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Date deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public Date getReceiveTime() {
        return receiveTime;
    }

    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }

    public Date getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(Date closeTime) {
        this.closeTime = closeTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
