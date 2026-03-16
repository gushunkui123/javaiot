package com.agileboot.admin.customize.service.permission.sync;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条权限同步结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionSyncItemDTO {

    public static final Comparator<PermissionSyncItemDTO> DEFAULT_COMPARATOR = Comparator
        .comparing((PermissionSyncItemDTO i) -> StrUtil.blankToDefault(i.getPermission(), ""))
        .thenComparing(i -> StrUtil.blankToDefault(i.getMenuName(), ""))
        .thenComparing(i -> StrUtil.blankToDefault(i.getParentPermission(), ""))
        .thenComparing(i -> CollUtil.isEmpty(i.getHandlers()) ? "" : CollUtil.getFirst(i.getHandlers()));

    private Long menuId;

    private Long parentMenuId;

    private String permission;

    private String parentPermission;

    private String menuName;

    @Builder.Default
    private List<String> requestMethods = new ArrayList<>();

    @Builder.Default
    private List<String> requestPaths = new ArrayList<>();

    @Builder.Default
    private List<String> handlers = new ArrayList<>();

    private String details;

}
