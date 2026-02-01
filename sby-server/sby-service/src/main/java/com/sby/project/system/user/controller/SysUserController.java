package com.sby.project.system.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sby.project.common.annotation.HasPermission;
import com.sby.project.common.context.BaseContext;
import com.sby.project.common.result.Result;
import com.sby.project.system.user.dto.UserQueryDTO;
import com.sby.project.system.user.dto.UserSaveDTO;
import com.sby.project.system.user.entity.SysUser;
import com.sby.project.system.user.service.SysUserService;
import com.sby.project.system.user.vo.UserInfoVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/system/user")
public class SysUserController {
    @Resource
    private SysUserService userService;


    /**
     * 获取当前登录用户信息
     * <p>包含基本信息、角色标识、权限标识</p>
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getInfo() {
        log.info("第三步，注册拦截器");
        log.info("*****************************************************");
        // 1. 从上下文获取当前登录用户 ID
        Long userId = BaseContext.getCurrentId();

        // 2. 调用 Service 获取完整权限包数据
        UserInfoVO userInfo = userService.getUserInfo(userId);

        return Result.success(userInfo);
    }

    /**
     * 分页查询用户
     */
    @GetMapping("/list")
    @HasPermission("sys:user:list")
    public Result<IPage<SysUser>> list(Page<SysUser> page, UserQueryDTO userQueryDTO) {
        Page<SysUser> sysUserPage = userService.pageList(page, userQueryDTO);
        return Result.success(sysUserPage);
    }

    /**
     * 新增用户
     */
    @PostMapping("/add")
    @HasPermission("sys:user:add")
    public Result<String> add(@RequestBody UserSaveDTO userSaveDTO) {
        // 密码加密逻辑建议放在 Service 层
        userService.saveUser(userSaveDTO);
        return Result.success("新增成功");
    }

    /**
     * 修改用户
     */
    @PutMapping("/update")
    @HasPermission("sys:user:edit")
    public Result<String> update(@RequestBody UserSaveDTO userSaveDTO) {
        userService.updateUser(userSaveDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除用户
     * @param id 用户ID
     */
    @DeleteMapping("/{id}")
    @HasPermission("sys:user:delete")
    public Result<String> delete(@PathVariable Long id) {
        // 逻辑删除
        userService.deleteUser(id);
        return Result.success("删除成功");
    }
}
