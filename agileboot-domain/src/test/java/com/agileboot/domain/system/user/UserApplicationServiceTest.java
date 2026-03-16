package com.agileboot.domain.system.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.domain.common.cache.CacheCenter;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.system.post.db.SysPostService;
import com.agileboot.domain.system.role.db.SysRoleService;
import com.agileboot.domain.system.user.db.SysUserEntity;
import com.agileboot.domain.system.user.db.SysUserService;
import com.agileboot.domain.system.user.model.UserModel;
import com.agileboot.domain.system.user.model.UserModelFactory;
import com.agileboot.infrastructure.cache.redis.RedisCacheTemplate;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserApplicationServiceTest {

    private final SysUserService userService = mock(SysUserService.class);
    private final SysRoleService roleService = mock(SysRoleService.class);
    private final SysPostService postService = mock(SysPostService.class);
    private final UserModelFactory userModelFactory = mock(UserModelFactory.class);
    private final UserApplicationService applicationService =
        new UserApplicationService(userService, roleService, postService, userModelFactory);

    @SuppressWarnings("unchecked")
    private final RedisCacheTemplate<SysUserEntity> userCache = mock(RedisCacheTemplate.class);

    private RedisCacheTemplate<SysUserEntity> originalUserCache;

    @BeforeEach
    void setUp() {
        originalUserCache = CacheCenter.userCache;
        CacheCenter.userCache = userCache;
    }

    @AfterEach
    void tearDown() {
        CacheCenter.userCache = originalUserCache;
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
        verify(userModel2).checkCanBeDelete(loginUser);
        verify(userModel2).deleteById();
        verify(userCache).delete(2L);
    }
}
