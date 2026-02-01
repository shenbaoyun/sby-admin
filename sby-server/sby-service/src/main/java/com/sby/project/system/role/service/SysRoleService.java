package com.sby.project.system.role.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sby.project.system.role.dto.RoleSaveDTO;
import com.sby.project.system.role.entity.SysRole;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Set;

public interface SysRoleService extends IService<SysRole> {
    // 分页查询
    Page<SysRole> pageList(Page<SysRole> page, String roleName);
    // 保存角色及权限
    void saveRole(RoleSaveDTO roleSaveDTO);
    // 修改角色及权限
    void updateRole(RoleSaveDTO roleSaveDTO);
    // 删除角色
    void deleteRole(Long id);

    void assignMenus(@NotNull(message = "角色ID不能为空") Long roleId, List<Long> menuIds);

    Set<String> selectRoleKeysByUserId(Long userId);
}