package com.sby.project.system.menu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sby.project.system.menu.dto.MenuSaveDTO;
import com.sby.project.system.menu.entity.SysMenu;
import com.sby.project.system.menu.mapper.SysMenuMapper;
import com.sby.project.system.menu.service.SysMenuService;
import com.sby.project.system.role.entity.SysRoleMenu;
import com.sby.project.system.role.service.SysRoleMenuService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单业务实现类
 * <p>包含树形转换算法及严谨的父子级校验逻辑</p>
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Resource
    private SysRoleMenuService sysRoleMenuService;

    @Override
    public List<SysMenu> treeList() {
        List<SysMenu> allMenus = this.list(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        return buildMenuTree(allMenus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMenu(MenuSaveDTO menuDTO) {
        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        this.save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuSaveDTO menuDTO) {
        if (menuDTO.getId() == null) {
            throw new RuntimeException("修改失败：菜单ID不能为空");
        }

        // 业务校验：上级菜单不能选择自己
        if (menuDTO.getId().equals(menuDTO.getParentId())) {
            throw new RuntimeException("修改失败：上级菜单不能选择自身");
        }

        // 业务校验：确保修改的菜单在库中存在
        SysMenu oldMenu = this.getById(menuDTO.getId());
        if (oldMenu == null) {
            throw new RuntimeException("修改失败：菜单不存在");
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        this.updateById(menu);
    }

    /**
     * 删除菜单
     * @param id 菜单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        // 1. 业务校验：检查是否存在子菜单
        // 防止删除父节点后，子节点变成无法访问的“孤儿数据”
        boolean hasChildren = this.count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id)) > 0;

        if (hasChildren) {
            throw new RuntimeException("删除失败：该菜单下还存在子菜单，请先删除子菜单");
        }

        // 2. 业务校验：检查该菜单是否已经分配给了角色
        // 只有解除所有角色的权限绑定后，才允许删除菜单，确保系统安全性
        boolean isUsedByRole = sysRoleMenuService.count(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getMenuId, id)) > 0;

        if (isUsedByRole) {
            throw new RuntimeException("删除失败：该菜单已分配给角色，请先在【角色管理】中取消分配");
        }

        // 3. 执行删除
        // 由于继承了 BaseEntity，这里执行的是逻辑删除
        boolean success = this.removeById(id);
        if (!success) {
            throw new RuntimeException("删除失败：数据可能已被他人删除");
        }
    }

    /**
     * 内部方法：扁平列表转树形结构 (两次遍历 Map 法)
     */
    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> tree = new ArrayList<>();
        Map<Long, SysMenu> map = menus.stream()
                .collect(Collectors.toMap(SysMenu::getId, m -> m));

        for (SysMenu menu : menus) {
            Long parentId = menu.getParentId();
            if (parentId == null || parentId == 0L) {
                tree.add(menu);
            } else {
                SysMenu parent = map.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(menu);
                }
            }
        }
        return tree;
    }

    @Override
    public List<String> selectPermsByUserId(Long userId) {
        // 如果是超级管理员(ID=1)，可以根据业务需求返回所有权限，或者特定标识
        if (userId != null && userId == 1L) {
            return baseMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                            .isNotNull(SysMenu::getPerms)
                            .ne(SysMenu::getPerms, ""))
                    .stream().map(SysMenu::getPerms).collect(Collectors.toList());
        }
        return baseMapper.selectPermsByUserId(userId);
    }
}