package com.sby.project.system.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sby.project.system.menu.service.SysMenuService;
import com.sby.project.system.role.service.SysRoleService;
import com.sby.project.system.user.dto.UserQueryDTO;
import com.sby.project.system.user.dto.UserSaveDTO;
import com.sby.project.system.user.entity.SysUser;
import com.sby.project.system.user.entity.SysUserRole;
import com.sby.project.system.user.mapper.SysUserMapper;
import com.sby.project.system.user.mapper.SysUserRoleMapper;
import com.sby.project.system.user.service.SysUserRoleService;
import com.sby.project.system.user.service.SysUserService;
import com.sby.project.system.user.vo.UserInfoVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户管理业务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SysUserRoleService sysUserRoleService;

    @Resource
    private SysMenuService menuService;

    @Resource
    private SysRoleService roleService;

    /**
     * 分页查询用户
     * @param page 分页参数对象
     * @param userQueryDTO 查询条件传输对象
     * @return 分页结果集
     */
    @Override
    public Page<SysUser> pageList(Page<SysUser> page, UserQueryDTO userQueryDTO) {
        // 1. 判空处理，防止 NPE
        if (userQueryDTO == null) {
            return this.page(page);
        }

        // 2. 构造动态 SQL 条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        // 模糊匹配用户名：只有当前端传了 username 时才拼接 SQL
        wrapper.like(StringUtils.hasText(userQueryDTO.getUsername()),
                SysUser::getUsername,
                userQueryDTO.getUsername());

        // 默认按创建时间倒序排列（新用户在前）
        wrapper.orderByDesc(SysUser::getCreateTime);

        // 3. 执行查询并返回结果
        return this.page(page, wrapper);
    }



    /**
     * 新增用户
     * @param userSaveDTO 用户保存对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务控制，失败自动回滚
    public void saveUser(UserSaveDTO userSaveDTO) {
        // 1. 业务校验：用户名不能重复
        Long count = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, userSaveDTO.getUsername()));
        if (count > 0) {
            throw new RuntimeException("用户名已存在，请更换！");
        }

        // 2. 实体转换
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userSaveDTO, user);

        // 3. 密码安全：BCrypt 强哈希加密
        if (StringUtils.hasText(userSaveDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userSaveDTO.getPassword()));
        } else {
            throw new RuntimeException("新增用户必须设置初始密码");
        }

        // 4. 执行插入：MyBatis-Plus 自动生成雪花算法 ID 并回填至 user 对象
        this.save(user);

        // 5. 处理多对多关联：写入用户-角色关联表
        saveUserRoleRelations(user.getId(), userSaveDTO.getRoleIds());
    }

    /**
     * 修改用户
     * @param userSaveDTO 用户修改对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserSaveDTO userSaveDTO) {
        // 1. 校验用户是否存在
        SysUser oldUser = this.getById(userSaveDTO.getId());
        if (oldUser == null) {
            throw new RuntimeException("修改失败：用户不存在");
        }

        // 2. 更新用户信息：排除密码，防止误操作将原密码置空
        SysUser user = new SysUser();
        BeanUtils.copyProperties(userSaveDTO, user, "password");

        // 如果前端传了新密码，则进行加密更新，否则保留原密码
        if (StringUtils.hasText(userSaveDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userSaveDTO.getPassword()));
        }
        this.updateById(user);

        // 3. 更新权限关联：采用“先删后增”策略，保证数据最终一致性
        // 先删除该用户旧的所有角色关系
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId()));

        // 重新插入新的角色关系
        saveUserRoleRelations(user.getId(), userSaveDTO.getRoleIds());

        // 4. 清理缓存：数据变更后，强制清理 Redis 中的权限和 Token 缓存
        // 这样用户在下一次请求时会重新拉取最新权限，或因 Token 失效重新登录
        String accessKey = "login:access:" + user.getId();
        String refreshKey = "login:refresh:" + user.getId();
        String permissionKey = "login:permission:" + user.getId();
        stringRedisTemplate.delete(Arrays.asList(accessKey, refreshKey, permissionKey));
    }

    /**
     * 私有辅助方法：保存用户与角色的关联关系
     */
    private void saveUserRoleRelations(Long userId, List<Long> roleIds) {
        if (!CollectionUtils.isEmpty(roleIds)) {
            // 构造对象列表
            List<SysUserRole> list = roleIds.stream()
                    .map(roleId -> new SysUserRole(userId, roleId))
                    .toList();
            // 直接调用 MP 自带的批量保存
            sysUserRoleService.saveBatch(list);
        }
    }

    /**
     * 删除用户
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 1. 检查用户是否存在（可选）
        SysUser user = this.getById(id);
        if (user == null) {
            return; // 用户不存在直接返回，保证幂等性
        }

        // 2. 逻辑删除用户记录
        // 因为实体类上有 @TableLogic，此方法实际执行的是 UPDATE SET deleted = 1
        this.removeById(id);

        // 3. 清理中间表关联 (sys_user_role)
        // 雖然用戶被逻辑删除了，但為了數據整洁，建议清理掉关联表
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));

        // 4. 销毁缓存 (核心步骤)
        // 必须让该用户的所有 Token 失效，否则被删除的用户在 Token 过期前依然能访问系统
        String accessKey = "login:access:" + id;
        String refreshKey = "login:refresh:" + id;
        String permissionKey = "login:permission:" + id;

        // 批量删除 Redis Key
        stringRedisTemplate.delete(Arrays.asList(accessKey, refreshKey, permissionKey));
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        // 1. 获取并校验用户信息
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("获取用户信息失败，用户不存在");
        }
        // 密码脱敏，保护安全
        user.setPassword(null);

        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUser(user);

        // 2. 获取角色标识 (Set 自动去重)
        // 建议：如果 userId 为 1L，可以直接添加 "admin" 标识
        Set<String> roles = roleService.selectRoleKeysByUserId(userId);
        if (userId == 1L) {
            roles.add("admin");
        }
        userInfoVO.setRoles(roles);

        // 3. 获取权限标识 (重点处理超级管理员)
        Set<String> permissions;
        if (userId == 1L) {
            // 超级管理员拥有所有权限标识
            permissions = new HashSet<>();
            permissions.add("*:*:*");
        } else {
            List<String> permsList = menuService.selectPermsByUserId(userId);
            // 过滤掉空字符串和 null，防止前端解析报错
            permissions = permsList.stream()
                    .filter(StringUtils::hasText)
                    .collect(Collectors.toSet());
        }
        userInfoVO.setPermissions(permissions);

        return userInfoVO;
    }
}