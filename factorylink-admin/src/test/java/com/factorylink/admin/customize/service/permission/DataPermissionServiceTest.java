package com.factorylink.admin.customize.service.permission;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class DataPermissionServiceTest {

    private final DataPermissionService dataPermissionService = new DataPermissionService();

    @Test
    void checkUserIdShouldAlwaysReturnTrue() {
        assertTrue(dataPermissionService.checkUserId(1L));
        assertTrue(dataPermissionService.checkUserId(null));
    }

    @Test
    void checkUserIdsShouldAlwaysReturnTrue() {
        assertTrue(dataPermissionService.checkUserIds(Arrays.asList(1L, 2L)));
        assertTrue(dataPermissionService.checkUserIds(Collections.emptyList()));
        assertTrue(dataPermissionService.checkUserIds(null));
    }

    @Test
    void checkDeptIdShouldAlwaysReturnTrue() {
        assertTrue(dataPermissionService.checkDeptId(1L));
        assertTrue(dataPermissionService.checkDeptId(null));
    }
}
