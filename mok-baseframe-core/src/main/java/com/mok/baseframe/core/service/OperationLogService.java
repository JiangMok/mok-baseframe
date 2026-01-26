package com.mok.baseframe.core.service;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.entity.OperationLogEntity;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @description: 操作日志接口
 * @author: JN
 * @date: 2026/1/5 16:08
 * @param:
 * @return:
 **/
public interface OperationLogService {

    /**
     * @description: 分页查询操作日志
     * @author: JN
     * @date: 2026/1/5 16:08
     * @param: [param]
     * @return: com.mok.securityframework.common.PageResult<com.mok.securityframework.entity.OperationLog>
     **/
    PageResult<OperationLogEntity> getPageList(PageParam param);

    /**
     * @description: 根据ID获取
     * @author: JN
     * @date: 2026/1/5 17:20
     * @param: [id]
     * @return: com.mok.securityframework.entity.OperationLog
     **/
    OperationLogEntity getById(String id);

    /**
     * @description: 记录(添加)操作日志
     * @author: JN
     * @date: 2026/1/5 16:08
     * @param: [logRecord]
     * @return: void
     **/
    void recordLog(OperationLogEntity logRecord);

    /**
     * @description: 通过IU删除
     * @author: JN
     * @date: 2026/1/5 17:25
     * @param: [id]
     * @return: int
     **/
    int removeById(String id);

    /**
     * @description: 清理指定日期之前的操作日志
     * @author: JN
     * @date: 2026/1/5 16:09
     * @param: [dateTime]
     * @return: int
     **/
    int cleanLogsBefore(LocalDateTime dateTime);

    /**
     * @description: 获取操作统计
     * @author: JN
     * @date: 2026/1/5 16:10
     * @param: []
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     **/
    Map<String, Object> getOperationStats();
}
