package com.mok.baseframe.dto;

/**
 *
 * @author: mok
 * @date: 2026/2/27
 */
public class OrderPayDTO {
    String orderNo;
    Integer payType;

    @Override
    public String toString() {
        return "OrderPayDTO{" +
                "orderNo='" + orderNo + '\'' +
                ", payType=" + payType +
                '}';
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }
}
