package com.sby.project.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sby.project.system.user.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    // 继承了 BaseMapper，已经自带了常用的 CRUD 方法
}