package com.mok.baseframe.dao;

import com.mok.baseframe.entity.InventoryLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description: 库存流水 mapper
 * @author: mok
 * @date: 2026/2/4 23:35
 **/
@Mapper
public interface InventoryLogMapper {
    // 插入库存流水记录
    int insert(InventoryLogEntity inventoryLog);
}
