package com.mok.baseframe.es.repository;

import com.mok.baseframe.es.entity.OperationLogEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @description: ElasticSearch版本操作日志 数据库访问层
 * @author: mok
 * @date: 2026/3/27 15:49
 **/
@Repository
public interface OperationLogRepository extends ElasticsearchRepository<OperationLogEntity, String> {
}
