package com.factorylink.domain.system.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.common.cache.CacheCenter;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.system.post.db.SysPostService;
import com.factorylink.domain.system.role.db.SysRoleEntity;
import com.factorylink.domain.system.role.db.SysRoleService;
import com.factorylink.domain.system.user.db.SysUserEntity;
import com.factorylink.domain.system.user.command.UpdateUserCommand;
import com.factorylink.domain.system.user.db.SysUserService;
import com.factorylink.domain.system.user.dto.UserAccountSummaryDTO;
import com.factorylink.domain.system.user.model.UserModel;
import com.factorylink.domain.system.user.model.UserModelFactory;
import com.factorylink.infrastructure.cache.redis.RedisCacheTemplate;
import com.factorylink.infrastructure.user.web.SystemLoginUser;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserApplicationServiceTest {

    private final SysUserService userService = mock(SysUserService.class);
    private final SysRoleService roleService = mock(SysRoleService.class);
    private final SysPostService postService = mock(SysPostService.class);
    private final UserModelFactory userModelFactory = mock(UserModelFactory.class);
    private final AuditUserEnricher auditUserEnricher = mock(AuditUserEnricher.class);
    private final UserApplicationService applicationService =
        new UserApplicationService(userService, roleService, postService, userModelFactory, auditUserEnricher);

    @SuppressWarnings("unchecked")
    private final RedisCacheTemplate<SysUserEntity> userCache = mock(RedisCacheTemplate.class);
    @SuppressWarnings("unchecked")
    private final RedisCacheTemplate<String> usernameCache = mock(RedisCacheTemplate.class);
    @SuppressWarnings("unchecked")
    private final RedisCacheTemplate<SysRoleEntity> roleCache = mock(RedisCacheTemplate.class);

    private RedisCacheTemplate<SysUserEntity> originalUserCache;
    private RedisCacheTemplate<String> originalUsernameCache;
    private RedisCacheTemplate<SysRoleEntity> originalRoleCache;

    @BeforeEach
    void setUp() {
        originalUserCache = CacheCenter.userCache;
        originalUsernameCache = CacheCenter.usernameCache;
        originalRoleCache = CacheCenter.roleCache;
        CacheCenter.userCache = userCache;
        CacheCenter.usernameCache = usernameCache;
        CacheCenter.roleCache = roleCache;
    }

    @AfterEach
    void tearDown() {
        CacheCenter.userCache = originalUserCache;
        CacheCenter.usernameCache = originalUsernameCache;
        CacheCenter.roleCache = originalRoleCache;
    }

    @Test
    void deleteUsersShouldRemoveUserCacheForEachDeletedUser() {
        UserModel userModel1 = mock(UserModel.class);
        UserModel userModel2 = mock(UserModel.class);
        when(userModelFactory.loadById(1L)).thenReturn(userModel1);
        when(userModelFactory.loadById(2L)).thenReturn(userModel2);
        when(userModel1.getUserId()).thenReturn(1L);
        when(userModel2.getUserId()).thenReturn(2L);
        SystemLoginUser loginUser = mock(SystemLoginUser.class);

        applicationService.deleteUsers(loginUser, new BulkOperationCommand<>(List.of(1L, 2L)));

        verify(userModel1).checkCanBeDelete(loginUser);
        verify(userModel1).deleteById();
        verify(userCache).delete(1L);
        verify(usernameCache).delete(1L);
        verify(userModel2).checkCanBeDelete(loginUser);
        verify(userModel2).deleteById();
        verify(userCache).delete(2L);
        verify(usernameCache).delete(2L);
    }

    @Test
    void updateUserShouldRemoveUserAndUsernameCache() {
        UpdateUserCommand command = new UpdateUserCommand();
        command.setUserId(8L);
        UserModel userModel = mock(UserModel.class);
        when(userModelFactory.loadById(8L)).thenReturn(userModel);
        when(userModel.getUserId()).thenReturn(8L);

        applicationService.updateUser(command);

        verify(userModel).loadUpdateUserCommand(command);
        verify(userModel).checkPhoneNumberIsUnique();
        verify(userModel).checkEmailIsUnique();
        verify(userModel).checkFieldRelatedEntityExist();
        verify(userModel).updateById();
        verify(userCache).delete(8L);
        verify(usernameCache).delete(8L);
    }

    @Test
    void getUserAccountSummaryListShouldMapRoleNameAndEnabledStatus() {
        SysRoleEntity adminRole = new SysRoleEntity();
        adminRole.setRoleName("管理员");
        when(roleCache.getObjectById(10L)).thenReturn(adminRole);

        SysRoleEntity operatorRole = new SysRoleEntity();
        operatorRole.setRoleName("操作员");
        when(roleCache.getObjectById(11L)).thenReturn(operatorRole);

        SysUserEntity enabledUser = new SysUserEntity();
        enabledUser.setUsername("alice");
        enabledUser.setRoleId(10L);
        enabledUser.setEmail("alice@example.com");
        enabledUser.setStatus(1);

        SysUserEntity disabledUser = new SysUserEntity();
        disabledUser.setUsername("bob");
        disabledUser.setRoleId(11L);
        disabledUser.setEmail("bob@example.com");
        disabledUser.setStatus(2);

        when(userService.listUserAccountSummary()).thenReturn(List.of(enabledUser, disabledUser));

        List<UserAccountSummaryDTO> accountSummaryList = applicationService.getUserAccountSummaryList();

        assertEquals(2, accountSummaryList.size());
        assertEquals("alice", accountSummaryList.get(0).getUsername());
        assertEquals("管理员", accountSummaryList.get(0).getRoleName());
        assertEquals("alice@example.com", accountSummaryList.get(0).getEmail());
        assertTrue(accountSummaryList.get(0).getEnabled());
        assertEquals("bob", accountSummaryList.get(1).getUsername());
        assertEquals("操作员", accountSummaryList.get(1).getRoleName());
        assertEquals("bob@example.com", accountSummaryList.get(1).getEmail());
        assertFalse(accountSummaryList.get(1).getEnabled());
    }
}
