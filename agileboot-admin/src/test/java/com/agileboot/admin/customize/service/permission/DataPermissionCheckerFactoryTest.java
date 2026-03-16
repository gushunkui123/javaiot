package com.agileboot.admin.customize.service.permission;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.agileboot.admin.customize.service.permission.model.AbstractDataPermissionChecker;
import com.agileboot.admin.customize.service.permission.model.checker.DefaultDataPermissionChecker;
import com.agileboot.infrastructure.user.web.RoleInfo;
import com.agileboot.infrastructure.user.web.SystemLoginUser;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DataPermissionCheckerFactoryTest {

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        setStaticField("defaultSelfChecker", new DefaultDataPermissionChecker());
    }

    @Test
    void testGetCheckerShouldFallbackToDefaultWhenLoginUserIsNull() {
        AbstractDataPermissionChecker checker = DataPermissionCheckerFactory.getChecker(null);

        assertInstanceOf(DefaultDataPermissionChecker.class, checker);
    }

    @Test
    void testGetCheckerShouldFallbackToDefaultWhenRoleInfoHasNoDataScope() {
        SystemLoginUser loginUser = new SystemLoginUser();
        loginUser.setRoleInfo(RoleInfo.EMPTY_ROLE);

        AbstractDataPermissionChecker checker = DataPermissionCheckerFactory.getChecker(loginUser);

        assertInstanceOf(DefaultDataPermissionChecker.class, checker);
    }

    private void setStaticField(String fieldName, Object value) throws ReflectiveOperationException {
        Field field = DataPermissionCheckerFactory.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }
}
