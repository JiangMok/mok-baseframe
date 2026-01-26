package com.mok.baseframe.mq.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mok.baseframe.dao.OperationLogMapper;
import com.mok.baseframe.entity.OperationLogEntity;
import com.mok.baseframe.mq.service.ConsumerService;
import org.springframework.stereotype.Service;

/**
 * @description:消费者检查sevice实现类
 * @author: JN
 * @date: 2026/1/22
 */
@Service
public class ConsumerServiceImpl implements ConsumerService {

    private final OperationLogMapper operationLogMapper;

    public ConsumerServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public boolean checkOperationLogExistsById(String id) {
        LambdaQueryWrapper<OperationLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLogEntity::getId, id);
        return operationLogMapper.exists(wrapper);
    }
}
