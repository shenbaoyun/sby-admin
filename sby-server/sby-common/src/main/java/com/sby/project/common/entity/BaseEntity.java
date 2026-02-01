package com.sby.project.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体基类
 * <p>
 * 提取所有数据库表的公共审计字段。
 * 通过继承此类，子类实体将自动具备主键、审计跟踪、乐观锁和逻辑删除功能。
 * 配合 MyMetaObjectHandler 实现公共字段的自动填充。
 * </p>
 *
 * @author Gemini
 * @since 2026-02-01
 */
@Data
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     * <p>使用 Mybatis-Plus 雪花算法 (ASSIGN_ID) 自动生成全局唯一的长整型 ID</p>
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间
     * <p>在记录首次插入数据库时自动填充当前系统时间，后续不可更改</p>
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * <p>在记录插入及每次执行更新操作时，自动刷新为当前系统时间</p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建者用户 ID
     * <p>在记录插入时，从登录上下文 (BaseContext) 获取当前用户 ID 并自动填充</p>
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createUserId;

    /**
     * 更新者用户 ID
     * <p>在记录插入及更新时，自动同步当前操作人的 ID</p>
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateUserId;

    /**
     * 乐观锁版本号
     * <p>
     * 用于防止并发修改冲突。
     * 插入时默认为 1。更新时会自动执行：SET version = version + 1 WHERE version = old_version
     * </p>
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    /**
     * 逻辑删除标识
     * <p>
     * 0 代表记录正常（未删除）
     * 1 代表记录已逻辑删除（在回收站中，SQL 查询会自动过滤）
     * </p>
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}