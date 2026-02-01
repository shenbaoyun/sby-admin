package com.sby.project.common.aspect;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.sby.project.common.annotation.HasPermission;
import com.sby.project.common.context.BaseContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Set;

/**
 * 权限校验切面
 * 基于 JDK 21 + Spring Boot 3.5 + Fastjson 2
 */
@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 在带有 @HasPermission 注解的方法执行前进行权限校验
     * @param hasPermission 注解对象
     */
    @Before("@annotation(hasPermission)")
    public void doBefore(HasPermission hasPermission) {
        // 1. 获取当前登录用户 ID (JDK 21 推荐对结果进行即时判空)
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new RuntimeException("未获取到登录信息，请重新登录");
        }

        // 2. 校验注解填写的权限标识是否合法
        String requiredPerm = hasPermission.value();
        if (!StringUtils.hasText(requiredPerm)) {
            return; // 或者抛出异常，视业务而定
        }

        // 3. 从 Redis 中读取权限列表
        String permKey = "login:permission:" + userId;
        String permsJson = stringRedisTemplate.opsForValue().get(permKey);

        if (!StringUtils.hasText(permsJson)) {
            throw new RuntimeException("权限验证失败，用户权限数据缺失");
        }

        // 4. 使用 Fastjson 2 解析为 Set，提升 contains() 性能
        // TypeReference 确保泛型在反序列化时不丢失
        Set<String> perms = JSON.parseObject(permsJson, new TypeReference<Set<String>>() {});

        // 防止 perms 为空
        if (perms == null) {
            perms = Collections.emptySet();
        }

        // 5. 校验权限
        if (!perms.contains(requiredPerm)) {
            // 提示：实际开发中推荐抛出自定义异常如 PermissionException
            // 这样全局异常处理器（@RestControllerAdvice）可以捕获并返回 403 状态码
            throw new RuntimeException("权限不足，无法操作：" + requiredPerm);
        }
    }
}