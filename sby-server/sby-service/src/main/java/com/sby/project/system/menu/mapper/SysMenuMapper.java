package com.sby.project.system.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sby.project.system.menu.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {
    /**
     * 根据用户ID查询权限标识符列表 (如：sys:user:add, sys:user:delete)
     */
    List<String> selectPermsByUserId(@Param("userId") Long userId);
}