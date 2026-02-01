package com.sby.project.system.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sby.project.system.user.entity.SysUserRole;
import com.sby.project.system.user.mapper.SysUserRoleMapper;
import com.sby.project.system.user.service.SysUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
}