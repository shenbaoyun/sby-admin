package com.sby.project.system.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * 角色分配权限 DTO
 */
@Data
public class RoleAssignDTO {

    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    /** 选中的菜单ID集合 */
    private List<Long> menuIds;
}