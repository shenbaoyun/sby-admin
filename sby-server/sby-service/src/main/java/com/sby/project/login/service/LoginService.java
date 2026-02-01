package com.sby.project.login.service;

import com.alibaba.fastjson2.JSONObject;
import com.sby.project.login.dto.LoginDTO;

import java.util.Map;

public interface LoginService {
    JSONObject login(LoginDTO loginDTO);

    void logout();

    /**
     * 根据刷新令牌获取新的双令牌
     * @param refreshToken 刷新令牌
     * @return 新的 AccessToken 和 RefreshToken
     */
    JSONObject refreshToken(String refreshToken);
}
