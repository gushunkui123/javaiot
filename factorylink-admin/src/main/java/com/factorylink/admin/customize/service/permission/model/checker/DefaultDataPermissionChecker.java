package com.factorylink.admin.customize.service.permission.model.checker;

import com.factorylink.infrastructure.user.web.SystemLoginUser;
import com.factorylink.admin.customize.service.permission.model.AbstractDataPermissionChecker;
import com.factorylink.admin.customize.service.permission.model.DataCondition;
import com.factorylink.domain.system.dept.db.SysDeptService;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据权限测试接口
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DefaultDataPermissionChecker extends AbstractDataPermissionChecker {

    private SysDeptService deptService;

    @Override
    public boolean check(SystemLoginUser loginUser, DataCondition condition) {
        return false;
    }

}
