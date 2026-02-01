package com.sby.project.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sby.project.system.role.entity.SysRoleMenu;
import com.sby.project.system.role.mapper.SysRoleMenuMapper;
import com.sby.project.system.role.service.SysRoleMenuService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色菜单关联业务实现类
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        // 1. 根据角色ID查询中间表
        List<SysRoleMenu> list = this.list(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));

        // 2. 提取并返回菜单ID列表
        return list.stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }
}