package com.mok.baseframe.dto;

import java.util.Objects;

/**
 * 订单取消-消息 实体类
 *
 * @author: mok
 * @date: 2026/2/5
 */
public class OrderCancelMessage {
    private String orderNo;
    private Long createTime;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderCancelMessage that = (OrderCancelMessage) o;
        return Objects.equals(orderNo, that.orderNo) && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNo, createTime);
    }

    @Override
    public String toString() {
        return "OrderCancelMessage{" +
                "orderNo='" + orderNo + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
