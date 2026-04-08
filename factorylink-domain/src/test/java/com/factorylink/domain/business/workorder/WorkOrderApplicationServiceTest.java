package com.factorylink.domain.business.workorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.workorder.command.AddWorkOrderCommand;
import com.factorylink.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.factorylink.domain.business.workorder.db.BizWorkOrderService;
import com.factorylink.domain.business.workorder.dto.WorkOrderDTO;
import com.factorylink.domain.business.workorder.model.WorkOrderModel;
import com.factorylink.domain.business.workorder.model.WorkOrderModelFactory;
import com.factorylink.domain.business.workorder.query.WorkOrderQuery;
import com.factorylink.domain.business.formula.model.FormulaModelFactory;
import com.factorylink.domain.business.machine.ScaleSyncService;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.common.command.BulkOperationCommand;
import org.springframework.context.ApplicationEventPublisher;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorkOrderApplicationServiceTest {

    private final WorkOrderModelFactory workOrderModelFactory = mock(WorkOrderModelFactory.class);
    private final BizWorkOrderService workOrderService = mock(BizWorkOrderService.class);
    private final AuditUserEnricher auditUserEnricher = mock(AuditUserEnricher.class);
    private final ScaleSyncService scaleSyncService = mock(ScaleSyncService.class);
    private final FormulaModelFactory formulaModelFactory = mock(FormulaModelFactory.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final WorkOrderApplicationService applicationService =
        new WorkOrderApplicationService(
            workOrderModelFactory,
            workOrderService,
            auditUserEnricher,
            scaleSyncService,
            formulaModelFactory,
            applicationEventPublisher
        );

    @Test
    void getWorkOrderListShouldEnrichAuditUsers() {
        WorkOrderQuery query = mock(WorkOrderQuery.class);
        BizWorkOrderEntity entity = new BizWorkOrderEntity();
        entity.setWorkOrderId(1L);
        Page<BizWorkOrderEntity> page = new Page<>();
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(workOrderService.page(any(), any())).thenReturn(page);

        PageDTO<WorkOrderDTO> pageDTO = applicationService.getWorkOrderList(query);

        assertEquals(1, pageDTO.getRows().size());
        verify(auditUserEnricher).enrich(pageDTO.getRows());
    }

    @Test
    void getWorkOrderInfoShouldEnrichAuditUsers() {
        WorkOrderModel model = new WorkOrderModel();
        model.setWorkOrderId(9L);
        model.setWorkOrderNo("WO001");
        when(workOrderModelFactory.loadById(9L)).thenReturn(model);

        WorkOrderDTO dto = applicationService.getWorkOrderInfo(9L);

        assertEquals(9L, dto.getWorkOrderId());
        verify(auditUserEnricher).enrich(dto);
    }

    @Test
    void getWorkOrderListAllShouldReturnAllRecords() {
        WorkOrderQuery query = mock(WorkOrderQuery.class);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<BizWorkOrderEntity> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        when(query.toQueryWrapper()).thenReturn(wrapper);
        BizWorkOrderEntity entity1 = new BizWorkOrderEntity();
        entity1.setWorkOrderId(1L);
        BizWorkOrderEntity entity2 = new BizWorkOrderEntity();
        entity2.setWorkOrderId(2L);
        when(workOrderService.list(wrapper)).thenReturn(List.of(entity1, entity2));

        List<WorkOrderDTO> result = applicationService.getWorkOrderListAll(query);

        assertEquals(2, result.size());
        verify(auditUserEnricher).enrich(result);
    }

    @Test
    void addWorkOrderShouldValidateAndInsert() {
        AddWorkOrderCommand command = new AddWorkOrderCommand();
        command.setWorkOrderNo("WO010");
        command.setFormulaCode("F001");
        command.setOrderDate(new Date());
        command.setOrderBatchNum(10);

        WorkOrderModel model = mock(WorkOrderModel.class);
        when(workOrderModelFactory.create()).thenReturn(model);

        applicationService.addWorkOrder(command);

        verify(model).loadFromAddCommand(command);
        verify(model).checkWorkOrderNoUnique();
        verify(model).insert();
    }

    @Test
    void updateWorkOrderShouldLoadAndPersist() {
        UpdateWorkOrderCommand command = new UpdateWorkOrderCommand();
        command.setWorkOrderId(5L);
        command.setWorkOrderNo("WO005");
        command.setFormulaCode("F005");
        command.setOrderDate(new Date());
        command.setOrderBatchNum(20);

        WorkOrderModel model = mock(WorkOrderModel.class);
        when(workOrderModelFactory.loadById(5L)).thenReturn(model);

        applicationService.updateWorkOrder(command);

        verify(model).loadFromUpdateCommand(command);
        verify(model).checkWorkOrderNoUnique();
        verify(model).updateById();
    }

    @Test
    void deleteWorkOrderShouldDeleteEachById() {
        WorkOrderModel model1 = mock(WorkOrderModel.class);
        WorkOrderModel model2 = mock(WorkOrderModel.class);
        when(workOrderModelFactory.loadById(1L)).thenReturn(model1);
        when(workOrderModelFactory.loadById(2L)).thenReturn(model2);

        applicationService.deleteWorkOrder(new BulkOperationCommand<>(List.of(1L, 2L)));

        verify(model1).checkCanDelete();
        verify(model1).deleteById();
        verify(model2).checkCanDelete();
        verify(model2).deleteById();
    }

    @Test
    void deleteWorkOrderShouldRejectCompletedWorkOrder() {
        WorkOrderModel model = mock(WorkOrderModel.class);
        when(workOrderModelFactory.loadById(1L)).thenReturn(model);
        doThrow(new ApiException(Business.WORK_ORDER_COMPLETED_CAN_NOT_BE_DELETED))
            .when(model).checkCanDelete();

        ApiException exception = assertThrows(ApiException.class,
            () -> applicationService.deleteWorkOrder(new BulkOperationCommand<>(List.of(1L))));

        assertEquals(Business.WORK_ORDER_COMPLETED_CAN_NOT_BE_DELETED, exception.getErrorCode());
    }

}
