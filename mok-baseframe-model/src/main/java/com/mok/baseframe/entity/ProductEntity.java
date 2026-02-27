package com.mok.baseframe.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

/**
 * @description:商品实体类
 * @author: JN
 * @date: 2026/2/4
 */
public class ProductEntity {
    private String id;
    private String productName;
    private String productDesc;
    private BigDecimal price;
    private Integer stock;
    private Integer seckillStock;
    private BigDecimal seckillPrice;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date seckillStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date seckillEndTime;
    private Integer status;
    private Integer version;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSeckillStock() {
        return seckillStock;
    }

    public void setSeckillStock(Integer seckillStock) {
        this.seckillStock = seckillStock;
    }

    public BigDecimal getSeckillPrice() {
        return seckillPrice;
    }

    public void setSeckillPrice(BigDecimal seckillPrice) {
        this.seckillPrice = seckillPrice;
    }

    public Date getSeckillStartTime() {
        return seckillStartTime;
    }

    public void setSeckillStartTime(Date seckillStartTime) {
        this.seckillStartTime = seckillStartTime;
    }

    public Date getSeckillEndTime() {
        return seckillEndTime;
    }

    public void setSeckillEndTime(Date seckillEndTime) {
        this.seckillEndTime = seckillEndTime;
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
        return "ProductEntity{" +
                "id='" + id + '\'' +
                ", productName='" + productName + '\'' +
                ", productDesc='" + productDesc + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", seckillStock=" + seckillStock +
                ", seckillPrice=" + seckillPrice +
                ", seckillStartTime=" + seckillStartTime +
                ", seckillEndTime=" + seckillEndTime +
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
        ProductEntity that = (ProductEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(productName, that.productName) && Objects.equals(productDesc, that.productDesc) && Objects.equals(price, that.price) && Objects.equals(stock, that.stock) && Objects.equals(seckillStock, that.seckillStock) && Objects.equals(seckillPrice, that.seckillPrice) && Objects.equals(seckillStartTime, that.seckillStartTime) && Objects.equals(seckillEndTime, that.seckillEndTime) && Objects.equals(status, that.status) && Objects.equals(version, that.version) && Objects.equals(createTime, that.createTime) && Objects.equals(updateTime, that.updateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, productName, productDesc, price, stock, seckillStock, seckillPrice, seckillStartTime, seckillEndTime, status, version, createTime, updateTime);
    }
}
