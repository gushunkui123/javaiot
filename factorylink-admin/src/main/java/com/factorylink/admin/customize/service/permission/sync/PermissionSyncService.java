package com.factorylink.admin.customize.service.permission.sync;

import cn.hutool.core.util.StrUtil;
import com.factorylink.common.utils.jackson.JacksonUtil;
import com.factorylink.domain.system.menu.db.SysMenuEntity;
import com.factorylink.domain.system.menu.db.SysMenuService;
import com.factorylink.domain.system.menu.dto.MetaDTO;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 权限标识同步服务：扫描代码注解 → 比对数据库 → 补齐缺失的按钮权限。
 */
@Service
@RequiredArgsConstructor
public class PermissionSyncService {

    private final PermissionEndpointScanner permissionEndpointScanner;
    private final SysMenuService menuService;

    /**
     * 预览：只比对，不写入。
     */
    public PermissionSyncResultDTO preview() {
        return doBuildResult(false);
    }

    /**
     * 同步：比对并写入缺失的权限。
     */
    @Transactional(rollbackFor = Exception.class)
    public PermissionSyncResultDTO sync() {
        return doBuildResult(true);
    }

    private PermissionSyncResultDTO doBuildResult(boolean apply) {
        // 1. 扫描代码中的权限标识 (permission -> menuName)
        Map<String, String> scannedPermissions = permissionEndpointScanner.scan();

        // 2. 查询数据库中已有的权限标识
        Set<String> existingPermissions = menuService.list().stream()
            .map(SysMenuEntity::getPermission)
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toSet());

        // 3. 构建以权限标识为 key 的菜单索引（用于查找父节点）
        Map<String, SysMenuEntity> menuByPermission = menuService.list().stream()
            .filter(m -> StrUtil.isNotBlank(m.getPermission()))
            .collect(Collectors.toMap(SysMenuEntity::getPermission, m -> m, (a, b) -> a));

        PermissionSyncResultDTO result = new PermissionSyncResultDTO();
        result.setScannedCount(scannedPermissions.size());
        result.setExistingCount(existingPermissions.size());

        // 4. 找出缺失的权限标识，逐个处理
        for (Map.Entry<String, String> entry : scannedPermissions.entrySet()) {
            String permission = entry.getKey();
            String menuName = entry.getValue();

            if (existingPermissions.contains(permission)) {
                continue;
            }

            String parentPermission = deriveParentPermission(permission);
            SysMenuEntity parentMenu = menuByPermission.get(parentPermission);

            // 父节点不存在或父节点是按钮 → 无法挂载
            if (parentMenu == null || Boolean.TRUE.equals(parentMenu.getIsButton())) {
                result.getSkipped().add(PermissionSyncItemDTO.builder()
                    .permission(permission)
                    .menuName(menuName)
                    .parentPermission(parentPermission)
                    .details("未找到父节点")
                    .build());
                continue;
            }

            // 创建按钮权限菜单
            PermissionSyncItemDTO item = PermissionSyncItemDTO.builder()
                .permission(permission)
                .menuName(menuName)
                .parentPermission(parentPermission)
                .details("新增按钮权限")
                .build();

            if (apply) {
                SysMenuEntity menu = buildButtonMenu(permission, menuName, parentMenu.getMenuId());
                menuService.save(menu);
            }

            result.getAdded().add(item);
        }

        result.setApplied(apply);

        // 5. 排序
        result.setAdded(result.getAdded().stream()
            .sorted(PermissionSyncItemDTO.DEFAULT_COMPARATOR).toList());
        result.setSkipped(result.getSkipped().stream()
            .sorted(PermissionSyncItemDTO.DEFAULT_COMPARATOR).toList());

        return result;
    }

    /**
     * 根据命名约定推导父权限标识。
     * 例如 system:menu:add → system:menu:list
     */
    private String deriveParentPermission(String permission) {
        int lastColon = permission.lastIndexOf(':');
        if (lastColon < 0) {
            return permission;
        }
        return permission.substring(0, lastColon) + ":list";
    }

    private SysMenuEntity buildButtonMenu(String permission, String menuName, Long parentId) {
        SysMenuEntity menu = new SysMenuEntity();
        menu.setParentId(parentId);
        menu.setMenuName(menuName);
        menu.setMenuType(0);
        menu.setRouterName(" ");
        menu.setPath("");
        menu.setIsButton(true);
        menu.setPermission(permission);
        menu.setStatus(1);
        menu.setMetaInfo(buildMetaInfo(menuName));
        return menu;
    }

    private String buildMetaInfo(String menuName) {
        MetaDTO metaDTO = new MetaDTO();
        metaDTO.setTitle(menuName);
        return JacksonUtil.to(metaDTO);
    }

}
