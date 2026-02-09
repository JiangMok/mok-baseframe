package com.mok.baseframe.order.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @description: * 订单号生成器
 * * 格式：年月日时分秒 + 6位自增序列 + 2位随机数
 * @author: mok
 * @date: 2026/2/4 23:41
 **/
public class OrderNoGenerator {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final int MAX_SEQUENCE = 999999;

    /**
     * 生成订单号
     */
    public static String generateOrderNo() {
        String dateStr = DATE_FORMAT.format(new Date());
        long seq = SEQUENCE.incrementAndGet();
        if (seq > MAX_SEQUENCE) {
            SEQUENCE.set(0);
            seq = SEQUENCE.incrementAndGet();
        }
        String seqStr = String.format("%06d", seq);
        String randomStr = String.format("%02d", (int) (Math.random() * 100));
        return dateStr + seqStr + randomStr;
    }

    /**
     * 生成优惠券码
     */
    public static String generateCouponCode() {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "CP" + uuid.substring(0, 10);
    }

    /**
     * 生成发货单号
     */
    public static String generateDeliveryNo() {
        String dateStr = DATE_FORMAT.format(new Date());
        long seq = SEQUENCE.incrementAndGet();
        String seqStr = String.format("%06d", seq % 1000000);
        return "DL" + dateStr + seqStr;
    }
}