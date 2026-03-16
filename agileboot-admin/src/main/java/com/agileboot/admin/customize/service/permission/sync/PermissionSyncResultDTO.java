package com.agileboot.admin.customize.service.permission.sync;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 权限点同步结果。
 */
@Data
public class PermissionSyncResultDTO {

    private boolean dryRun;

    private boolean applied;

    private boolean hasBlockingIssues;

    private int protectedPermissionCount;

    private int scannedEndpointCount;

    private List<PermissionSyncItemDTO> created = new ArrayList<>();

    private List<PermissionSyncItemDTO> updated = new ArrayList<>();

    private List<PermissionSyncItemDTO> skipped = new ArrayList<>();

    private List<PermissionSyncItemDTO> unresolved = new ArrayList<>();

    private List<PermissionSyncItemDTO> conflicts = new ArrayList<>();

    private List<PermissionSyncItemDTO> unprotected = new ArrayList<>();

    private List<PermissionSyncItemDTO> stale = new ArrayList<>();

}
