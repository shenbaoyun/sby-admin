package com.sby.project.system.menu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sby.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单权限实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 菜单名称 */
    private String menuName;

    /** 父菜单ID（一级菜单为0） */
    private Long parentId;

    /** 显示顺序 */
    private Integer sort;

    /** 路由地址 */
    private String path;

    /** 组件路径 (Vue 视图文件位置) */
    private String component;

    /** 权限标识 (用于按钮级权限校验，如: sys:user:add) */
    private String perms;

    /** 菜单图标 */
    private String icon;

    /** 菜单类型（M目录 C菜单 F按钮） */
    private String menuType;

    /** 菜单可见性（0显示 1隐藏） */
    private Integer visible;

    /** 菜单状态（0正常 1停用） */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 子菜单列表（逻辑字段，不属于数据库表） */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();
}