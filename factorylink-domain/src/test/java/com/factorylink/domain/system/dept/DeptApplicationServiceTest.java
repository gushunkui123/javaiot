package com.factorylink.domain.system.dept;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factorylink.domain.common.cache.CacheCenter;
import com.factorylink.domain.system.dept.command.UpdateDeptCommand;
import com.factorylink.domain.system.dept.db.SysDeptEntity;
import com.factorylink.domain.system.dept.db.SysDeptService;
import com.factorylink.domain.system.dept.model.DeptModel;
import com.factorylink.domain.system.dept.model.DeptModelFactory;
import com.factorylink.domain.system.role.db.SysRoleService;
import com.factorylink.infrastructure.cache.guava.AbstractGuavaCacheTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeptApplicationServiceTest {

    private final SysDeptService deptService = mock(SysDeptService.class);
    private final SysRoleService roleService = mock(SysRoleService.class);
    private final DeptModelFactory deptModelFactory = mock(DeptModelFactory.class);
    private final DeptApplicationService applicationService =
        new DeptApplicationService(deptService, roleService, deptModelFactory);

    @SuppressWarnings("unchecked")
    private final AbstractGuavaCacheTemplate<SysDeptEntity> deptCache = mock(AbstractGuavaCacheTemplate.class);

    private AbstractGuavaCacheTemplate<SysDeptEntity> originalDeptCache;

    @BeforeEach
    void setUp() {
        originalDeptCache = CacheCenter.deptCache;
        CacheCenter.deptCache = deptCache;
    }

    @AfterEach
    void tearDown() {
        CacheCenter.deptCache = originalDeptCache;
    }

    @Test
    void updateDeptShouldInvalidateDeptCache() {
        UpdateDeptCommand command = new UpdateDeptCommand();
        command.setDeptId(5L);
        DeptModel deptModel = mock(DeptModel.class);
        when(deptModelFactory.loadById(5L)).thenReturn(deptModel);
        when(deptModel.getDeptId()).thenReturn(5L);

        applicationService.updateDept(command);

        verify(deptModel).loadUpdateCommand(command);
        verify(deptModel).checkDeptNameUnique();
        verify(deptModel).checkParentIdConflict();
        verify(deptModel).checkStatusAllowChange();
        verify(deptModel).generateAncestors();
        verify(deptModel).updateById();
        verify(deptCache).invalidate("5");
    }

    @Test
    void removeDeptShouldInvalidateDeptCache() {
        DeptModel deptModel = mock(DeptModel.class);
        when(deptModelFactory.loadById(8L)).thenReturn(deptModel);
        when(deptModel.getDeptId()).thenReturn(8L);

        applicationService.removeDept(8L);

        verify(deptModel).checkHasChildDept();
        verify(deptModel).checkDeptAssignedToUsers();
        verify(deptModel).deleteById();
        verify(deptCache).invalidate("8");
    }
}
