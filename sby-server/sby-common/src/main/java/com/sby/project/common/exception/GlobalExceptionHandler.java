package com.sby.project.common.exception;

import com.sby.project.common.result.Result; // 确保导入你的 Result 类
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.validation.ObjectError;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice // 该注解结合了 @ControllerAdvice 和 @ResponseBody
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常 (我们在代码里主动 throw 的 RuntimeException)
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<String> exceptionHandler(RuntimeException ex) {
        log.error("业务异常信息：{}", ex.getMessage());
        // 这里的 ex.getMessage() 就是你在 Service 里 throw "用户名密码错误" 的内容
        return Result.error(ex.getMessage());
    }

    /**
     * 捕获系统未预料的异常 (比如 SQL 报错、空指针等)
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex) {
        log.error("系统运行异常：", ex);
        return Result.error("系统繁忙，请稍后再试");
    }

    /**
     * 捕获参数校验异常 (@Valid 产生的异常)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidationException(MethodArgumentNotValidException ex) {
        // 提取所有的错误信息并用逗号拼接
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.error("参数校验失败：{}", message);
        return Result.error(message); // 返回给前端：用户名不能为空, 密码不能为空
    }
}