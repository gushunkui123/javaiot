package com.agileboot.domain.system.material;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.system.material.command.AddMaterialCommand;
import com.agileboot.domain.system.material.command.UpdateMaterialCommand;
import com.agileboot.domain.system.material.db.SysMaterialService;
import com.agileboot.domain.system.material.model.MaterialModel;
import com.agileboot.domain.system.material.model.MaterialModelFactory;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MaterialApplicationServiceTest {

    private final MaterialModelFactory materialModelFactory = mock(MaterialModelFactory.class);
    private final SysMaterialService materialService = mock(SysMaterialService.class);
    private final MaterialApplicationService applicationService = new MaterialApplicationService(materialModelFactory, materialService);

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
