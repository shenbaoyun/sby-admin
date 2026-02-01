package com.sby.project.login.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sby.project.common.context.BaseContext;
import com.sby.project.login.dto.LoginDTO;
import com.sby.project.login.service.LoginService;
import com.sby.project.login.util.JwtUtils;
import com.sby.project.sys.entity.SysUser;
import com.sby.project.sys.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    // 建议定义在常量类或 Service 顶部
    private static final long ACCESS_EXPIRE_MINUTES = 30;
    private static final long REFRESH_EXPIRE_DAYS = 7;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder; // 注入加密器

    @Override
    public Map<String, String> login(LoginDTO loginDTO) {
        log.info("第四步，执行 login ");
        // 1. 根据用户名查询用户
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));

        return createAndStoreToken(user.getId(), user.getUsername());
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken) {
        try {
            // 1. 解析 RefreshToken (JwtUtils 会自动校验是否过期)
            Claims claims = JwtUtils.parseToken(refreshToken);
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = (String) claims.get("username");

            // 2. 校验 Redis
            // 只有 Redis 里存在且和传入的一致，才允许续期
            String redisKey = "login:refresh_token:" + userId;
            String savedToken = stringRedisTemplate.opsForValue().get(redisKey);

            if (savedToken == null || !savedToken.equals(refreshToken)) {
                throw new RuntimeException("刷新令牌已失效，请重新登录");
            }

            return createAndStoreToken(userId, username);

        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            throw new RuntimeException("身份验证已过期，请重新登录");
        }
    }

    private Map<String, String> createAndStoreToken(Long userId, String username) {
        // 1. 准备 Claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // 2. 生成双 Token (统一计算毫秒数)
        String accessToken = JwtUtils.createToken(claims, ACCESS_EXPIRE_MINUTES * 60 * 1000L);
        String refreshToken = JwtUtils.createToken(claims, REFRESH_EXPIRE_DAYS * 24 * 60 * 60 * 1000L);

        // 3. 存入 Redis
        String accessKey = "login:access_token:" + userId;
        String refreshKey = "login:refresh_token:" + userId;

        stringRedisTemplate.opsForValue().set(accessKey, accessToken, ACCESS_EXPIRE_MINUTES, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(refreshKey, refreshToken, REFRESH_EXPIRE_DAYS, TimeUnit.DAYS);

        // 4. 封装返回
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }

    @Override
    public void logout() {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            // 1. 删除 AccessToken
            stringRedisTemplate.delete("login:access_token:" + userId);
            // 2. 删除 RefreshToken
            stringRedisTemplate.delete("login:refresh_token:" + userId);
            log.info("用户 ID: {} 已安全退出，Redis Token 已清除", userId);
        }

    }
}