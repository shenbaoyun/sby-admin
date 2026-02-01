package com.sby.project.system.role.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sby.project.system.role.entity.SysRoleMenu;
import java.util.List;

/**
 * 角色菜单关联业务接口
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 根据角色ID获取已分配的菜单ID列表
     * @param roleId 角色ID
     * @return 菜单ID集合
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);
}