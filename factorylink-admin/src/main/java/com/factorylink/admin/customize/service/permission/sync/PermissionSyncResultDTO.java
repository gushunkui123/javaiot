package com.factorylink.admin.customize.service.permission.sync;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 权限同步结果。
 */
@Data
public class PermissionSyncResultDTO {

    /** 代码中扫描到的唯一权限标识数 */
    private int scannedCount;

    /** 数据库中已有的权限标识数 */
    private int existingCount;

    /** false=预览模式，true=已执行写入 */
    private boolean applied;

    /** 本次新增/待新增的按钮权限 */
    private List<PermissionSyncItemDTO> added = new ArrayList<>();

    /** 跳过的权限（未找到父节点等） */
    private List<PermissionSyncItemDTO> skipped = new ArrayList<>();

}
