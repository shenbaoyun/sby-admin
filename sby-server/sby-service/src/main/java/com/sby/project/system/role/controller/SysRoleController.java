package com.sby.project.system.role.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sby.project.common.annotation.HasPermission;
import com.sby.project.common.result.Result;
import com.sby.project.system.role.dto.RoleAssignDTO;
import com.sby.project.system.role.dto.RoleSaveDTO;
import com.sby.project.system.role.entity.SysRole;
import com.sby.project.system.role.entity.SysRoleMenu;
import com.sby.project.system.role.service.SysRoleMenuService;
import com.sby.project.system.role.service.SysRoleService;
import lombok.extern.java.Log;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理控制层
 * <p>提供角色的分页查询、新增、修改及删除等接口</p>
 *
 * @author Gemini
 * @since 2026-02-01
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Resource
    private SysRoleService roleService;

    @Resource
    private SysRoleMenuService roleMenuService;

    /**
     * 获取角色分页列表
     * * @param page     分页对象（包含当前页、每页条数）
     * @param roleName 角色名称（模糊查询关键字）
     * @return 包含分页数据的统一响应结果
     */
    @GetMapping("/list")
    public Result<Page<SysRole>> list(Page<SysRole> page, String roleName) {
        return Result.success(roleService.pageList(page, roleName));
    }

    /**
     * 根据角色ID获取已分配的菜单ID列表
     * <p>用于前端权限树回显勾选状态</p>
     */
    @GetMapping("/menuIds/{roleId}")
    @HasPermission("system:role:query")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long roleId) {
        // 调用 service 层的业务方法
        return Result.success(roleMenuService.selectMenuIdsByRoleId(roleId));
    }

    /**
     * 分配角色权限（菜单）
     * @param assignDTO 角色ID与菜单ID集合
     * @return 成功提示
     */
    @PutMapping("/assignMenus")
    @HasPermission("system:role:assign")
    public Result<String> assignMenus(@RequestBody @Validated RoleAssignDTO assignDTO) {
        roleService.assignMenus(assignDTO.getRoleId(), assignDTO.getMenuIds());
        return Result.success("分配成功");
    }

    /**
     * 新增角色
     * <p>包括角色基本信息及所分配的菜单权限 ID 集合</p>
     * * @param roleSaveDTO 角色保存信息传输对象
     * @return 成功提示信息
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody RoleSaveDTO roleSaveDTO) {
        roleService.saveRole(roleSaveDTO);
        return Result.success("新增成功");
    }

    /**
     * 修改角色
     * <p>更新角色信息并重新分配菜单权限，同时会触发相关用户的权限缓存清理</p>
     * * @param roleSaveDTO 角色修改信息传输对象（必须包含 ID）
     * @return 成功提示信息
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody RoleSaveDTO roleSaveDTO) {
        roleService.updateRole(roleSaveDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除角色
     * <p>逻辑删除角色信息，并同步清理角色与菜单、角色与用户的关联记录及相关缓存</p>
     * * @param id 角色主键 ID
     * @return 成功提示信息
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success("删除成功");
    }
}