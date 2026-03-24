package com.factorylink.admin.customize.service.permission;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 数据权限校验服务
 *
 * 当前为小型项目，暂不启用基于部门的数据权限控制，所有检查直接放行。
 * 功能权限（@permission.has）仍然生效。
 * 如需恢复数据权限，可参考 git 历史记录还原此类的实现。
 *
 * @author valarchie
 */
@Service("dataScope")
@RequiredArgsConstructor
public class DataPermissionService {

    public boolean checkUserId(Long userId) {
        return true;
    }

    public boolean checkUserIds(List<Long> userIds) {
        return true;
    }

    public boolean checkDeptId(Long deptId) {
        return true;
    }

}
