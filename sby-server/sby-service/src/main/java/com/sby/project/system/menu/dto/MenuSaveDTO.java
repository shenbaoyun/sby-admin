package com.sby.project.system.menu.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 菜单保存/修改数据传输对象
 */
@Data
public class MenuSaveDTO {

    private Long id; // 修改时必传

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    private Integer sort;

    private String path;

    private String component;

    private String perms;

    private String icon;

    private String menuType; // M目录 C菜单 F按钮

    private Integer visible; // 0显示 1隐藏

    private Integer status;  // 0正常 1停用

    private String remark;
}