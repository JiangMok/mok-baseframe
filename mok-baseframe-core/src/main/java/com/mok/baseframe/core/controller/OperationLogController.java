package com.mok.baseframe.core.controller;


import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.entity.OperationLogEntity;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.core.service.OperationLogService;
import com.mok.baseframe.utils.LogUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequestMapping("/operation-log")

@Tag(name = "操作日志", description = "操作日志相关接口")
public class OperationLogController {
    private static final Logger log = LogUtils.getLogger(OperationLogController.class);

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * @description: 分页查询操作日志
     * @author: JN
     * @date: 2026/1/6 14:20
     * @param: [param]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.common.PageResult < com.mok.securityframework.entity.OperationLog>>
     **/
    @Operation(summary = "分页查询操作日志")
    @OperationLog(title = "分页查询操作日志", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @PreAuthorize("@permissionChecker.hasPermission('system:log:query')")
    public R<PageResult<OperationLogEntity>> page(@RequestBody PageParam param) {
        return R.ok(operationLogService.getPageList(param));
    }

    /**
     * @description: 获取操作日志详情
     * @author: JN
     * @date: 2026/1/6 14:20
     * @param: [id]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.entity.OperationLog>
     **/
    @Operation(summary = "获取操作日志详情")
    @OperationLog(title = "获取操作日志详情", businessType = BusinessType.QUERY)
    @GetMapping("/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('system:log:query')")
    public R<OperationLogEntity> detail(
            @Parameter(description = "日志ID") @PathVariable("id") String id) {

        OperationLogEntity logRecord = operationLogService.getById(id);
        if (logRecord == null) {
            return R.error(404, "操作日志不存在");
        }

        return R.ok(logRecord);
    }

    /**
     * @description: 删除操作日志
     * @author: JN
     * @date: 2026/1/6 14:21
     * @param: [id]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "删除操作日志")
    @OperationLog(title = "删除操作日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@permissionChecker.hasPermission('system:log:delete')")
    public R<String> delete(
            @Parameter(description = "日志ID") @PathVariable("id") String id) {
        operationLogService.removeById(id);
        return R.ok("删除成功");
    }

    /**
     * @description: 清理某一日期之前的日志
     * @author: JN
     * @date: 2026/1/6 14:21
     * @param: [beforeDate]
     * @return: com.mok.securityframework.common.R<java.lang.String>
     **/
    @Operation(summary = "清理历史日志")
    @OperationLog(title = "清除历史日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/clean")
    @PreAuthorize("@permissionChecker.hasPermission('system:log:delete')")
    public R<String> cleanLogs(
            @Parameter(description = "清理指定日期之前的日志，格式：yyyy/MM/dd HH:mm:ss")
            @RequestParam("beforeDate") @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss") LocalDateTime beforeDate) {
        int deletedCount = operationLogService.cleanLogsBefore(beforeDate);
        return R.ok(String.format("已清理%d条日志", deletedCount));
    }

    /**
     * @description: 获取操作统计
     * @author: JN
     * @date: 2026/1/6 14:21
     * @param: []
     * @return: com.mok.securityframework.common.R<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @Operation(summary = "获取操作统计")
    @OperationLog(title = "获取操作统计", businessType = BusinessType.QUERY)
    @GetMapping("/stats")
    @PreAuthorize("@permissionChecker.hasPermission('system:log:query')")
    public R<Map<String, Object>> getOperationStats() {
        Map<String, Object> stats = operationLogService.getOperationStats();
        return R.ok(stats);
    }
}