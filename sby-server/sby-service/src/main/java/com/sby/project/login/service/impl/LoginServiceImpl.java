package com.sby.project.login.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject; // 引入 FastJSON2
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sby.project.common.context.BaseContext;
import com.sby.project.login.dto.LoginDTO;
import com.sby.project.login.service.LoginService;
import com.sby.project.common.util.JwtUtils;
import com.sby.project.system.user.entity.SysUser;
import com.sby.project.system.user.mapper.SysUserMapper;
import com.sby.project.system.menu.mapper.SysMenuMapper;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    // 登录配置常量
    private static final long ACCESS_EXPIRE_MINUTES = 30; // 访问令牌 30 分钟
    private static final long REFRESH_EXPIRE_DAYS = 7;     // 刷新令牌 7 天
    private static final String REDIS_PERM_PREFIX = "login:permission:"; // 权限缓存前缀

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysMenuMapper sysMenuMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public JSONObject login(LoginDTO loginDTO) {
        log.info("执行登录业务流程，用户: {}", loginDTO.getUsername());

        // 1. 校验用户是否存在
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));



        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 核心：查询并同步权限到 Redis
        // 这里在登录时就完成权限初始化
        syncUserPermissions(user.getId());

        // 3. 调用统一签发 Token 方法
        return createAndStoreToken(user.getId(), user.getUsername());
    }

    @Override
    public JSONObject refreshToken(String refreshToken) {
        try {
            // 1. 解析校验 JWT 签名与过期时间
            Claims claims = JwtUtils.parseToken(refreshToken);
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = (String) claims.get("username");

            // 2. 安全校验：检查 Redis 中是否存在该 RefreshToken (防止被手动注销或拉黑)
            String redisRefreshKey = "login:refresh:" + userId;
            String savedToken = stringRedisTemplate.opsForValue().get(redisRefreshKey);

            if (savedToken == null || !savedToken.equals(refreshToken)) {
                throw new RuntimeException("刷新令牌已失效，请重新登录");
            }

            // 3. 【无感刷新关键点】续期时，顺便更新一次权限缓存
            // 这样能保证用户不重新登录也能获取到管理员新分配的权限
            syncUserPermissions(userId);

            // 4. 签发新的双 Token 对
            return createAndStoreToken(userId, username);

        } catch (Exception e) {
            log.error("Token刷新业务异常: {}", e.getMessage());
            throw new RuntimeException("身份校验未通过，请重新登录");
        }
    }

    /**
     * 内部私有方法：同步数据库权限到 Redis 缓存
     * <p>用于权限校验拦截器从缓存中获取权限，提升接口访问性能</p>
     */
    private void syncUserPermissions(Long userId) {
        // 1. 调用 Mapper 执行高效的三表联查获取权限标识符列表
        List<String> perms = sysMenuMapper.selectPermsByUserId(userId);

        // 2. 构造缓存 Key
        String permKey = REDIS_PERM_PREFIX + userId;

        // 3. 将权限列表存入 Redis
        // 注意：建议直接存 JSON 字符串，拦截器读取时性能更好
        if (perms == null) {
            perms = new ArrayList<>(); // 防止空指针，存入空集合
        }

        stringRedisTemplate.opsForValue().set(
                permKey,
                JSON.toJSONString(perms),
                REFRESH_EXPIRE_DAYS + 1,
                TimeUnit.DAYS
        );

        log.info("用户 ID: {} 的权限列表已刷新同步，权限数: {}", userId, perms.size());
    }

    /**
     * 内部私有方法：统一生成、存储、并返回 Token 对象
     */
    private JSONObject createAndStoreToken(Long userId, String username) {
        // 1. 构建 JWT 载荷
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);

        // 2. 生产双令牌
        String accessToken = JwtUtils.createToken(claims, ACCESS_EXPIRE_MINUTES * 60 * 1000L);
        String refreshToken = JwtUtils.createToken(claims, REFRESH_EXPIRE_DAYS * 24 * 60 * 60 * 1000L);

        // 3. 持久化到 Redis (Access 用于拦截器校验，Refresh 用于续期校验)
        stringRedisTemplate.opsForValue().set("login:access:" + userId, accessToken, ACCESS_EXPIRE_MINUTES, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set("login:refresh:" + userId, refreshToken, REFRESH_EXPIRE_DAYS, TimeUnit.DAYS);

        // 4. 使用 FastJSON2 的 JSONObject 返回，优雅替代 Map
        JSONObject result = new JSONObject();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        return result;
    }

    @Override
    public void logout() {
        Long userId = BaseContext.getCurrentId();
        if (userId != null) {
            // 清理该用户的所有在线缓存（访问、刷新、权限）
            stringRedisTemplate.delete("login:access:" + userId);
            stringRedisTemplate.delete("login:refresh:" + userId);
            stringRedisTemplate.delete(REDIS_PERM_PREFIX + userId);
            log.info("用户 ID: {} 已执行登出逻辑，相关缓存已清理", userId);
        }
    }
}