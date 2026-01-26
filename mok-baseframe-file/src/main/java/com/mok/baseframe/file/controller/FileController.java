package com.mok.baseframe.file.controller;

import com.mok.baseframe.common.PageParam;
import com.mok.baseframe.common.PageResult;
import com.mok.baseframe.common.R;
import com.mok.baseframe.core.annotation.OperationLog;
import com.mok.baseframe.dto.BatchDeleteRequest;
import com.mok.baseframe.dto.FileUploadResponse;
import com.mok.baseframe.entity.FileEntity;
import com.mok.baseframe.enums.BusinessType;
import com.mok.baseframe.file.service.FileService;
import com.mok.baseframe.utils.LogUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @description: 文件上传controller
 **/
@Tag(name = "文件管理", description = "文件上传下载接口")
@RestController
@RequestMapping("/files")
public class FileController {
    private static final Logger log = LogUtils.getLogger(FileController.class);

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    /**
     * @description: 分页查询角色列表
     * @author: JN
     * @date: 2026/1/5 16:50
     * @param: [param]
     * @return: com.mok.securityframework.common.R<com.mok.securityframework.common.PageResult < com.mok.securityframework.entity.Role>>
     **/
    @Operation(summary = "分页查询文件列表")
    @OperationLog(title = "分页查询文件", businessType = BusinessType.QUERY)
    @PostMapping("/page")
    @PreAuthorize("@permissionChecker.hasPermission('system:files:query')")
    public R<PageResult<FileEntity>> page(@RequestBody @Valid PageParam param) {
        return R.ok(fileService.getPageList(param));
    }

    /**
     * 上传文件 - 需要登录
     * 测试时，先登录获取token，然后在Header中添加：Authorization: Bearer {token}
     */
    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    @PreAuthorize("@permissionChecker.hasPermission('system:files:upload')")
    public R<FileUploadResponse> upload(
            @Parameter(description = "文件")
            @RequestParam("file") MultipartFile file) {

        log.info("文件上传: originalName={}, size={}, contentType={}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            FileUploadResponse result = fileService.upload(file);
            return R.ok("文件上传成功", result);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return R.error(e.getMessage());
        }
    }

    @Operation(summary = "获取文件详情")
    @GetMapping("/{fileId}")
    public R<FileEntity> getFileInfo(
            @Parameter(description = "文件ID")
            @PathVariable String fileId) {

        try {
            FileEntity fileInfo = fileService.getFileInfo(fileId);
            return R.ok(fileInfo);
        } catch (Exception e) {
            log.error("获取文件详情失败: {}", fileId, e);
            return R.error("获取文件详情失败");
        }
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{fileId}")
    public void download(
            @Parameter(description = "文件ID")
            @PathVariable("fileId") String fileId,
            HttpServletResponse response) {

        log.info("文件下载: fileId={}", fileId);
        fileService.download(fileId, response);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/delete/{fileId}")
    @PreAuthorize("@permissionChecker.hasPermission('system:files:delete')")
    public R<Void> delete(
            @Parameter(description = "文件ID")
            @PathVariable("fileId") String fileId) {

        log.info("删除文件: fileId={}", fileId);
        try {
            fileService.delete(fileId);
            return R.ok();
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileId, e);
            return R.error("删除失败");
        }
    }

    @Operation(summary = "批量删除文件")
    @DeleteMapping("/batchDelete")
    @PreAuthorize("@permissionChecker.hasPermission('system:files:delete')")
    public R<String> batchDelete(@Valid @RequestBody BatchDeleteRequest request) {

        log.info("批量删除文件: ids={}", request.getIds());
        try {
            fileService.batchDelete(request.getIds());
            return R.ok();
        } catch (Exception e) {
            log.error("批量删除文件失败", e);
            return R.error("批量删除失败");
        }
    }

    @Operation(summary = "更新下载次数")
    @PutMapping("/updateDownloadCount/{fileId}")
    public R<Void> updateDownloadCount(
            @Parameter(description = "文件ID")
            @PathVariable("fileId") String fileId) {

        log.info("更新下载次数: fileId={}", fileId);
        try {
            fileService.updateDownloadCount(fileId);
            return R.ok();
        } catch (Exception e) {
            log.error("更新下载次数失败: {}", fileId, e);
            return R.error("更新失败");
        }
    }


}