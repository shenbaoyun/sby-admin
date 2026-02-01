package com.sby.project.config;

import com.sby.project.common.interceptor.LoginInterceptor;
import com.sby.project.common.interceptor.PermissionInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 系统启动时，spring默认加载拦截器
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 定义统一的白名单
    private static final String[] WHITE_LIST = {
            "/auth/login",
            "/auth/refresh",
            "/error",
            "/favicon.ico"
    };

    @Resource
    private LoginInterceptor loginInterceptor;

    @Resource
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 先检查登录
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);

        // 再检查权限
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(WHITE_LIST);
    }
}