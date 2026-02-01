package com.sby.project.system.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sby.project.system.role.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色标识
     * @param userId 用户ID
     * @return 角色权限标识集合
     */
    Set<String> selectRoleKeysByUserId(@Param("userId") Long userId);
}