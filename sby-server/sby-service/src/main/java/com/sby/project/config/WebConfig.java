package com.sby.project.config;

import com.sby.project.login.interceptor.LoginInterceptor;
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

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                // WebConfig.java
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/refresh",
                        "/error",
                        "/favicon.ico",
                        // --- Knife4j & Swagger 放行路径 ---
                        "/doc.html",            // Knife4j 的主页
                        "/swagger-ui.html",     // 原生 Swagger 主页
                        "/swagger-ui/**",       // Swagger 静态资源
                        "/v3/api-docs/**",      // 关键：数据源子路径（如 swagger-config）
                        "/webjars/**"           // 静态资源文件
                );
    }
}