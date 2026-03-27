package com.mok.baseframe.es.controller;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.common.annotation.OperationLog;
import com.mok.baseframe.common.enums.BusinessType;
import com.mok.baseframe.es.entity.OperationLogEntity;
import com.mok.baseframe.es.service.ESOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ElasticSearch 版本操作日志controller
 *
 * @author: mok
 * @date: 2026/3/27
 */
@RestController
@RequestMapping("/es-operation-log")
@Tag(name = "ElasticSearch操作日志", description = "ElasticSearch操作日志相关接口")
public class ESOperationLogController {

    private final ESOperationLogService operationLogService;

    public ESOperationLogController(ESOperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Operation(summary = "分页查询操作日志")
    @OperationLog(title = "分页查询操作日志", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @PreAuthorize("@permissionChecker.hasPermission('system:log:query')")
    public R<PageResult<OperationLogEntity>> page(@RequestBody PageParam param) {
        return R.ok(operationLogService.getPageList(param));
    }
}
