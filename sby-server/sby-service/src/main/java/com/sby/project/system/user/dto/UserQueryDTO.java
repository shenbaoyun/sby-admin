package com.sby.project.system.user.dto;

import lombok.Data;

@Data
public class UserQueryDTO {
    private String username;
    private String password;
    private String nickname;
    private String status;
    private String beginTime; // 数据库 Entity 里没有这个字段
    private String endTime;
}