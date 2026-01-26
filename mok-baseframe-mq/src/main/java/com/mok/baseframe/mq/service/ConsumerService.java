package com.mok.baseframe.mq.service;

/**
 * @description:消费者检查接口
 * @author: JN
 * @date: 2026/1/22 19:51
 * @param:
 * @return:
 **/
public interface ConsumerService {
    /**
     * @description: 根据 操作日志id 查询操作日志是否存在
     * @author: JN
     * @date: 2026/1/22 19:52
     * @param: [id]
     * @return: boolean
     **/
    boolean checkOperationLogExistsById(String id);
}
