package com.sby.project.system.user.vo;

import com.sby.project.system.user.entity.SysUser;
import lombok.Data;
import java.util.Set;

/**
 * 用户信息视图对象（包含权限和角色标识）
 */
@Data
public class UserInfoVO {
    /** 用户基本信息（脱敏后的实体或单独定义字段） */
    private SysUser user;

    /** 角色标识集合（如：admin, common） */
    private Set<String> roles;

    /** 权限标识集合（如：system:user:add, system:user:delete） */
    private Set<String> permissions;
}