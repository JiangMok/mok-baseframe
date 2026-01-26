package com.mok.baseframe.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.baseframe.dao.PermissionMapper;
import com.mok.baseframe.entity.PermissionEntity;
import com.mok.baseframe.service.SecurityPermissionService;
import org.springframework.stereotype.Service;

/**
 * @description: 认证模块 权限 service 接口实现类
 * @author: JN
 * @date: 2026/1/15 12:34
 * @param:
 * @return:
 **/
@Service
public class SecurityPermissionServiceImpl
        extends ServiceImpl<PermissionMapper, PermissionEntity>
        implements SecurityPermissionService {

}
