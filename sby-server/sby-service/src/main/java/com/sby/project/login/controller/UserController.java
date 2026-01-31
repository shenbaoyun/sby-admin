package com.sby.project.login.controller;

import com.sby.project.common.context.BaseContext;
import com.sby.project.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @GetMapping("/info")
    public Result<String> getInfo() {

        log.info("第三步，注册拦截器");
        log.info("*****************************************************");
        // 如果拦截器通过，这里能执行
        return Result.success("当前登录用户 ID 是: " + BaseContext.getCurrentId());
    }
}