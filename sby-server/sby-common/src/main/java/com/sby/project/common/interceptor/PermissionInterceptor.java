package com.sby.project.common.interceptor;

import com.alibaba.fastjson2.JSON;
import com.sby.project.common.annotation.HasPermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 权限拦截器：基于注解校验 Redis 中的权限列表
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 如果访问的不是控制器方法（如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 2. 获取方法上的 RequiresPermissions 注解
        HasPermission annotation = handlerMethod.getMethodAnnotation(HasPermission.class);

        // 3. 如果方法上没有该注解，说明不需要权限校验，放行
        if (annotation == null) {
            return true;
        }

        // 4. 获取注解要求的权限标识 (如 sys:user:delete)
        String requiredPerm = annotation.value();

        // 5. 从 Header 或 Token 获取当前用户 ID (这里假设你已经把 userId 存入 request 域)
        // 实际生产中通常从 Token 解析，此处简化演示
        String userId = request.getHeader("X-User-Id");
        if (!StringUtils.hasText(userId)) {
            throw new RuntimeException("未获取到用户信息，拒绝访问");
        }

        // 6. 从 Redis 获取该用户的权限列表
        String permKey = "login:permission:" + userId;
        String permJson = stringRedisTemplate.opsForValue().get(permKey);

        if (!StringUtils.hasText(permJson)) {
            throw new RuntimeException("用户权限已过期或未授权");
        }

        List<String> userPerms = JSON.parseArray(permJson, String.class);

        // 7. 校验用户是否拥有该权限
        if (userPerms.contains(requiredPerm)) {
            return true; // 校验通过
        }

        // 8. 校验失败，抛出异常或返回 403
        throw new RuntimeException("对不起，您没有权限进行此操作：" + requiredPerm);
    }
}