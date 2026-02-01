package com.sby.project.system.role.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sby.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 角色名称 (如: 超级管理员) */
    private String roleName;

    /** 角色权限字符串 (用于权限拦截器判断，如: admin) */
    private String roleKey;

    /** 显示顺序 */
    private Integer sort;

    /** 角色状态（0正常 1停用） */
    private Integer status;

    /** 备注 */
    private String remark;
}