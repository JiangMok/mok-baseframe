package com.mok.baseframe.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mok.baseframe.entity.FileEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FileMapper extends BaseMapper<FileEntity> {
    
    /**
     * 更新下载次数 >>> 注解写 sql
     */
    @Update("UPDATE sys_file SET download_count = download_count + 1, update_time = NOW() WHERE id = #{id}")
    int incrementDownloadCount(@Param("id") String id);
    
    /**
     * 批量逻辑删除
     */
    int batchDelete(@Param("ids") List<String> ids, @Param("updateBy") String updateBy);
}