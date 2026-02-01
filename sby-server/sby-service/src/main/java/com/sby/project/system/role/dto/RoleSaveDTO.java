package com.sby.project.system.role.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleSaveDTO {
    private Long id;          // 修改时必传
    private String roleName;  // 角色名称
    private String roleKey;   // 角色权限标识
    private Integer sort;     // 排序
    private Integer status;   // 状态
    private String remark;    // 备注
    private List<Long> menuIds; // 关联的菜单ID集合
}