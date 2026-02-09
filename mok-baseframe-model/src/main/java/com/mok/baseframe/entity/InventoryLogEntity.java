package com.mok.baseframe.entity;

import java.util.Date;
import java.util.Objects;

/**
 * 库存流水实体类
 *
 * @author: mok
 * @date: 2026/2/4
 */
public class InventoryLogEntity {
    private String id;
    private String productId;
    private String orderId;
    private String orderNo;
    private Integer changeType;
    private Integer changeQuantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
    private String remark;
    private Date createTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InventoryLogEntity that = (InventoryLogEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(productId, that.productId) && Objects.equals(orderId, that.orderId) && Objects.equals(orderNo, that.orderNo) && Objects.equals(changeType, that.changeType) && Objects.equals(changeQuantity, that.changeQuantity) && Objects.equals(beforeQuantity, that.beforeQuantity) && Objects.equals(afterQuantity, that.afterQuantity) && Objects.equals(remark, that.remark) && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productId, orderId, orderNo, changeType, changeQuantity, beforeQuantity, afterQuantity, remark, createTime);
    }

    @Override
    public String toString() {
        return "InventoryLogEntity{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", changeType=" + changeType +
                ", changeQuantity=" + changeQuantity +
                ", beforeQuantity=" + beforeQuantity +
                ", afterQuantity=" + afterQuantity +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
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

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public Integer getChangeQuantity() {
        return changeQuantity;
    }

    public void setChangeQuantity(Integer changeQuantity) {
        this.changeQuantity = changeQuantity;
    }

    public Integer getBeforeQuantity() {
        return beforeQuantity;
    }

    public void setBeforeQuantity(Integer beforeQuantity) {
        this.beforeQuantity = beforeQuantity;
    }

    public Integer getAfterQuantity() {
        return afterQuantity;
    }

    public void setAfterQuantity(Integer afterQuantity) {
        this.afterQuantity = afterQuantity;
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
}
