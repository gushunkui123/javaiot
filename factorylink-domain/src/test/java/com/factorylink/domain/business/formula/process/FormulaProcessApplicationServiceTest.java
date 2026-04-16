package com.factorylink.domain.business.formula.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.formula.process.command.AddFormulaProcessCommand;
import com.factorylink.domain.business.formula.process.command.FormulaProcessStepCommand;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessService;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepService;
import com.factorylink.domain.business.formula.process.dto.FormulaProcessDTO;
import com.factorylink.domain.business.formula.process.model.FormulaProcessModel;
import com.factorylink.domain.business.formula.process.model.FormulaProcessModelFactory;
import com.factorylink.domain.business.formula.process.query.FormulaProcessQuery;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.common.command.BulkOperationCommand;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormulaProcessApplicationServiceTest {

    private final FormulaProcessModelFactory formulaProcessModelFactory = mock(FormulaProcessModelFactory.class);
    private final BizFormulaProcessService formulaProcessService = mock(BizFormulaProcessService.class);
    private final BizFormulaProcessStepService formulaProcessStepService = mock(BizFormulaProcessStepService.class);
    private final BizFormulaService formulaService = mock(BizFormulaService.class);
    private final AuditUserEnricher auditUserEnricher = mock(AuditUserEnricher.class);
    private final FormulaProcessApplicationService applicationService =
        new FormulaProcessApplicationService(formulaProcessModelFactory, formulaProcessService,
            formulaProcessStepService, formulaService, auditUserEnricher);

    @Test
    void getFormulaProcessListShouldEnrichAuditUsers() {
        FormulaProcessQuery query = mock(FormulaProcessQuery.class);
        BizFormulaProcessEntity entity = new BizFormulaProcessEntity();
        entity.setProcessId(1L);
        Page<BizFormulaProcessEntity> page = new Page<>();
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(formulaProcessService.page(any(), any())).thenReturn(page);

        PageDTO<FormulaProcessDTO> pageDTO = applicationService.getFormulaProcessList(query);

        assertEquals(1, pageDTO.getRows().size());
        verify(auditUserEnricher).enrich(pageDTO.getRows());
    }

    @Test
    void getFormulaProcessInfoShouldReturnStepsSorted() {
        FormulaProcessModel model = new FormulaProcessModel();
        model.setProcessId(8L);
        model.setProcessCode("P001");
        when(formulaProcessModelFactory.loadById(8L)).thenReturn(model);

        BizFormulaProcessStepEntity step2 = new BizFormulaProcessStepEntity();
        step2.setStepId(2L);
        step2.setSortOrder(2);
        step2.setActionId(9);
        BizFormulaProcessStepEntity step1 = new BizFormulaProcessStepEntity();
        step1.setStepId(1L);
        step1.setSortOrder(1);
        step1.setActionId(2);
        when(formulaProcessStepService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(step2, step1));

        FormulaProcessDTO dto = applicationService.getFormulaProcessInfo(8L);

        assertEquals(8L, dto.getProcessId());
        assertEquals(2, dto.getSteps().size());
        assertEquals(2, dto.getSteps().get(0).getActionId());
        assertEquals(9, dto.getSteps().get(1).getActionId());
        verify(auditUserEnricher).enrich(dto);
    }

    @Test
    void addFormulaProcessShouldValidateAndInsert() {
        AddFormulaProcessCommand command = new AddFormulaProcessCommand();
        command.setProcessCode("P001");
        command.setProcessName("标准工艺");
        command.setSteps(List.of(buildStepCommand()));

        FormulaProcessModel model = mock(FormulaProcessModel.class);
        when(formulaProcessModelFactory.create()).thenReturn(model);

        applicationService.addFormulaProcess(command);

        verify(model).loadFromAddCommand(command);
        verify(model).checkProcessCodeUnique();
        verify(model).insert();
    }

    @Test
    void deleteFormulaProcessShouldRejectWhenReferencedByFormula() {
        when(formulaService.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
            () -> applicationService.deleteFormulaProcess(new BulkOperationCommand<>(List.of(1L))));

        assertEquals(Business.FORMULA_PROCESS_ALREADY_BOUND_TO_FORMULA_CAN_NOT_BE_DELETED, exception.getErrorCode());
        verify(formulaProcessModelFactory, never()).loadById(any());
    }

    private FormulaProcessStepCommand buildStepCommand() {
        FormulaProcessStepCommand step = new FormulaProcessStepCommand();
        step.setSortOrder(1);
        step.setStepNo(1);
        step.setActionId(2);
        step.setMixingTime(BigDecimal.ZERO);
        step.setMixingCurrent(BigDecimal.ZERO);
        step.setMixingTemp(BigDecimal.ZERO);
        step.setRotateSpeed(BigDecimal.ZERO);
        step.setPressure(BigDecimal.ZERO);
        step.setClosingConditionId(0);
        step.setTurningTimes(0);
        step.setRisingTime(BigDecimal.ZERO);
        step.setFallingTime(BigDecimal.ZERO);
        return step;
    }
}
