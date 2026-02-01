package com.sby.project.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.sby.project.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 系统用户实体类
 * <p>继承 BaseEntity，具备自动填充、逻辑删除及乐观锁功能</p>
 *
 * @author Gemini
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 (登录账号) */
    private String username;

    /** 密码 (加密存储) */
    private String password;

    /** 用户昵称 */
    private String nickname;

    /** 账号状态（0正常 1停用） */
    private Integer status;

    /** 备注 */
    private String remark;
}