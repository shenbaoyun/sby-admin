package com.sby.project.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sby.project.system.role.entity.SysRoleMenu;
import com.sby.project.system.user.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
}