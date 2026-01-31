package com.sby.project.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.sby.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true) // lombok 包含父类字段
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
}