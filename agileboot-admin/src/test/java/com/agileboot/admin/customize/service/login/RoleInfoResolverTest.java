package com.agileboot.admin.customize.service.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.common.enums.common.StatusEnum;
import com.agileboot.domain.system.menu.db.SysMenuEntity;
import com.agileboot.domain.system.menu.db.SysMenuService;
import com.agileboot.domain.system.role.db.SysRoleEntity;
import com.agileboot.domain.system.role.db.SysRoleService;
import com.agileboot.infrastructure.user.web.DataScopeEnum;
import com.agileboot.infrastructure.user.web.RoleInfo;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RoleInfoResolverTest {

    private final SysMenuService menuService = mock(SysMenuService.class);
    private final SysRoleService roleService = mock(SysRoleService.class);
    private final RoleInfoResolver resolver = new RoleInfoResolver(menuService, roleService);

    @Test
    void testResolveShouldReturnAdminRoleInfoForAdminUser() {
        when(menuService.list(org.mockito.ArgumentMatchers.<Wrapper<SysMenuEntity>>any()))
            .thenReturn(List.of(menu(10L, "system:user:list"), menu(11L, "system:role:list")));

        RoleInfo roleInfo = resolver.resolve(1L, true);

        assertEquals(RoleInfo.ADMIN_ROLE_ID, roleInfo.getRoleId());
        assertEquals(RoleInfo.ADMIN_ROLE_KEY, roleInfo.getRoleKey());
        assertTrue(roleInfo.getMenuPermissions().contains(RoleInfo.ALL_PERMISSIONS));
        assertEquals(Set.of(10L, 11L), roleInfo.getMenuIds());
    }

    @Test
    void testResolveShouldReturnRoleInfoForEnabledRole() {
        SysRoleEntity roleEntity = new SysRoleEntity();
        roleEntity.setRoleId(2L);
        roleEntity.setRoleKey("common");
        roleEntity.setDataScope(DataScopeEnum.CUSTOM_DEFINE.getValue());
        roleEntity.setDeptIdSet("1,2");
        roleEntity.setStatus(StatusEnum.ENABLE.getValue());

        when(roleService.getById(2L)).thenReturn(roleEntity);
        when(roleService.getMenuListByRoleId(2L)).thenReturn(List.of(
            menu(20L, "system:user:list"),
            menu(21L, ""),
            menu(22L, "system:user:edit")
        ));

        RoleInfo roleInfo = resolver.resolve(2L, false);

        assertEquals(2L, roleInfo.getRoleId());
        assertEquals("common", roleInfo.getRoleKey());
        assertEquals(DataScopeEnum.CUSTOM_DEFINE, roleInfo.getDataScope());
        assertEquals(Set.of(1L, 2L), roleInfo.getDeptIdSet());
        assertEquals(Set.of(20L, 21L, 22L), roleInfo.getMenuIds());
        assertEquals(Set.of("system:user:list", "system:user:edit"), roleInfo.getMenuPermissions());
    }

    @Test
    void testResolveShouldReturnEmptyRoleWhenRoleDisabled() {
        SysRoleEntity roleEntity = new SysRoleEntity();
        roleEntity.setRoleId(3L);
        roleEntity.setStatus(StatusEnum.DISABLE.getValue());
        when(roleService.getById(3L)).thenReturn(roleEntity);

        RoleInfo roleInfo = resolver.resolve(3L, false);

        assertSame(RoleInfo.EMPTY_ROLE, roleInfo);
        verify(roleService, never()).getMenuListByRoleId(anyLong());
    }

    private SysMenuEntity menu(Long menuId, String permission) {
        SysMenuEntity menu = new SysMenuEntity();
        menu.setMenuId(menuId);
        menu.setPermission(permission);
        return menu;
    }
}
