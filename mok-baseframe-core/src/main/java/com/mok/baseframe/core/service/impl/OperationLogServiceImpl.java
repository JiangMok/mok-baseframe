package com.mok.baseframe.core.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.core.config.OperationLogConfig;
import com.mok.baseframe.dao.OperationLogMapper;
import com.mok.baseframe.entity.OperationLogEntity;
import com.mok.baseframe.core.service.OperationLogService;
import com.mok.baseframe.utils.LogUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 操作日志接口实现类
 * @author: JN
 * @date: 2026/1/5
 */

@Service

public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLogEntity> implements OperationLogService {
    private static final Logger log = LogUtils.getLogger(OperationLogServiceImpl.class);

    private final OperationLogConfig operationLogConfig;

    public OperationLogServiceImpl(OperationLogConfig operationLogConfig) {
        this.operationLogConfig = operationLogConfig;
    }

    @Override
    public PageResult<OperationLogEntity> getPageList(PageParam param) {
        Page<OperationLogEntity> page = new Page<>(param.getPageNum(), param.getPageSize());
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        // 条件查询
        if (StringUtils.hasText(param.getKeyword())) {
            wrapper.like(OperationLogEntity::getTitle, param.getKeyword())
                    //.like 模糊查询
                    .or().like(OperationLogEntity::getOperatorName, param.getKeyword())
                    .or().like(OperationLogEntity::getOperUrl, param.getKeyword());
        }
        if (param.get("operatorType") != null) {
            //.eq 等于
            wrapper.eq(OperationLogEntity::getOperatorType, param.get("operatorType"));
        }
        if (param.get("status")!= null) {
            //.eq 等于
            wrapper.eq(OperationLogEntity::getStatus, param.get("status"));
        }
        if (param.get("businessType") != null) {
            wrapper.eq(OperationLogEntity::getBusinessType, param.get("businessType"));
        }

        // 时间范围查询
        if (param.get("startTime") != null) {
            //.ge 大于等于
            wrapper.ge(OperationLogEntity::getOperTime, param.get("startTime"));
        }
        if (param.get("endTime") != null) {
            //.le 小于等于
            wrapper.le(OperationLogEntity::getOperTime, param.get("endTime"));
        }
        // 排序
        wrapper.orderByDesc(OperationLogEntity::getOperTime);
        IPage<OperationLogEntity> result = baseMapper.selectPage(page, wrapper);
        return PageResult.fromIPage(result);
    }

    @Override
    public OperationLogEntity getById(String id) {
        return baseMapper.selectById(id);
    }

    @Transactional
    @Override
    public void recordLog(OperationLogEntity logRecord) {
        if (!operationLogConfig.getEnabled()) {
            return;
        }
        try {
            // 限制参数长度
            if (logRecord.getOperParam() != null &&
                    logRecord.getOperParam().length() > operationLogConfig.getMaxContentLength()) {
                logRecord.setOperParam(logRecord.getOperParam()
                        //substring(int beginIndex)：返回从指定索引开始到字符串末尾的子字符串。
                        //substring(int beginIndex, int endIndex)：返回从开始索引（包括）到结束索引（不包括）的子字符串。
                        .substring(0, operationLogConfig.getMaxContentLength()) + "...");
            }
            if (logRecord.getJsonResult() != null &&
                    logRecord.getJsonResult().length() > operationLogConfig.getMaxContentLength()) {
                logRecord.setJsonResult(logRecord.getJsonResult()
                        .substring(0, operationLogConfig.getMaxContentLength()) + "...");
            }
            logRecord.setOperTime(LocalDateTime.now());
            logRecord.setId(IdUtil.simpleUUID());
            save(logRecord);
            log.debug("操作日志已记录：{} - {}", logRecord.getTitle(), logRecord.getOperatorName());
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    @Transactional
    @Override
    public int removeById(String id) {
        return baseMapper.deleteById(id);
    }

    @Transactional
    @Override
    public int cleanLogsBefore(LocalDateTime dateTime) {
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        //.lt 小于
        wrapper.lt(OperationLogEntity::getOperTime, dateTime);
        return baseMapper.delete(wrapper);
    }

    /**
     * @description: 获取操作日志的统计信息
     * 包括 : 总操作数、今日操作数、成功率、操作最多的用户
     * @author: JN
     * @date: 2026/1/5 16:21
     * @param: []
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    @Override
    public Map<String, Object> getOperationStats() {
        //创建一个 HashMap ,用于存储统计结果
        //使用 Map<String,Object> 可以灵活存储不同的类型的统计值
        Map<String, Object> stats = new HashMap<>();

        //统计总操作数
        //  使用 mybatis-plus 的 .selectCount 方法,传入 null 表示不添加查询条件
        //  作用 : 获取操作日志表中的总记录数
        Long totalCount = baseMapper.selectCount(null);
        //将统计结果存入 map
        stats.put("totalCount", totalCount);

        //统计今日操作数
        //  LocalDateTime.now().toLocalDate() : 获取今天的日期,.atStartOfDay:获取当天的起始时间
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LambdaQueryWrapper<OperationLogEntity> todayWrapper = new LambdaQueryWrapper<>();
        //.ge 大于等于
        todayWrapper.ge(OperationLogEntity::getOperTime, todayStart);
        Long todayCount = baseMapper.selectCount(todayWrapper);
        stats.put("todayCount", todayCount);

        //统计操作成功率
        LambdaQueryWrapper<OperationLogEntity> successWrapper = new LambdaQueryWrapper<>();
        successWrapper.eq(OperationLogEntity::getStatus, 0);
        Long successCount = baseMapper.selectCount(successWrapper);
        Double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
        stats.put("successRate", String.format("%.2f%%", successRate));

        //统计操作最多的用户
        List<Map<String, Object>> topUsers = baseMapper.selectTopOperators(5);
        // 将结果存入 Map
        stats.put("topUsers", topUsers);

        return stats;
    }
}
