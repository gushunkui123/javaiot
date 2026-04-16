package com.factorylink.domain.business.machine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.factorylink.common.config.MachineConfigProvider;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessService;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepService;
import com.factorylink.domain.business.machine.converter.FormulaScaleConverter;
import com.factorylink.domain.business.machine.converter.MaterialScaleConverter;
import com.factorylink.domain.business.machine.converter.WorkOrderScaleConverter;
import com.factorylink.domain.business.machine.db.BizSyncLogEntity;
import com.factorylink.domain.business.machine.db.BizSyncLogService;
import com.factorylink.domain.business.machine.dto.SyncResultDTO;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.factorylink.domain.business.workorder.db.BizWorkOrderService;
import com.factorylink.infrastructure.machine.client.MainScaleClient;
import com.factorylink.infrastructure.machine.client.MicroScaleClient;
import com.factorylink.infrastructure.machine.dto.ScaleApiResponse;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaProcessRequest;
import com.factorylink.infrastructure.machine.dto.response.MaterialInBucketData;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ScaleSyncServiceTest {

    private final MainScaleClient mainScaleClient = mock(MainScaleClient.class);
    private final MicroScaleClient microScaleClient = mock(MicroScaleClient.class);
    private final MachineConfigProvider machineConfigProvider = mock(MachineConfigProvider.class);
    private final BizSyncLogService syncLogService = mock(BizSyncLogService.class);
    private final BizFormulaService formulaService = mock(BizFormulaService.class);
    private final BizFormulaItemService formulaItemService = mock(BizFormulaItemService.class);
    private final BizFormulaProcessService formulaProcessService = mock(BizFormulaProcessService.class);
    private final BizFormulaProcessStepService formulaProcessStepService = mock(BizFormulaProcessStepService.class);
    private final BizMaterialService materialService = mock(BizMaterialService.class);
    private final BizWorkOrderService workOrderService = mock(BizWorkOrderService.class);
    private final FormulaScaleConverter formulaScaleConverter = new FormulaScaleConverter(materialService);
    private final ScaleSyncService scaleSyncService =
        new ScaleSyncService(
            mainScaleClient,
            microScaleClient,
            machineConfigProvider,
            syncLogService,
            new ObjectMapper(),
            formulaScaleConverter,
            new MaterialScaleConverter(),
            new WorkOrderScaleConverter(),
            formulaService,
            formulaItemService,
            formulaProcessService,
            formulaProcessStepService,
            materialService,
            workOrderService
        );

    @Test
    void syncFormulaShouldUseBoundProcessStepsInOrder() {
        BizFormulaEntity formula = new BizFormulaEntity();
        formula.setFormulaId(10L);
        formula.setFormulaCode("F001");
        formula.setFormulaName("测试配方");
        formula.setProcessId(5L);
        when(formulaService.getById(10L)).thenReturn(formula);

        BizFormulaItemEntity mainItem = new BizFormulaItemEntity();
        mainItem.setMaterialId(1L);
        mainItem.setMaterialWeight(BigDecimal.TEN);
        mainItem.setStepNo(1);
        BizFormulaItemEntity microItem = new BizFormulaItemEntity();
        microItem.setMaterialId(2L);
        microItem.setMaterialWeight(BigDecimal.ONE);
        when(formulaItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(mainItem, microItem));

        BizMaterialEntity mainMaterial = new BizMaterialEntity();
        mainMaterial.setMaterialId(1L);
        mainMaterial.setMaterialCode("M001");
        mainMaterial.setMaterialType("主料");
        mainMaterial.setMaterialName("主料1");
        BizMaterialEntity microMaterial = new BizMaterialEntity();
        microMaterial.setMaterialId(2L);
        microMaterial.setMaterialCode("A001");
        microMaterial.setMaterialType("色料");
        microMaterial.setMaterialName("色料1");
        when(materialService.listByIds(any())).thenReturn(List.of(mainMaterial, microMaterial));

        BizFormulaProcessEntity process = new BizFormulaProcessEntity();
        process.setProcessId(5L);
        when(formulaProcessService.getById(5L)).thenReturn(process);

        BizFormulaProcessStepEntity step2 = new BizFormulaProcessStepEntity();
        step2.setStepId(2L);
        step2.setSortOrder(2);
        step2.setStepNo(0);
        step2.setActionId(9);
        step2.setMixingTime(BigDecimal.valueOf(8));
        step2.setMixingCurrent(BigDecimal.valueOf(9));
        step2.setMixingTemp(BigDecimal.valueOf(10));
        step2.setRotateSpeed(BigDecimal.valueOf(11));
        step2.setPressure(BigDecimal.valueOf(12));
        step2.setClosingConditionId(13);
        step2.setTurningTimes(14);
        step2.setRisingTime(BigDecimal.valueOf(15));
        step2.setFallingTime(BigDecimal.valueOf(16));
        BizFormulaProcessStepEntity step1 = new BizFormulaProcessStepEntity();
        step1.setStepId(1L);
        step1.setSortOrder(1);
        step1.setStepNo(1);
        step1.setActionId(2);
        step1.setMixingTime(BigDecimal.ONE);
        step1.setMixingCurrent(BigDecimal.valueOf(2));
        step1.setMixingTemp(BigDecimal.valueOf(3));
        step1.setRotateSpeed(BigDecimal.valueOf(4));
        step1.setPressure(BigDecimal.valueOf(5));
        step1.setClosingConditionId(6);
        step1.setTurningTimes(7);
        step1.setRisingTime(BigDecimal.valueOf(8));
        step1.setFallingTime(BigDecimal.valueOf(9));
        when(formulaProcessStepService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(step2, step1));

        when(machineConfigProvider.isDeviceEnabled("MAIN_SCALE")).thenReturn(true);
        when(machineConfigProvider.isDeviceEnabled("MICRO_SCALE")).thenReturn(true);
        when(machineConfigProvider.isDeviceOnline("MAIN_SCALE")).thenReturn(true);
        when(machineConfigProvider.isDeviceOnline("MICRO_SCALE")).thenReturn(true);
        when(mainScaleClient.deleteFormula(any())).thenReturn(successResponse());
        when(microScaleClient.deleteFormula(any())).thenReturn(successResponse());
        when(mainScaleClient.addParts(any())).thenReturn(successResponse());
        when(mainScaleClient.addFormula(any())).thenReturn(successResponse());
        when(microScaleClient.addFormula(any())).thenReturn(successResponse());
        when(mainScaleClient.updateFormulaProcess(any())).thenReturn(successResponse());
        when(mainScaleClient.getMaterialInBuckets()).thenReturn(responseWithData(bucket("M001")));
        when(microScaleClient.getMaterialInBuckets()).thenReturn(responseWithData(bucket("A001")));
        when(syncLogService.save(any(BizSyncLogEntity.class))).thenReturn(true);

        SyncResultDTO result = scaleSyncService.syncFormula(10L, ScaleSyncService.OperationType.ADD);

        assertEquals(true, result.isAllSuccess());
        ArgumentCaptor<ScaleFormulaProcessRequest> captor =
            ArgumentCaptor.forClass(ScaleFormulaProcessRequest.class);
        verify(mainScaleClient).updateFormulaProcess(captor.capture());
        ScaleFormulaProcessRequest request = captor.getValue();
        assertEquals("F001", request.getFormulaCode());
        assertEquals(2, request.getFormulaProcessEntryList().size());
        assertEquals(2, request.getFormulaProcessEntryList().get(0).getActionId());
        assertEquals(1, request.getFormulaProcessEntryList().get(0).getStepNo());
        assertEquals(BigDecimal.valueOf(5), request.getFormulaProcessEntryList().get(0).getPressure());
        assertEquals(9, request.getFormulaProcessEntryList().get(1).getActionId());
        assertEquals(BigDecimal.valueOf(12), request.getFormulaProcessEntryList().get(1).getPressure());
    }

    @Test
    void syncFormulaShouldRejectWhenProcessNotBound() {
        BizFormulaEntity formula = new BizFormulaEntity();
        formula.setFormulaId(10L);
        formula.setFormulaCode("F001");
        when(formulaService.getById(10L)).thenReturn(formula);
        when(machineConfigProvider.isDeviceEnabled("MAIN_SCALE")).thenReturn(true);
        when(machineConfigProvider.isDeviceEnabled("MICRO_SCALE")).thenReturn(true);
        when(machineConfigProvider.isDeviceOnline("MAIN_SCALE")).thenReturn(true);
        when(machineConfigProvider.isDeviceOnline("MICRO_SCALE")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class,
            () -> scaleSyncService.syncFormula(10L, ScaleSyncService.OperationType.ADD));

        assertEquals(Business.FORMULA_NO_PROCESS_ASSIGNED_CAN_NOT_SYNC, exception.getErrorCode());
        verify(mainScaleClient, never()).deleteFormula(any());
        verify(mainScaleClient, never()).addFormula(any());
        verify(mainScaleClient, never()).updateFormulaProcess(any());
    }

    private <T> ScaleApiResponse<T> successResponse() {
        ScaleApiResponse<T> response = new ScaleApiResponse<>();
        response.setSuccess(1);
        return response;
    }

    private ScaleApiResponse<MaterialInBucketData> responseWithData(MaterialInBucketData data) {
        ScaleApiResponse<MaterialInBucketData> response = new ScaleApiResponse<>();
        response.setSuccess(1);
        response.setRtndata(List.of(data));
        return response;
    }

    private MaterialInBucketData bucket(String materialNo) {
        MaterialInBucketData data = new MaterialInBucketData();
        data.setMaterialNo(materialNo);
        return data;
    }
}
