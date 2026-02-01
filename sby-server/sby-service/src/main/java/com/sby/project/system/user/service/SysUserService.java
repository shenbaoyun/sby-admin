package com.sby.project.system.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sby.project.system.user.dto.UserQueryDTO;
import com.sby.project.system.user.dto.UserSaveDTO;
import com.sby.project.system.user.entity.SysUser;
import com.sby.project.system.user.vo.UserInfoVO;

public interface SysUserService extends IService<SysUser> {
    Page<SysUser> pageList(Page<SysUser> page, UserQueryDTO userQueryDTO);

    void saveUser(UserSaveDTO userSaveDTO);

    void updateUser(UserSaveDTO userSaveDTO);

    void deleteUser(Long id);

    UserInfoVO getUserInfo(Long userId);
}
