package com.factorylink.domain.business.workorder.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.workorder.command.AddWorkOrderCommand;
import com.factorylink.domain.business.workorder.db.BizWorkOrderService;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.jupiter.api.Test;

class WorkOrderModelTest {

    private final BizWorkOrderService workOrderService = mock(BizWorkOrderService.class);
    private final WorkOrderModelFactory workOrderModelFactory = new WorkOrderModelFactory(workOrderService);

    @Test
    void loadFromAddCommandShouldTrimFields() {
        AddWorkOrderCommand command = new AddWorkOrderCommand();
        command.setWorkOrderNo("  WO001  ");
        command.setFormulaCode("  F001  ");
        command.setOrderDate(new Date());
        command.setMachineId(1);
        command.setOrderBatchNum(10);
        WorkOrderModel model = workOrderModelFactory.create();

        model.loadFromAddCommand(command);

        assertEquals("WO001", model.getWorkOrderNo());
        assertEquals("F001", model.getFormulaCode());
    }

    @Test
    void loadFromAddCommandShouldDoNothingWhenNull() {
        WorkOrderModel model = workOrderModelFactory.create();
        model.setWorkOrderNo("EXISTING");

        model.loadFromAddCommand(null);

        assertEquals("EXISTING", model.getWorkOrderNo());
    }

    @Test
    void loadFromAddCommandShouldCopyAllFields() {
        AddWorkOrderCommand command = new AddWorkOrderCommand();
        command.setWorkOrderNo("WO100");
        command.setFormulaCode("F100");
        command.setPlant("工厂A");
        command.setMachineId(3);
        command.setLineNo("A");
        command.setOrderBatchNum(5);
        command.setOrderDate(new Date());
        command.setBatchWeight(BigDecimal.valueOf(40));
        WorkOrderModel model = workOrderModelFactory.create();

        model.loadFromAddCommand(command);

        assertEquals("WO100", model.getWorkOrderNo());
        assertEquals("F100", model.getFormulaCode());
        assertEquals("工厂A", model.getPlant());
        assertEquals(3, model.getMachineId());
        assertEquals("A", model.getLineNo());
        assertEquals(5, model.getOrderBatchNum());
        assertEquals(BigDecimal.valueOf(40), model.getBatchWeight());
        assertEquals(BigDecimal.valueOf(200), model.getOrderWeight());
    }

    @Test
    void checkWorkOrderNoUniqueShouldThrowWhenDuplicated() {
        WorkOrderModel model = workOrderModelFactory.create();
        model.setWorkOrderId(1L);
        model.setWorkOrderNo("WO001");
        when(workOrderService.isWorkOrderNoDuplicated(1L, "WO001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, model::checkWorkOrderNoUnique);
        assertEquals(Business.WORK_ORDER_NO_IS_NOT_UNIQUE, exception.getErrorCode());
    }

    @Test
    void checkWorkOrderNoUniqueShouldPassWhenNotDuplicated() {
        WorkOrderModel model = workOrderModelFactory.create();
        model.setWorkOrderId(1L);
        model.setWorkOrderNo("WO999");
        when(workOrderService.isWorkOrderNoDuplicated(1L, "WO999")).thenReturn(false);

        assertDoesNotThrow(model::checkWorkOrderNoUnique);
    }

    @Test
    void factoryLoadByIdShouldThrowWhenNotFound() {
        when(workOrderService.getById(99L)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class, () -> workOrderModelFactory.loadById(99L));
        assertEquals(Business.COMMON_OBJECT_NOT_FOUND, exception.getErrorCode());
    }
}
