package com.factorylink.integrationTest.db;

import cn.hutool.core.collection.CollUtil;
import com.factorylink.integrationTest.IntegrationTestApplication;
import com.factorylink.domain.system.menu.db.SysMenuEntity;
import com.factorylink.domain.system.menu.db.SysMenuService;
import java.util.List;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = IntegrationTestApplication.class)
@RunWith(SpringRunner.class)
class SysMenuServiceImplTest {

    @Resource
    SysMenuService menuService;

    @Test
    @Rollback
    void testGetMenuListByUserId() {
        List<SysMenuEntity> menusMissingLastMenu = menuService.getMenuListByUserId(2L);
        List<SysMenuEntity> allMenus = menuService.list();

        Assertions.assertEquals(allMenus.size(), menusMissingLastMenu.size() + 1);
    }

    @Test
    @Rollback
    void testGetMenuIdsByRoleId() {
        List<Long> menusMissingLastMenu = menuService.getMenuIdsByRoleId(2L);
        List<SysMenuEntity> allMenus = menuService.list();

        Assertions.assertEquals(allMenus.size(), menusMissingLastMenu.size() + 1);
    }

    @Test
    @Rollback
    void testIsMenuNameDuplicated() {
        boolean addWithSame = menuService.isMenuNameDuplicated("用户管理", null, 1L);
        boolean updateWithSame = menuService.isMenuNameDuplicated("用户管理", 5L, 1L);
        boolean addWithoutSame = menuService.isMenuNameDuplicated("用户管理", null, 2L);

        Assertions.assertTrue(addWithSame);
        Assertions.assertFalse(updateWithSame);
        Assertions.assertFalse(addWithoutSame);
    }

    @Test
    @Rollback
    void testHasChildrenMenus() {
        boolean hasChildrenMenu = menuService.hasChildrenMenu(5L);
        boolean hasNotChildrenMenu = menuService.hasChildrenMenu(20L);

        Assertions.assertTrue(hasChildrenMenu);
        Assertions.assertFalse(hasNotChildrenMenu);
    }

    @Test
    @Rollback
    void testIsMenuAssignToRole() {
        List<SysMenuEntity> allMenus = menuService.list();
        List<Long> roleMenuIds = menuService.getMenuIdsByRoleId(2L);

        boolean isAssignToRole = menuService.isMenuAssignToRoles(CollUtil.getFirst(allMenus).getMenuId());
        Long unassignedMenuId = allMenus.stream()
            .map(SysMenuEntity::getMenuId)
            .filter(menuId -> !roleMenuIds.contains(menuId))
            .findFirst()
            .orElseThrow();
        boolean isNotAssignToRole = menuService.isMenuAssignToRoles(unassignedMenuId);

        Assertions.assertFalse(isNotAssignToRole);
        Assertions.assertTrue(isAssignToRole);
    }



}
