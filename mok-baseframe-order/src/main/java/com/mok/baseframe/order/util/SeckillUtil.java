package com.mok.baseframe.order.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀工具类
 * 包含秒杀相关的通用方法和工具函数
 */
public class SeckillUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SeckillUtil.class);
    
    // 验证码字符集（去除容易混淆的字符）
    private static final String VERIFY_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    
    // 随机数生成器
    private static final Random RANDOM = new Random();
    
    // 秒杀路径盐值（应该从配置文件读取，这里使用默认值）
    private static final String SECKILL_SALT = "mok_seckill_salt_2024";
    
    /**
     * 生成秒杀验证码（6位字母数字混合）
     * 用于防止机器人刷单
     */
    public static String generateVerifyCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(VERIFY_CODE_CHARS.length());
            sb.append(VERIFY_CODE_CHARS.charAt(index));
        }
        return sb.toString();
    }
    
    /**
     * 生成数字验证码（6位）
     */
    public static String generateNumericVerifyCode() {
        return String.format("%06d", RANDOM.nextInt(1000000));
    }
    
    /**
     * 生成秒杀路径（用于隐藏真实秒杀接口）
     * 使用MD5加密：用户ID + 商品ID + 盐值 + 时间戳
     */
    public static String generateSeckillPath(Long userId, Long productId) {
        return generateSeckillPath(userId, productId, System.currentTimeMillis());
    }
    
    /**
     * 生成秒杀路径（带时间戳）
     */
    public static String generateSeckillPath(Long userId, Long productId, long timestamp) {
        if (userId == null || productId == null) {
            throw new IllegalArgumentException("用户ID和商品ID不能为空");
        }
        
        String base = userId + "_" + productId + "_" + SECKILL_SALT + "_" + timestamp;
        return DigestUtils.md5DigestAsHex(base.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 验证秒杀路径
     */
    public static boolean verifySeckillPath(Long userId, Long productId, long timestamp, String path) {
        if (userId == null || productId == null || path == null) {
            return false;
        }
        
        // 检查时间戳是否过期（5分钟内有效）
        long currentTime = System.currentTimeMillis();
        if (Math.abs(currentTime - timestamp) > TimeUnit.MINUTES.toMillis(5)) {
            logger.warn("秒杀路径已过期，用户ID：{}，商品ID：{}，时间戳：{}", userId, productId, timestamp);
            return false;
        }
        
        String expectedPath = generateSeckillPath(userId, productId, timestamp);
        return expectedPath.equals(path);
    }
    
    /**
     * 生成秒杀接口隐藏路径（用于前端获取）
     * 格式：/api/seckill/{path}/do
     */
    public static String generateSeckillUrl(Long userId, Long productId) {
        String path = generateSeckillPath(userId, productId);
        return "/api/seckill/" + path + "/do";
    }
    
    /**
     * 计算秒杀剩余时间（秒）
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 剩余秒数（负数表示已结束，0表示正在进行，正数表示未开始）
     */
    public static long calculateRemainSeconds(long startTime, long endTime) {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime < startTime) {
            // 秒杀未开始
            return (startTime - currentTime) / 1000;
        } else if (currentTime <= endTime) {
            // 秒杀进行中
            return 0;
        } else {
            // 秒杀已结束
            return -1;
        }
    }
    
    /**
     * 格式化剩余时间
     * @param remainSeconds 剩余秒数
     * @return 格式化后的时间字符串
     */
    public static String formatRemainTime(long remainSeconds) {
        if (remainSeconds < 0) {
            return "已结束";
        } else if (remainSeconds == 0) {
            return "进行中";
        } else {
            long days = remainSeconds / (24 * 3600);
            long hours = (remainSeconds % (24 * 3600)) / 3600;
            long minutes = (remainSeconds % 3600) / 60;
            long seconds = remainSeconds % 60;
            
            StringBuilder sb = new StringBuilder();
            if (days > 0) {
                sb.append(days).append("天");
            }
            if (hours > 0) {
                sb.append(hours).append("小时");
            }
            if (minutes > 0) {
                sb.append(minutes).append("分");
            }
            if (seconds > 0) {
                sb.append(seconds).append("秒");
            }
            
            return sb.toString();
        }
    }
    
    /**
     * 生成秒杀限流Key
     * 格式：seckill:rate:limit:{userId}:{productId}
     */
    public static String generateRateLimitKey(Long userId, Long productId) {
        return "seckill:rate:limit:" + userId + ":" + productId;
    }
    
    /**
     * 生成秒杀库存Key
     * 格式：seckill:stock:{productId}
     */
    public static String generateStockKey(Long productId) {
        return "seckill:stock:" + productId;
    }
    
    /**
     * 生成秒杀结果Key
     * 格式：seckill:result:{userId}:{productId}
     */
    public static String generateResultKey(Long userId, Long productId) {
        return "seckill:result:" + userId + ":" + productId;
    }
    
    /**
     * 生成秒杀用户已抢Key（防止重复抢购）
     * 格式：seckill:user:{userId}:{productId}
     */
    public static String generateUserSeckillKey(Long userId, Long productId) {
        return "seckill:user:" + userId + ":" + productId;
    }
    
    /**
     * 生成秒杀排队队列Key
     * 格式：seckill:queue:{productId}
     */
    public static String generateQueueKey(Long productId) {
        return "seckill:queue:" + productId;
    }
    
    /**
     * 生成秒杀订单Key
     * 格式：seckill:order:{productId}:{orderNo}
     */
    public static String generateSeckillOrderKey(Long productId, String orderNo) {
        return "seckill:order:" + productId + ":" + orderNo;
    }
    
    /**
     * 验证商品是否处于秒杀时间段
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return true-在秒杀时间内，false-不在秒杀时间内
     */
    public static boolean isInSeckillTime(java.util.Date startTime, java.util.Date endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long start = startTime.getTime();
        long end = endTime.getTime();
        
        return currentTime >= start && currentTime <= end;
    }
    
    /**
     * 验证商品是否即将开始秒杀（用于预热）
     * @param startTime 开始时间
     * @param preheatSeconds 预热提前秒数
     * @return true-需要预热，false-不需要预热
     */
    public static boolean needPreheat(java.util.Date startTime, int preheatSeconds) {
        if (startTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long start = startTime.getTime();
        long preheatTime = start - TimeUnit.SECONDS.toMillis(preheatSeconds);
        
        return currentTime >= preheatTime && currentTime < start;
    }
    
    /**
     * 计算秒杀进度百分比
     * @param totalStock 总库存
     * @param currentStock 当前库存
     * @return 进度百分比（0-100）
     */
    public static int calculateProgress(int totalStock, int currentStock) {
        if (totalStock <= 0) {
            return 0;
        }
        
        int sold = totalStock - currentStock;
        int progress = (int) ((sold * 100.0) / totalStock);
        
        // 确保在0-100范围内
        return Math.max(0, Math.min(100, progress));
    }
    
    /**
     * 生成秒杀活动状态描述
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 商品状态
     * @return 状态描述
     */
    public static String getSeckillStatusDesc(java.util.Date startTime, java.util.Date endTime, Integer status) {
        if (status != null && status == 0) {
            return "已下架";
        }
        
        if (startTime == null || endTime == null) {
            return "非秒杀商品";
        }
        
        long currentTime = System.currentTimeMillis();
        long start = startTime.getTime();
        long end = endTime.getTime();
        
        if (currentTime < start) {
            return "未开始";
        } else if (currentTime <= end) {
            return "进行中";
        } else {
            return "已结束";
        }
    }
    
    /**
     * 生成秒杀请求ID（用于幂等性控制）
     * 格式：{userId}_{productId}_{timestamp}_{random}
     */
    public static String generateRequestId(Long userId, Long productId) {
        long timestamp = System.currentTimeMillis();
        int random = RANDOM.nextInt(10000);
        return userId + "_" + productId + "_" + timestamp + "_" + random;
    }
    
    /**
     * 解析秒杀请求ID
     * @param requestId 请求ID
     * @return 数组[userId, productId, timestamp, random]
     */
    public static String[] parseRequestId(String requestId) {
        if (requestId == null || !requestId.contains("_")) {
            return new String[0];
        }
        return requestId.split("_");
    }
    
    /**
     * 验证秒杀参数是否合法
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @return true-合法，false-不合法
     */
    public static boolean validateSeckillParams(Long userId, Long productId, Integer quantity) {
        if (userId == null || userId <= 0) {
            logger.warn("秒杀参数验证失败：用户ID无效");
            return false;
        }
        
        if (productId == null || productId <= 0) {
            logger.warn("秒杀参数验证失败：商品ID无效");
            return false;
        }
        
        if (quantity == null || quantity <= 0 || quantity > 10) {
            logger.warn("秒杀参数验证失败：购买数量无效，数量：{}", quantity);
            return false;
        }
        
        return true;
    }
    
    /**
     * 生成秒杀缓存预热Key
     * 格式：seckill:preheat:{productId}
     */
    public static String generatePreheatKey(Long productId) {
        return "seckill:preheat:" + productId;
    }
    
    /**
     * 生成秒杀统计Key（用于统计秒杀数据）
     * 格式：seckill:stat:{productId}:{date}
     */
    public static String generateStatKey(Long productId, String date) {
        return "seckill:stat:" + productId + ":" + date;
    }
    
    /**
     * 获取当前日期字符串（格式：yyyyMMdd）
     */
    public static String getCurrentDateString() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd");
        return sdf.format(new java.util.Date());
    }
    
    /**
     * 生成秒杀失败重试Key
     * 格式：seckill:retry:{userId}:{productId}
     */
    public static String generateRetryKey(Long userId, Long productId) {
        return "seckill:retry:" + userId + ":" + productId;
    }
    
    /**
     * 计算秒杀失败重试延迟时间（毫秒）
     * 使用指数退避算法
     * @param retryCount 重试次数
     * @return 延迟毫秒数
     */
    public static long calculateRetryDelay(int retryCount) {
        // 基础延迟1秒，最大延迟10秒
        long baseDelay = 1000;
        long maxDelay = 10000;
        
        long delay = baseDelay * (1L << retryCount); // 2的retryCount次方
        return Math.min(delay, maxDelay);
    }
}