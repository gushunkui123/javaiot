package com.factorylink.admin.customize.service.permission.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factorylink.domain.system.menu.db.SysMenuEntity;
import com.factorylink.domain.system.menu.db.SysMenuService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PermissionSyncServiceTest {

    private final PermissionEndpointScanner scanner = mock(PermissionEndpointScanner.class);
    private final SysMenuService menuService = mock(SysMenuService.class);
    private final PermissionSyncService permissionSyncService = new PermissionSyncService(scanner, menuService);

    @Test
    void testPreviewShouldReturnAddedAndSkipped() {
        Map<String, String> scanned = new LinkedHashMap<>();
        scanned.put("system:user:list", "用户列表");
        scanned.put("system:user:add", "新增用户");
        scanned.put("system:user:edit", "修改用户");
        scanned.put("system:dept:remove", "删除部门");
        scanned.put("system:post:query", "岗位详情");
        when(scanner.scan()).thenReturn(scanned);

        when(menuService.list()).thenReturn(List.of(
            pageMenu(5L, 1L, "用户管理", "system:user:list"),
            buttonMenu(22L, 5L, "修改用户", "system:user:edit"),
            pageMenu(8L, 1L, "部门管理", "system:dept:list")
        ));

        PermissionSyncResultDTO result = permissionSyncService.preview();

        assertFalse(result.isApplied());
        assertEquals(5, result.getScannedCount());
        assertEquals(3, result.getExistingCount());

        // system:user:add 有父节点 system:user:list → 新增
        // system:dept:remove 有父节点 system:dept:list → 新增
        assertEquals(2, result.getAdded().size());

        // system:post:query 的父节点 system:post:list 不存在 → 跳过
        assertEquals(1, result.getSkipped().size());
        assertEquals("system:post:query", result.getSkipped().get(0).getPermission());
        assertEquals("未找到父节点", result.getSkipped().get(0).getDetails());
    }

    @Test
    void testPreviewShouldNotWriteToDatabase() {
        Map<String, String> scanned = new LinkedHashMap<>();
        scanned.put("system:user:add", "新增用户");
        when(scanner.scan()).thenReturn(scanned);
        when(menuService.list()).thenReturn(List.of(
            pageMenu(5L, 1L, "用户管理", "system:user:list")
        ));

        permissionSyncService.preview();

        verify(menuService, never()).save(any(SysMenuEntity.class));
    }

    @Test
    void testSyncShouldSaveMissingPermissions() {
        Map<String, String> scanned = new LinkedHashMap<>();
        scanned.put("system:user:list", "用户列表");
        scanned.put("system:user:add", "新增用户");
        when(scanner.scan()).thenReturn(scanned);

        when(menuService.list()).thenReturn(List.of(
            pageMenu(5L, 1L, "用户管理", "system:user:list")
        ));

        PermissionSyncResultDTO result = permissionSyncService.sync();

        assertTrue(result.isApplied());
        assertEquals(1, result.getAdded().size());
        assertEquals("system:user:add", result.getAdded().get(0).getPermission());
        verify(menuService).save(any(SysMenuEntity.class));
    }

    @Test
    void testSyncShouldSkipWhenParentNotFound() {
        Map<String, String> scanned = new LinkedHashMap<>();
        scanned.put("system:post:query", "岗位详情");
        when(scanner.scan()).thenReturn(scanned);
        when(menuService.list()).thenReturn(List.of());

        PermissionSyncResultDTO result = permissionSyncService.sync();

        assertTrue(result.isApplied());
        assertEquals(0, result.getAdded().size());
        assertEquals(1, result.getSkipped().size());
        verify(menuService, never()).save(any(SysMenuEntity.class));
    }

    @Test
    void testSyncShouldSkipAlreadyExistingPermissions() {
        Map<String, String> scanned = new LinkedHashMap<>();
        scanned.put("system:user:list", "用户列表");
        scanned.put("system:user:edit", "修改用户");
        when(scanner.scan()).thenReturn(scanned);

        when(menuService.list()).thenReturn(List.of(
            pageMenu(5L, 1L, "用户管理", "system:user:list"),
            buttonMenu(22L, 5L, "修改用户", "system:user:edit")
        ));

        PermissionSyncResultDTO result = permissionSyncService.sync();

        assertTrue(result.isApplied());
        assertEquals(0, result.getAdded().size());
        assertEquals(0, result.getSkipped().size());
        verify(menuService, never()).save(any(SysMenuEntity.class));
    }

    private SysMenuEntity pageMenu(Long menuId, Long parentId, String menuName, String permission) {
        SysMenuEntity menu = new SysMenuEntity();
        menu.setMenuId(menuId);
        menu.setParentId(parentId);
        menu.setMenuName(menuName);
        menu.setPermission(permission);
        menu.setMenuType(1);
        menu.setIsButton(false);
        menu.setStatus(1);
        return menu;
    }

    private SysMenuEntity buttonMenu(Long menuId, Long parentId, String menuName, String permission) {
        SysMenuEntity menu = new SysMenuEntity();
        menu.setMenuId(menuId);
        menu.setParentId(parentId);
        menu.setMenuName(menuName);
        menu.setPermission(permission);
        menu.setMenuType(0);
        menu.setIsButton(true);
        menu.setStatus(1);
        return menu;
    }

}
