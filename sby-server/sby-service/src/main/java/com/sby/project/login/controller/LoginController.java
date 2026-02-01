package com.sby.project.login.controller;

import com.alibaba.fastjson2.JSONObject;
import com.sby.project.common.result.Result;
import com.sby.project.login.dto.RefreshDTO;
import com.sby.project.login.service.LoginService;
import com.sby.project.login.dto.LoginDTO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

/**
 * 登录授权
 */
@Slf4j
@RestController
@RequestMapping("/auth") // 建议增加统一前缀
public class LoginController {

    @Resource
    private LoginService loginService;

    /**
     * 用户登录
     * @param loginDTO 登录信息
     */
    @PostMapping("/login")
    public Result<JSONObject> login(@RequestBody @Valid LoginDTO loginDTO) {
        return Result.success(loginService.login(loginDTO));
    }

    /**
     * 专门用于续期的接口
     * 前端在 AccessToken 过期后，携带 RefreshToken 请求此接口
     * @param refreshDTO
     * @return
     */
    @PostMapping("/refresh")
    public Result<JSONObject> refresh(@RequestBody @Valid RefreshDTO refreshDTO) {
        return Result.success(loginService.refreshToken(refreshDTO.getRefreshToken()));
    }

    /**
     * 退出登录接口
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        loginService.logout();
        return Result.success("退出成功");
    }
}