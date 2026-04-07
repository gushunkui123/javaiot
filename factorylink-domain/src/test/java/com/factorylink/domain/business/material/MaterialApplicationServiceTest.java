package com.factorylink.domain.business.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factorylink.common.exception.ApiException;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.common.core.page.PageDTO;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.material.command.AddMaterialCommand;
import com.factorylink.domain.business.material.command.UpdateMaterialCommand;
import com.factorylink.domain.business.material.dto.MaterialDTO;
import com.factorylink.domain.business.machine.ScaleSyncService;
import com.factorylink.domain.business.machine.ScaleSyncService.OperationType;
import com.factorylink.domain.business.machine.dto.SyncResultDTO;
import com.factorylink.domain.business.material.query.MaterialQuery;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.factorylink.domain.business.material.model.MaterialModel;
import com.factorylink.domain.business.material.model.MaterialModelFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MaterialApplicationServiceTest {

    private final MaterialModelFactory materialModelFactory = mock(MaterialModelFactory.class);
    private final BizMaterialService materialService = mock(BizMaterialService.class);
    private final BizFormulaItemService formulaItemService = mock(BizFormulaItemService.class);
    private final AuditUserEnricher auditUserEnricher = mock(AuditUserEnricher.class);
    private final ScaleSyncService scaleSyncService = mock(ScaleSyncService.class);
    private final MaterialApplicationService applicationService =
        new MaterialApplicationService(
            materialModelFactory,
            materialService,
            formulaItemService,
            auditUserEnricher,
            scaleSyncService
        );

    @Test
    void getMaterialListShouldEnrichAuditUsers() {
        MaterialQuery query = mock(MaterialQuery.class);
        BizMaterialEntity entity = new BizMaterialEntity();
        entity.setMaterialId(1L);
        Page<BizMaterialEntity> page = new Page<>();
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(materialService.page(any(), any())).thenReturn(page);

        PageDTO<MaterialDTO> pageDTO = applicationService.getMaterialList(query);

        assertEquals(1, pageDTO.getRows().size());
        verify(auditUserEnricher).enrich(pageDTO.getRows());
    }

    @Test
    void addMaterialShouldValidateInsertAndAutoSync() {
        AddMaterialCommand command = new AddMaterialCommand();
        MaterialModel materialModel = mock(MaterialModel.class);
        when(materialModelFactory.create()).thenReturn(materialModel);
        when(materialModel.getMaterialId()).thenReturn(10L);
        when(scaleSyncService.syncMaterial(10L, OperationType.ADD)).thenReturn(new SyncResultDTO());

        applicationService.addMaterial(command);

        verify(materialModel).loadFromAddCommand(command);
        verify(materialModel).checkMaterialUnique();
        verify(materialModel).insert();
        verify(scaleSyncService).syncMaterial(10L, OperationType.ADD);
    }

    @Test
    void addMaterialShouldNotFailWhenAutoSyncThrows() {
        AddMaterialCommand command = new AddMaterialCommand();
        MaterialModel materialModel = mock(MaterialModel.class);
        when(materialModelFactory.create()).thenReturn(materialModel);
        when(materialModel.getMaterialId()).thenReturn(11L);
        doThrow(new RuntimeException("sync failed"))
            .when(scaleSyncService).syncMaterial(11L, OperationType.ADD);

        assertDoesNotThrow(() -> applicationService.addMaterial(command));

        verify(materialModel).insert();
        verify(scaleSyncService).syncMaterial(11L, OperationType.ADD);
    }

    @Test
    void updateMaterialShouldValidateAndPersist() {
        UpdateMaterialCommand command = new UpdateMaterialCommand();
        command.setMaterialId(8L);
        MaterialModel materialModel = mock(MaterialModel.class);
        when(materialModelFactory.loadById(8L)).thenReturn(materialModel);

        applicationService.updateMaterial(command);

        verify(materialModel).loadFromUpdateCommand(command);
        verify(materialModel).checkMaterialUnique();
        verify(materialModel).updateById();
    }

    @Test
    void deleteMaterialShouldRemoveBatchByIds() {
        when(formulaItemService.exists(any())).thenReturn(false);

        applicationService.deleteMaterial(new BulkOperationCommand<>(List.of(2L, 3L)));

        ArgumentCaptor<Collection<Long>> deletedIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(materialService).removeBatchByIds(deletedIdsCaptor.capture());
        Collection<Long> deletedIds = deletedIdsCaptor.getValue();
        assertEquals(2, deletedIds.size());
        assertTrue(deletedIds.containsAll(List.of(2L, 3L)));
    }

    @Test
    void deleteMaterialShouldRejectWhenReferencedByFormula() {
        when(formulaItemService.exists(any())).thenReturn(true);

        assertThrows(ApiException.class, () ->
            applicationService.deleteMaterial(new BulkOperationCommand<>(List.of(2L))));

        verify(materialService, never()).removeBatchByIds(any());
    }

    @Test
    void importMaterialShouldAddEachCommandAndAutoSync() {
        AddMaterialCommand cmd1 = new AddMaterialCommand();
        cmd1.setMaterialName("铜线");
        cmd1.setMaterialType("金属");
        AddMaterialCommand cmd2 = new AddMaterialCommand();
        cmd2.setMaterialName("铝线");
        cmd2.setMaterialType("金属");

        MaterialModel model1 = mock(MaterialModel.class);
        MaterialModel model2 = mock(MaterialModel.class);
        when(materialModelFactory.create()).thenReturn(model1, model2);
        when(model1.getMaterialId()).thenReturn(20L);
        when(model2.getMaterialId()).thenReturn(21L);
        when(scaleSyncService.syncMaterial(any(), eq(OperationType.ADD))).thenReturn(new SyncResultDTO());

        applicationService.importMaterial(List.of(cmd1, cmd2));

        verify(model1).loadFromAddCommand(cmd1);
        verify(model1).checkMaterialUnique();
        verify(model1).insert();
        verify(scaleSyncService).syncMaterial(20L, OperationType.ADD);
        verify(model2).loadFromAddCommand(cmd2);
        verify(model2).checkMaterialUnique();
        verify(model2).insert();
        verify(scaleSyncService).syncMaterial(21L, OperationType.ADD);
    }

}
