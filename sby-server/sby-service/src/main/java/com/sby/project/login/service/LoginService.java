package com.sby.project.login.service;

import com.sby.project.login.dto.LoginDTO;

import java.util.Map;

public interface LoginService {
    Map<String, String> login(LoginDTO loginDTO);

    void logout();

    /**
     * 根据刷新令牌获取新的双令牌
     * @param refreshToken 刷新令牌
     * @return 新的 AccessToken 和 RefreshToken
     */
    Map<String, String> refreshToken(String refreshToken);
}
