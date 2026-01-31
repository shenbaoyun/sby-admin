package com.sby.project.common.context;

/**
 * 核心：基于 ThreadLocal 封装的工具类
 * 用于在同一个线程内保存和获取用户信息
 */
public class BaseContext {

    // 创建一个 ThreadLocal 变量
    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    // 存入当前登录用户ID
    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }

    // 获取当前登录用户ID
    public static Long getCurrentId() {
        return THREAD_LOCAL.get();
    }

    // 移除信息（防止内存泄漏，非常重要！）
    public static void removeCurrentId() {
        THREAD_LOCAL.remove();
    }
}