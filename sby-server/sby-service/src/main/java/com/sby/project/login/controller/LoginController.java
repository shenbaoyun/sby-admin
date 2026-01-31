package com.sby.project.login.controller;

import com.sby.project.common.result.Result;
import com.sby.project.login.service.LoginService;
import com.sby.project.login.service.impl.LoginServiceImpl;
import com.sby.project.login.dto.LoginDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO loginDTO) {
        // 现在返回的是包含 accessToken 和 refreshToken 的 Map
        Map<String, String> tokens = loginService.login(loginDTO);
        return Result.success(tokens);
    }

    /**
     * 专门用于续期的接口
     * 前端在 AccessToken 过期后，携带 RefreshToken 请求此接口
     */
    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestParam String refreshToken) {
        Map<String, String> newTokens = loginService.refreshToken(refreshToken);
        return Result.success(newTokens);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        loginService.logout();
        return Result.success("退出成功");
    }
}