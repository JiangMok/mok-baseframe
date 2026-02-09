package com.mok.baseframe.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * @description:优惠券实体类
 * @author: JN
 * @date: 2026/2/4
 */
public class CouponEntity {
    private String id;
    private String couponName;
    private Integer couponType;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private BigDecimal discountRate;
    private Integer totalQuantity;
    private Integer remainingQuantity;
    private Integer perLimit;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Integer version;
    private Date createTime;
    private Date updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount) {
        this.thresholdAmount = thresholdAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public Integer getPerLimit() {
        return perLimit;
    }

    public void setPerLimit(Integer perLimit) {
        this.perLimit = perLimit;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    @Override
    public String toString() {
        return "CouponEntity{" +
                "id='" + id + '\'' +
                ", couponName='" + couponName + '\'' +
                ", couponType=" + couponType +
                ", thresholdAmount=" + thresholdAmount +
                ", discountAmount=" + discountAmount +
                ", discountRate=" + discountRate +
                ", totalQuantity=" + totalQuantity +
                ", remainingQuantity=" + remainingQuantity +
                ", perLimit=" + perLimit +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status=" + status +
                ", version=" + version +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CouponEntity that = (CouponEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(couponName, that.couponName) && Objects.equals(couponType, that.couponType) && Objects.equals(thresholdAmount, that.thresholdAmount) && Objects.equals(discountAmount, that.discountAmount) && Objects.equals(discountRate, that.discountRate) && Objects.equals(totalQuantity, that.totalQuantity) && Objects.equals(remainingQuantity, that.remainingQuantity) && Objects.equals(perLimit, that.perLimit) && Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime) && Objects.equals(status, that.status) && Objects.equals(version, that.version) && Objects.equals(createTime, that.createTime) && Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, couponName, couponType, thresholdAmount, discountAmount, discountRate, totalQuantity, remainingQuantity, perLimit, startTime, endTime, status, version, createTime, updateTime);
    }
}
