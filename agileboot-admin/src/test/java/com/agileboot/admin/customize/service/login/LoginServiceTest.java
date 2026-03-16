package com.agileboot.admin.customize.service.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.admin.customize.service.login.command.LoginCommand;
import com.agileboot.common.enums.common.ConfigKeyEnum;
import com.agileboot.domain.common.cache.GuavaCacheService;
import com.agileboot.domain.common.cache.RedisCacheService;
import com.agileboot.domain.system.config.db.SysConfigService;
import com.agileboot.domain.system.dept.db.SysDeptService;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class LoginServiceTest {

    private final TokenService tokenService = mock(TokenService.class);
    private final RedisCacheService redisCache = mock(RedisCacheService.class);
    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final SysConfigService configService = mock(SysConfigService.class);
    private final SysDeptService deptService = mock(SysDeptService.class);
    private final GuavaCacheService guavaCache = new GuavaCacheService(configService, deptService);
    private final LoginService loginService =
        new TestLoginService(tokenService, redisCache, guavaCache, authenticationManager);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testLoginShouldAuthenticateWithPlainPassword() {
        when(configService.getConfigValueByKey(ConfigKeyEnum.CAPTCHA.getValue())).thenReturn("false");
        SystemLoginUser loginUser = mock(SystemLoginUser.class);
        Authentication authenticationResult = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticationResult);
        when(authenticationResult.getPrincipal()).thenReturn(loginUser);
        when(tokenService.createTokenAndPutUserInCache(loginUser)).thenReturn("token");

        LoginCommand loginCommand = new LoginCommand();
        loginCommand.setUsername("admin");
        loginCommand.setPassword("admin123");

        String token = loginService.login(loginCommand);

        ArgumentCaptor<Authentication> authenticationCaptor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(authenticationCaptor.capture());
        Authentication authenticationRequest = authenticationCaptor.getValue();

        assertInstanceOf(UsernamePasswordAuthenticationToken.class, authenticationRequest);
        assertEquals("admin", authenticationRequest.getPrincipal());
        assertEquals("admin123", authenticationRequest.getCredentials());
        assertEquals("token", token);
    }

    private static final class TestLoginService extends LoginService {

        private TestLoginService(TokenService tokenService, RedisCacheService redisCache, GuavaCacheService guavaCache,
            AuthenticationManager authenticationManager) {
            super(tokenService, redisCache, guavaCache, authenticationManager);
        }

        @Override
        public void recordLoginInfo(SystemLoginUser loginUser) {
            // Skip redis-backed login record updates in the unit test.
        }
    }

}
