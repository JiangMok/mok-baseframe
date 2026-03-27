package com.mok.baseframe.es.service;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.es.entity.OperationLogEntity;

/**
 * @description: ElasticSearch 操作日志service
 * @author: mok
 * @date: 2026/3/27 15:54
 **/
public interface ESOperationLogService {

    /**
     * @description: 查询所有数据
     * @author: mok
     * @date: 2026/3/27 15:57
     * @param: []
     * @return: java.lang.Iterable<com.mok.baseframe.es.entity.OperationLogEntity>
     **/
    PageResult<OperationLogEntity> getPageList(PageParam param);

    /**
     * @description: 保存操作日志
     * @author: mok
     * @date: 2026/3/27 15:59
     * @param: []
     * @return: com.mok.baseframe.es.entity.OperationLogEntity
     **/
    OperationLogEntity save(OperationLogEntity operationLogEntity);

    /**
     * @description: 通过ID获取
     * @author: mok
     * @date: 2026/3/27 16:01
     * @param: [id]
     * @return: com.mok.baseframe.es.entity.OperationLogEntity
     **/
    OperationLogEntity findById(String id);
}
