package com.mok.baseframe.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mok.baseframe.dao.SecurityUserMapper;
import com.mok.baseframe.entity.UserEntity;
import com.mok.baseframe.service.SecurityUserService;
import org.springframework.stereotype.Service;

/**
 * @description: 认证模块的 用户 service 接口实现类
 * @author: JN
 * @date: 2026/1/15 12:33
 * @param:
 * @return:
 **/
@Service
public class SecurityUserServiceImpl
        extends ServiceImpl<SecurityUserMapper, UserEntity>
        implements SecurityUserService {
}
