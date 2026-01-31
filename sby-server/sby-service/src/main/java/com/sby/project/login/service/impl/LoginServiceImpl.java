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

        // 2. 生成双 Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());

        // 生成 Access Token (有效期 30 分钟)
//        String accessToken = JwtUtils.createToken(claims, 30 * 60 * 1000L);
//        // 生成 Refresh Token (有效期 7 天)
//        String refreshToken = JwtUtils.createToken(claims, 7 * 24 * 60 * 60 * 1000L);

        // 生成 Access Token (有效期 30 分钟)
        String accessToken = JwtUtils.createToken(claims, 30 * 1000L);
        // 生成 Refresh Token (有效期 7 天)
        String refreshToken = JwtUtils.createToken(claims, 2 * 60 * 1000L);

        // 3. 存入 Redis (Key 区分开)
        String accessKey = "login:access_token:" + user.getId();
        String refreshKey = "login:refresh_token:" + user.getId();

        stringRedisTemplate.opsForValue().set(accessKey, accessToken, 30, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(refreshKey, refreshToken, 7, TimeUnit.DAYS);

        // 4. 返回给前端
        Map<String, String> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
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

            // 3. 校验通过，生成新的一对 Token
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("userId", userId);
            newClaims.put("username", username);

            // 生成新的 AccessToken (30分钟) 和 RefreshToken (7天)
//            String newAccessToken = JwtUtils.createToken(newClaims, 30 * 60 * 1000L);
//            String newRefreshToken = JwtUtils.createToken(newClaims, 7 * 24 * 60 * 60 * 1000L);
            String newAccessToken = JwtUtils.createToken(newClaims, 30 * 1000L);
            String newRefreshToken = JwtUtils.createToken(newClaims, 2 * 60 * 1000L);

            // 4. 更新 Redis
            String accessKey = "login:access_token:" + userId;
            String refreshKey = "login:refresh_token:" + userId;

            stringRedisTemplate.opsForValue().set(accessKey, newAccessToken, 30, TimeUnit.MINUTES);
            stringRedisTemplate.opsForValue().set(refreshKey, newRefreshToken, 7, TimeUnit.DAYS);

            // 5. 返回
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);
            return tokens;

        } catch (Exception e) {
            log.error("Token刷新失败: {}", e.getMessage());
            throw new RuntimeException("身份验证已过期，请重新登录");
        }
    }

    @Override
    public void logout() {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            // 1. 删除 AccessToken
            stringRedisTemplate.delete("login:access:" + userId);
            // 2. 删除 RefreshToken
            stringRedisTemplate.delete("login:refresh:" + userId);
            log.info("用户 ID: {} 已安全退出，Redis Token 已清除", userId);
        }

    }
}