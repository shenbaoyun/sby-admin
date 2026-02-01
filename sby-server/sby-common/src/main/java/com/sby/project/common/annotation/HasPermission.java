package com.sby.project.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 标注在 Controller 方法上，用于检查当前登录用户是否拥有指定权限
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HasPermission {

    /**
     * 权限标识（如：system:user:add）
     */
    String value();
}