package com.sby.project.system.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 用户保存/修改 DTO
 */
@Data
public class UserSaveDTO implements Serializable {

    /**
     * 用户ID (修改时必传，新增时为 null)
     */
    private Long id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    private String username;

    /**
     * 密码 (新增时必传，修改时如果不传则表示不修改密码)
     */
    private String password;

    /**
     * 用户昵称
     */
    @NotBlank(message = "用户昵称不能为空")
    private String nickname;

    /**
     * 角色ID集合
     * 用于在保存用户的同时，关联用户与角色的关系
     */
    private List<Long> roleIds;
}