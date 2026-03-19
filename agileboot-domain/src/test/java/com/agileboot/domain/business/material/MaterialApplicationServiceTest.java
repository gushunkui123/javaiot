package com.agileboot.domain.business.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.common.audit.AuditUserEnricher;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.business.material.command.AddMaterialCommand;
import com.agileboot.domain.business.material.command.UpdateMaterialCommand;
import com.agileboot.domain.business.material.dto.MaterialDTO;
import com.agileboot.domain.business.material.query.MaterialQuery;
import com.agileboot.domain.business.material.db.BizMaterialEntity;
import com.agileboot.domain.business.material.db.BizMaterialService;
import com.agileboot.domain.business.material.model.MaterialModel;
import com.agileboot.domain.business.material.model.MaterialModelFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MaterialApplicationServiceTest {

    private final MaterialModelFactory materialModelFactory = mock(MaterialModelFactory.class);
    private final BizMaterialService materialService = mock(BizMaterialService.class);
    private final AuditUserEnricher auditUserEnricher = mock(AuditUserEnricher.class);
    private final MaterialApplicationService applicationService =
        new MaterialApplicationService(materialModelFactory, materialService, auditUserEnricher);

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
    void addMaterialShouldValidateAndInsert() {
        AddMaterialCommand command = new AddMaterialCommand();
        MaterialModel materialModel = mock(MaterialModel.class);
        when(materialModelFactory.create()).thenReturn(materialModel);

        applicationService.addMaterial(command);

        verify(materialModel).loadFromAddCommand(command);
        verify(materialModel).checkMaterialUnique();
        verify(materialModel).insert();
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
        applicationService.deleteMaterial(new BulkOperationCommand<>(List.of(2L, 3L)));

        ArgumentCaptor<Collection<Long>> deletedIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(materialService).removeBatchByIds(deletedIdsCaptor.capture());
        Collection<Long> deletedIds = deletedIdsCaptor.getValue();
        assertEquals(2, deletedIds.size());
        org.junit.jupiter.api.Assertions.assertTrue(deletedIds.containsAll(List.of(2L, 3L)));
    }

}
