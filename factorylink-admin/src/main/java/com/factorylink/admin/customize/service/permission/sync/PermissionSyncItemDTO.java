package com.factorylink.admin.customize.service.permission.sync;

import cn.hutool.core.util.StrUtil;
import java.util.Comparator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条权限同步明细。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionSyncItemDTO {

    public static final Comparator<PermissionSyncItemDTO> DEFAULT_COMPARATOR =
        Comparator.comparing((PermissionSyncItemDTO i) -> StrUtil.blankToDefault(i.getPermission(), ""))
            .thenComparing(i -> StrUtil.blankToDefault(i.getMenuName(), ""));

    /** 权限标识 */
    private String permission;

    /** 菜单名称 */
    private String menuName;

    /** 父权限标识 */
    private String parentPermission;

    /** 说明 */
    private String details;

}
