package com.sby.project.login.controller;

import com.sby.project.common.result.Result;
import com.sby.project.login.dto.RefreshDTO;
import com.sby.project.login.service.LoginService;
import com.sby.project.login.dto.LoginDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/auth") // 建议增加统一前缀
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody @Valid LoginDTO loginDTO) {
        return Result.success(loginService.login(loginDTO));
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refresh(@RequestBody @Valid RefreshDTO refreshDTO) {
        return Result.success(loginService.refreshToken(refreshDTO.getRefreshToken()));
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        loginService.logout();
        return Result.success("退出成功");
    }
}