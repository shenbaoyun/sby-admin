package com.sby.project.common.result;

import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable { // 加上序列化，方便后续存入 Redis

    private Integer code;    // 200-成功, 500-失败, 401-未登录
    private String msg;      // 提示信息
    private T data;          // 数据

    // 成功-带数据
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.msg = "操作成功";
        result.data = data;
        return result;
    }

    // 成功-不带数据 (比如用在删除、修改操作)
    public static <T> Result<T> success() {
        return success(null);
    }

    // 失败
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.code = 500;
        result.msg = msg;
        return result;
    }
}