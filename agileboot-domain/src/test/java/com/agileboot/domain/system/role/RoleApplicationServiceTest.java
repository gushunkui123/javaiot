package com.agileboot.domain.system.role;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.domain.common.cache.CacheCenter;
import com.agileboot.domain.system.menu.db.SysMenuService;
import com.agileboot.domain.system.role.command.UpdateDataScopeCommand;
import com.agileboot.domain.system.role.command.UpdateRoleCommand;
import com.agileboot.domain.system.role.command.UpdateStatusCommand;
import com.agileboot.domain.system.role.db.SysRoleEntity;
import com.agileboot.domain.system.role.db.SysRoleService;
import com.agileboot.domain.system.role.model.RoleModel;
import com.agileboot.domain.system.role.model.RoleModelFactory;
import com.agileboot.domain.system.user.db.SysUserService;
import com.agileboot.domain.system.user.model.UserModelFactory;
import com.agileboot.infrastructure.cache.redis.RedisCacheTemplate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RoleApplicationServiceTest {

    private final RoleModelFactory roleModelFactory = mock(RoleModelFactory.class);
    private final UserModelFactory userModelFactory = mock(UserModelFactory.class);
    private final SysRoleService roleService = mock(SysRoleService.class);
    private final SysUserService userService = mock(SysUserService.class);
    private final SysMenuService menuService = mock(SysMenuService.class);
    private final RoleApplicationService applicationService =
        new RoleApplicationService(roleModelFactory, userModelFactory, roleService, userService, menuService);

    @SuppressWarnings("unchecked")
    private final RedisCacheTemplate<SysRoleEntity> roleCache = mock(RedisCacheTemplate.class);

    private RedisCacheTemplate<SysRoleEntity> originalRoleCache;

    @BeforeEach
    void setUp() {
        originalRoleCache = CacheCenter.roleCache;
        CacheCenter.roleCache = roleCache;
    }

    @AfterEach
    void tearDown() {
        CacheCenter.roleCache = originalRoleCache;
    }

    @Test
    void updateRoleShouldInvalidateRoleCache() {
        UpdateRoleCommand command = new UpdateRoleCommand();
        command.setRoleId(7L);
        RoleModel roleModel = mock(RoleModel.class);
        when(roleModelFactory.loadById(7L)).thenReturn(roleModel);
        when(roleModel.getRoleId()).thenReturn(7L);

        applicationService.updateRole(command);

        verify(roleModel).loadUpdateCommand(command);
        verify(roleModel).checkRoleKeyUnique();
        verify(roleModel).checkRoleNameUnique();
        verify(roleModel).updateById();
        verify(roleCache).delete(7L);
    }

    @Test
    void updateStatusShouldInvalidateRoleCache() {
        UpdateStatusCommand command = new UpdateStatusCommand();
        command.setRoleId(9L);
        command.setStatus(1);
        RoleModel roleModel = mock(RoleModel.class);
        when(roleModelFactory.loadById(9L)).thenReturn(roleModel);
        when(roleModel.getRoleId()).thenReturn(9L);

        applicationService.updateStatus(command);

        verify(roleModel).setStatus(1);
        verify(roleModel).updateById();
        verify(roleCache).delete(9L);
    }

    @Test
    void updateDataScopeShouldInvalidateRoleCache() {
        UpdateDataScopeCommand command = new UpdateDataScopeCommand();
        command.setRoleId(11L);
        command.setDeptIds(List.of(1L, 2L));
        command.setDataScope(2);
        RoleModel roleModel = mock(RoleModel.class);
        when(roleModelFactory.loadById(11L)).thenReturn(roleModel);
        when(roleModel.getRoleId()).thenReturn(11L);

        applicationService.updateDataScope(command);

        verify(roleModel).setDeptIds(List.of(1L, 2L));
        verify(roleModel).setDataScope(2);
        verify(roleModel).generateDeptIdSet();
        verify(roleModel).updateById();
        verify(roleCache).delete(11L);
    }

    @Test
    void deleteRoleByBulkShouldInvalidateDeletedRoleCaches() {
        RoleModel roleModel1 = mock(RoleModel.class);
        RoleModel roleModel2 = mock(RoleModel.class);
        when(roleModelFactory.loadById(1L)).thenReturn(roleModel1);
        when(roleModelFactory.loadById(2L)).thenReturn(roleModel2);
        when(roleModel1.getRoleId()).thenReturn(1L);
        when(roleModel2.getRoleId()).thenReturn(2L);

        applicationService.deleteRoleByBulk(List.of(1L, 2L));

        verify(roleModel1).checkRoleCanBeDelete();
        verify(roleModel1).deleteById();
        verify(roleCache).delete(1L);
        verify(roleModel2).checkRoleCanBeDelete();
        verify(roleModel2).deleteById();
        verify(roleCache).delete(2L);
    }
}
