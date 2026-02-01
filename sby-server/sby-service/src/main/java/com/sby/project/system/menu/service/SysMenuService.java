package com.sby.project.system.menu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sby.project.system.menu.dto.MenuSaveDTO;
import com.sby.project.system.menu.entity.SysMenu;
import java.util.List;

/**
 * 菜单业务接口
 */
public interface SysMenuService extends IService<SysMenu> {

    /** 查询树形列表 */
    List<SysMenu> treeList();

    /** 保存菜单 */
    void saveMenu(MenuSaveDTO menuDTO);

    /** 修改菜单 */
    void updateMenu(MenuSaveDTO menuDTO);

    /** 删除菜单（含校验） */
    void deleteMenu(Long id);

    List<String> selectPermsByUserId(Long userId);
}