package com.factorylink.admin.customize.service.login;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.factorylink.common.enums.BasicEnumUtil;
import com.factorylink.common.enums.common.StatusEnum;
import com.factorylink.domain.system.menu.db.SysMenuEntity;
import com.factorylink.domain.system.menu.db.SysMenuService;
import com.factorylink.domain.system.role.db.SysRoleEntity;
import com.factorylink.domain.system.role.db.SysRoleService;
import com.factorylink.infrastructure.user.web.DataScopeEnum;
import com.factorylink.infrastructure.user.web.RoleInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Component;

/**
 * 统一解析登录态中的角色权限快照。
 */
@Component
@RequiredArgsConstructor
public class RoleInfoResolver {

    private final SysMenuService menuService;

    private final SysRoleService roleService;

    public RoleInfo resolve(Long roleId, boolean isAdmin) {
        if (roleId == null) {
            return RoleInfo.EMPTY_ROLE;
        }

        if (isAdmin) {
            QueryWrapper<SysMenuEntity> menuQuery = new QueryWrapper<>();
            menuQuery.select("menu_id");
            List<SysMenuEntity> allMenus = menuService.list(menuQuery);
            Set<Long> allMenuIds = allMenus.stream().map(SysMenuEntity::getMenuId).collect(Collectors.toSet());

            return new RoleInfo(RoleInfo.ADMIN_ROLE_ID, RoleInfo.ADMIN_ROLE_KEY, DataScopeEnum.ALL, SetUtils.emptySet(),
                RoleInfo.ADMIN_PERMISSIONS, allMenuIds);
        }

        SysRoleEntity roleEntity = roleService.getById(roleId);
        if (roleEntity == null || !StatusEnum.ENABLE.getValue().equals(roleEntity.getStatus())) {
            return RoleInfo.EMPTY_ROLE;
        }

        List<SysMenuEntity> menuList = roleService.getMenuListByRoleId(roleId);
        Set<Long> menuIds = menuList.stream().map(SysMenuEntity::getMenuId).collect(Collectors.toSet());
        Set<String> permissions = menuList.stream()
            .map(SysMenuEntity::getPermission)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());

        DataScopeEnum dataScopeEnum = BasicEnumUtil.fromValue(DataScopeEnum.class, roleEntity.getDataScope());
        Set<Long> deptIdSet = SetUtils.emptySet();
        if (StrUtil.isNotEmpty(roleEntity.getDeptIdSet())) {
            deptIdSet = StrUtil.split(roleEntity.getDeptIdSet(), ",").stream()
                .map(Convert::toLong)
                .collect(Collectors.toSet());
        }

        return new RoleInfo(roleId, roleEntity.getRoleKey(), dataScopeEnum, deptIdSet, permissions, menuIds);
    }
}
