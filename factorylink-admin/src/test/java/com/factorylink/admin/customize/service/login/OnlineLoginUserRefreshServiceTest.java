package com.factorylink.admin.customize.service.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factorylink.domain.common.cache.RedisCacheService;
import com.factorylink.domain.system.role.db.SysRoleMenuEntity;
import com.factorylink.domain.system.role.db.SysRoleMenuService;
import com.factorylink.domain.system.user.db.SysUserEntity;
import com.factorylink.domain.system.user.db.SysUserService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.factorylink.infrastructure.cache.redis.RedisCacheTemplate;
import com.factorylink.infrastructure.user.web.DataScopeEnum;
import com.factorylink.infrastructure.user.web.RoleInfo;
import com.factorylink.infrastructure.user.web.SystemLoginUser;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

class OnlineLoginUserRefreshServiceTest {

    @SuppressWarnings("unchecked")
    private final RedisTemplate<String, ?> redisTemplate = mock(RedisTemplate.class);
    private final RedisCacheService redisCache = mock(RedisCacheService.class);
    @SuppressWarnings("unchecked")
    private final RedisCacheTemplate<SystemLoginUser> loginUserCache = mock(RedisCacheTemplate.class);
    private final SysUserService userService = mock(SysUserService.class);
    private final SysRoleMenuService roleMenuService = mock(SysRoleMenuService.class);
    private final RoleInfoResolver roleInfoResolver = mock(RoleInfoResolver.class);
    private final OnlineLoginUserRefreshService refreshService =
        new OnlineLoginUserRefreshService(redisTemplate, redisCache, userService, roleMenuService, roleInfoResolver);

    OnlineLoginUserRefreshServiceTest() {
        redisCache.loginUserCache = loginUserCache;
    }

    @Test
    void testRefreshByUserIdsShouldRewriteMatchedSession() {
        SystemLoginUser loginUser = loginUser(1L, 2L, "old-user");
        RoleInfo refreshedRoleInfo = new RoleInfo(3L, "new-role", DataScopeEnum.ALL, Set.of(), Set.of("system:user:edit"),
            Set.of(100L));
        SysUserEntity latestUser = userEntity(1L, 3L, 8L, "new-user", "new-password", false);

        mockScan("login_tokens:token-1");
        when(loginUserCache.getObjectOnlyInCacheByKey("login_tokens:token-1")).thenReturn(loginUser);
        when(userService.getById(1L)).thenReturn(latestUser);
        when(roleInfoResolver.resolve(3L, false)).thenReturn(refreshedRoleInfo);

        int refreshedCount = refreshService.refreshByUserIds(List.of(1L));

        assertEquals(1, refreshedCount);
        assertEquals("token-1", loginUser.getCachedKey());
        assertEquals("new-user", loginUser.getUsername());
        assertEquals("new-password", loginUser.getPassword());
        assertEquals(8L, loginUser.getDeptId());
        assertSame(refreshedRoleInfo, loginUser.getRoleInfo());
        verify(loginUserCache).setByKey("login_tokens:token-1", loginUser);
    }

    @Test
    void testRefreshByRoleIdsShouldOnlyRefreshMatchingSessions() {
        SystemLoginUser matchedLoginUser = loginUser(1L, 10L, "match");
        SystemLoginUser untouchedLoginUser = loginUser(2L, 20L, "skip");
        RoleInfo refreshedRoleInfo = new RoleInfo(10L, "role-10", DataScopeEnum.ALL, Set.of(), Set.of("a:b:c"), Set.of(1L));

        mockScan("login_tokens:token-1", "login_tokens:token-2");
        when(loginUserCache.getObjectOnlyInCacheByKey("login_tokens:token-1")).thenReturn(matchedLoginUser);
        when(loginUserCache.getObjectOnlyInCacheByKey("login_tokens:token-2")).thenReturn(untouchedLoginUser);
        when(userService.getById(1L)).thenReturn(userEntity(1L, 10L, 5L, "match-new", "pwd-1", false));
        when(roleInfoResolver.resolve(10L, false)).thenReturn(refreshedRoleInfo);

        int refreshedCount = refreshService.refreshByRoleIds(List.of(10L));

        assertEquals(1, refreshedCount);
        verify(loginUserCache).setByKey("login_tokens:token-1", matchedLoginUser);
        verify(loginUserCache, never()).setByKey(eq("login_tokens:token-2"), any());
    }

    @Test
    void testRefreshByMenuIdsShouldResolveRoleIdsBeforeRefreshingSessions() {
        SystemLoginUser loginUser = loginUser(3L, 30L, "menu-user");
        RoleInfo refreshedRoleInfo = new RoleInfo(30L, "menu-role", DataScopeEnum.DEPT_TREE, Set.of(), Set.of("x:y:z"),
            Set.of(99L));
        SysRoleMenuEntity roleMenu = new SysRoleMenuEntity();
        roleMenu.setRoleId(30L);
        roleMenu.setMenuId(99L);

        when(roleMenuService.list(org.mockito.ArgumentMatchers.<Wrapper<SysRoleMenuEntity>>any()))
            .thenReturn(List.of(roleMenu));
        mockScan("login_tokens:token-3");
        when(loginUserCache.getObjectOnlyInCacheByKey("login_tokens:token-3")).thenReturn(loginUser);
        when(userService.getById(3L)).thenReturn(userEntity(3L, 30L, 6L, "menu-user-new", "pwd-3", false));
        when(roleInfoResolver.resolve(30L, false)).thenReturn(refreshedRoleInfo);

        int refreshedCount = refreshService.refreshByMenuIds(List.of(99L));

        assertEquals(1, refreshedCount);
        verify(loginUserCache).setByKey("login_tokens:token-3", loginUser);
    }

    @SuppressWarnings("unchecked")
    private void mockScan(String... keys) {
        Cursor<String> cursor = mock(Cursor.class);
        Iterator<String> iterator = List.of(keys).iterator();
        when(cursor.hasNext()).thenAnswer(inv -> iterator.hasNext());
        when(cursor.next()).thenAnswer(inv -> iterator.next());
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);
    }

    private SystemLoginUser loginUser(Long userId, Long roleId, String username) {
        RoleInfo roleInfo = new RoleInfo(roleId, "role-" + roleId, DataScopeEnum.ONLY_SELF, Set.of(), Set.of("old:perm"),
            Set.of(1L));
        SystemLoginUser loginUser = new SystemLoginUser(userId, false, username, "old-password", roleInfo, 1L);
        loginUser.setCachedKey("token-" + userId);
        return loginUser;
    }

    private SysUserEntity userEntity(Long userId, Long roleId, Long deptId, String username, String password, boolean isAdmin) {
        SysUserEntity userEntity = new SysUserEntity();
        userEntity.setUserId(userId);
        userEntity.setRoleId(roleId);
        userEntity.setDeptId(deptId);
        userEntity.setUsername(username);
        userEntity.setPassword(password);
        userEntity.setIsAdmin(isAdmin);
        return userEntity;
    }
}
