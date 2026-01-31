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
        log.info("*****************************************************");
        log.info("第一步，注册拦截器");
        // 注册自定义拦截器
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")             // 拦截所有请求
                .excludePathPatterns(               // 排除不需要拦截的路径
                        "/login",                   // 登录接口放行
                        "/error",                   // 排除框架异常路径
                        "/favicon.ico",              // 排除浏览器图标请求
                        "/refresh"                  // token续期接口
                );
    }
}