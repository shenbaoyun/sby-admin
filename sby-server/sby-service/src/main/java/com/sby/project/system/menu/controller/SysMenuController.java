package com.sby.project.system.menu.controller;

import com.sby.project.common.result.Result;
import com.sby.project.system.menu.dto.MenuSaveDTO;
import com.sby.project.system.menu.entity.SysMenu;
import com.sby.project.system.menu.service.SysMenuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 菜单管理控制层
 * <p>负责菜单的树形列表展示、维护及权限配置</p>
 *
 * @author Gemini
 * @since 2026-02-01
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Resource
    private SysMenuService menuService;

    /**
     * 获取菜单树形结构列表
     * @return 统一返回格式的树形菜单数据
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.success(menuService.treeList());
    }

    /**
     * 新增菜单
     * @param menuDTO 菜单保存对象
     * @return 成功提示
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody @Validated MenuSaveDTO menuDTO) {
        menuService.saveMenu(menuDTO);
        return Result.success("新增成功");
    }

    /**
     * 修改菜单
     * @param menuDTO 菜单修改对象
     * @return 成功提示
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody @Validated MenuSaveDTO menuDTO) {
        menuService.updateMenu(menuDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除菜单
     * @param id 菜单ID
     * @return 成功提示
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success("删除成功");
    }
}