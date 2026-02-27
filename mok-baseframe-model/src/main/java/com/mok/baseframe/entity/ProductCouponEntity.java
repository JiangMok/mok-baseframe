package com.mok.baseframe.entity;

import java.util.Date;
import java.util.Objects;

/**
 * 商品-优惠券 关联实体类
 *
 * @author: mok
 * @date: 2026/2/4
 */
public class ProductCouponEntity {
    private String id;
    private String productId;
    private String couponId;
    private Date createTime;
    private String[] couponIds;

    public String[] getCouponIds() {
        return couponIds;
    }

    public void setCouponIds(String[] couponIds) {
        this.couponIds = couponIds;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductCouponEntity that = (ProductCouponEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(productId, that.productId) && Objects.equals(couponId, that.couponId) && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productId, couponId, createTime);
    }

    @Override
    public String toString() {
        return "ProductCouponEntity{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", couponId='" + couponId + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
