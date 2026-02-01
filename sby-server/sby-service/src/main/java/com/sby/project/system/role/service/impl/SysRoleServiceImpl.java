package com.sby.project.system.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sby.project.system.role.dto.RoleSaveDTO;
import com.sby.project.system.role.entity.SysRole;
import com.sby.project.system.role.entity.SysRoleMenu;
import com.sby.project.system.role.mapper.SysRoleMapper;
import com.sby.project.system.role.service.SysRoleMenuService;
import com.sby.project.system.role.service.SysRoleService;
import com.sby.project.system.user.entity.SysUserRole;
import com.sby.project.system.user.service.SysUserRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色管理业务实现类
 * * <p>主要功能：角色的增删改查、角色与菜单的权限绑定、以及变更后的缓存失效处理</p>
 *
 * @author Gemini
 * @since 2026-02-01
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysRoleMenuService sysRoleMenuService; // 角色-菜单关联服务的 IService 接口

    @Resource
    private SysUserRoleService sysUserRoleService; // 用户-角色关联服务的 IService 接口

    @Resource
    private SysRoleMenuService roleMenuService;

    /**
     * 分页查询角色列表
     * * @param page 分页参数
     * @param roleName 角色名称（模糊匹配）
     * @return 分页后的角色数据
     */
    @Override
    public Page<SysRole> pageList(Page<SysRole> page, String roleName) {
        return this.page(page, new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(roleName), SysRole::getRoleName, roleName)
                .orderByAsc(SysRole::getSort)); // 按显示顺序升序排列
    }

    @Override
    public Set<String> selectRoleKeysByUserId(Long userId) {
        // 调用 Mapper 查询
        return baseMapper.selectRoleKeysByUserId(userId);
    }

    /**
     * 新增角色并分配菜单权限
     * * @param roleSaveDTO 角色保存信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，保证多表操作一致性
    public void saveRole(RoleSaveDTO roleSaveDTO) {
        // 1. 保存角色基本信息
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleSaveDTO, role);
        this.save(role); // 执行后 role.getId() 会自动回填雪花ID

        // 2. 批量保存角色与菜单的关联关系
        saveRoleMenuRelations(role.getId(), roleSaveDTO.getMenuIds());
    }

    /**
     * 修改角色信息及其菜单权限
     * * @param roleSaveDTO 角色修改信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleSaveDTO roleSaveDTO) {
        // 1. 更新角色主体信息
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleSaveDTO, role);
        this.updateById(role);

        // 2. 更新关联权限：采用“覆盖更新”策略（先删旧，后增新）
        // 删除该角色原有的所有菜单关联
        sysRoleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, role.getId()));

        // 插入新的菜单关联列表
        saveRoleMenuRelations(role.getId(), roleSaveDTO.getMenuIds());

        // 3. 实时生效：清理所有拥有该角色的用户在 Redis 里的权限缓存
        clearUserCacheByRole(role.getId());
    }

    /**
     * 删除角色
     * * @param id 角色ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        // 1. 先清理相关用户的缓存（必须在删除关联关系前进行，否则无法通过角色找到用户）
        clearUserCacheByRole(id);

        // 2. 删除角色主体（逻辑删除，因 BaseEntity 中有 @TableLogic）
        this.removeById(id);

        // 3. 物理删除角色与菜单的关联
        sysRoleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, id));

        // 4. 物理删除用户与角色的关联（用户不再拥有此角色）
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getRoleId, id));
    }

    /**
     * 批量保存角色-菜单关联关系的内部私有方法
     * * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    private void saveRoleMenuRelations(Long roleId, List<Long> menuIds) {
        if (!CollectionUtils.isEmpty(menuIds)) {
            // 将 Long 类型的菜单 ID 列表转换为实体类列表
            List<SysRoleMenu> list = new ArrayList<>();
            for (Long menuId : menuIds) {
                list.add(new SysRoleMenu(roleId, menuId));
            }
            // 调用 IService 的批量保存方法，性能优于循环单次插入
            sysRoleMenuService.saveBatch(list);
        }
    }

    /**
     * 根据角色ID清理所有受影响用户的 Redis 缓存
     * * <p>当角色权限变更或角色被删除时，确保用户的 Token 和 权限标识 立即失效，强制重新拉取或登录</p>
     * * @param roleId 角色ID
     */
    private void clearUserCacheByRole(Long roleId) {
        // 1. 查询所有拥有此角色的用户 ID
        List<SysUserRole> userRoleList = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>()
                        .select(SysUserRole::getUserId) // 仅查询 userId 列，优化 SQL 性能
                        .eq(SysUserRole::getRoleId, roleId)
        );

        if (CollectionUtils.isEmpty(userRoleList)) {
            return;
        }

        // 2. 构造批量删除的 Redis Key 集合（包含权限缓存、Access Token 和 Refresh Token）
        List<String> keys = userRoleList.stream()
                .flatMap(ur -> java.util.stream.Stream.of(
                        "login:permission:" + ur.getUserId(),
                        "login:access:" + ur.getUserId(),
                        "login:refresh:" + ur.getUserId()
                ))
                .collect(Collectors.toList());

        // 3. 执行批量删除
        stringRedisTemplate.delete(keys);
    }

    /**
     * 分配权限业务实现
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 1. 参数校验：检查角色是否存在
        SysRole role = this.getById(roleId);
        if (role == null) {
            throw new RuntimeException("分配失败：角色不存在");
        }

        // 2. 清理旧权限：删除该角色在 sys_role_menu 表中的所有历史关联
        roleMenuService.remove(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));

        // 3. 插入新权限：如果勾选了菜单，则批量插入
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> roleMenuList = menuIds.stream().map(menuId -> {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                return rm;
            }).collect(Collectors.toList());

            // 使用 MyBatis Plus 的批量保存方法
            roleMenuService.saveBatch(roleMenuList);
        }

        // 4. 核心步骤：清理受影响用户的缓存
        // 角色权限变了，所有拥有该角色的用户在 Redis 里的权限缓存都失效了，必须清除
        this.clearUserCacheByRole(roleId);
    }
}