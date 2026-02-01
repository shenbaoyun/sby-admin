package com.sby.project.common.interceptor;

import com.sby.project.common.context.BaseContext;
import com.sby.project.common.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：负责校验 JWT Token 并提取用户信息
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("第二步，执行拦截器");
        // 如果是 OPTIONS 请求，直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 1. 从请求头获取 AccessToken
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            return unAuthorized(response, "未携带令牌");
        }

        try {
            // 2. 解析 Token
            Claims claims = JwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());

            // 3. 校验 Redis 中的 AccessToken
            // 只有 Redis 里存在且一致，才算真的在线
            String redisKey = "login:access:" + userId;
            String redisToken = stringRedisTemplate.opsForValue().get(redisKey);

            System.out.println("redisToken = " + redisToken);
            System.out.println("token = " + token);
            if (redisToken == null || !redisToken.equals(token)) {
                return unAuthorized(response, "登录已过期或在别处登录");
            }

            // 4. 存入 ThreadLocal
            BaseContext.setCurrentId(userId);
            return true;

        } catch (Exception e) {
            // Token 解析失败（过期或伪造）
            return unAuthorized(response, "令牌无效或已过期");
        }
    }

    // 提取一个通用的返回 401 方法
    private boolean unAuthorized(HttpServletResponse response, String msg) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401, \"msg\":\"" + msg + "\"}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 7. 【重要】请求结束后清理 ThreadLocal，防止内存泄漏和数据污染
        BaseContext.removeCurrentId();
        log.info("清理线程变量完成");
    }
}