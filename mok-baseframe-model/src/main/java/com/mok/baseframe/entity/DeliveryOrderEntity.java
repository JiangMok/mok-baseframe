package com.mok.baseframe.entity;

import java.util.Date;
import java.util.Objects;

/**
 * 发货单实体类
 *
 * @author: mok
 * @date: 2026/2/4
 */
public class DeliveryOrderEntity {
    private String id;
    private String deliveryNo;
    private String orderId;
    private String orderNo;
    private String userId;
    private String productId;
    private String productName;
    private Integer quantity;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private Integer deliveryStatus;
    private String deliveryCompany;
    private String deliveryNumber;
    private Date deliveryTime;
    private Date receiveTime;
    private String remark;
    private Date createTime;
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeliveryOrderEntity that = (DeliveryOrderEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(deliveryNo, that.deliveryNo) && Objects.equals(orderId, that.orderId) && Objects.equals(orderNo, that.orderNo) && Objects.equals(userId, that.userId) && Objects.equals(productId, that.productId) && Objects.equals(productName, that.productName) && Objects.equals(quantity, that.quantity) && Objects.equals(receiverName, that.receiverName) && Objects.equals(receiverPhone, that.receiverPhone) && Objects.equals(receiverAddress, that.receiverAddress) && Objects.equals(deliveryStatus, that.deliveryStatus) && Objects.equals(deliveryCompany, that.deliveryCompany) && Objects.equals(deliveryNumber, that.deliveryNumber) && Objects.equals(deliveryTime, that.deliveryTime) && Objects.equals(receiveTime, that.receiveTime) && Objects.equals(remark, that.remark) && Objects.equals(createTime, that.createTime) && Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deliveryNo, orderId, orderNo, userId, productId, productName, quantity, receiverName, receiverPhone, receiverAddress, deliveryStatus, deliveryCompany, deliveryNumber, deliveryTime, receiveTime, remark, createTime, updateTime);
    }

    @Override
    public String toString() {
        return "DeliveryOrderEntity{" +
                "id='" + id + '\'' +
                ", deliveryNo='" + deliveryNo + '\'' +
                ", orderId='" + orderId + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", receiverName='" + receiverName + '\'' +
                ", receiverPhone='" + receiverPhone + '\'' +
                ", receiverAddress='" + receiverAddress + '\'' +
                ", deliveryStatus=" + deliveryStatus +
                ", deliveryCompany='" + deliveryCompany + '\'' +
                ", deliveryNumber='" + deliveryNumber + '\'' +
                ", deliveryTime=" + deliveryTime +
                ", receiveTime=" + receiveTime +
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

    public String getDeliveryNo() {
        return deliveryNo;
    }

    public void setDeliveryNo(String deliveryNo) {
        this.deliveryNo = deliveryNo;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public Integer getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(Integer deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getDeliveryCompany() {
        return deliveryCompany;
    }

    public void setDeliveryCompany(String deliveryCompany) {
        this.deliveryCompany = deliveryCompany;
    }

    public String getDeliveryNumber() {
        return deliveryNumber;
    }

    public void setDeliveryNumber(String deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
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
