package com.agileboot.domain.business.material.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.business.material.command.AddMaterialCommand;
import com.agileboot.domain.business.material.db.BizMaterialService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaterialModelTest {

    private static final long MATERIAL_ID = 1L;

    private final BizMaterialService materialService = mock(BizMaterialService.class);
    private final MaterialModelFactory materialModelFactory = new MaterialModelFactory(materialService);

    @Test
    void loadFromAddCommandShouldTrimFields() {
        AddMaterialCommand command = new AddMaterialCommand();
        command.setMaterialType("  金属  ");
        command.setMaterialName("  铜线  ");
        MaterialModel materialModel = materialModelFactory.create();

        materialModel.loadFromAddCommand(command);

        Assertions.assertEquals("金属", materialModel.getMaterialType());
        Assertions.assertEquals("铜线", materialModel.getMaterialName());
    }

    @Test
    void checkMaterialUniqueShouldRejectDuplicatedTypeAndName() {
        MaterialModel duplicatedMaterial = materialModelFactory.create();
        duplicatedMaterial.setMaterialId(MATERIAL_ID);
        duplicatedMaterial.setMaterialType("金属");
        duplicatedMaterial.setMaterialName("铜线");
        MaterialModel newMaterial = materialModelFactory.create();
        newMaterial.setMaterialId(MATERIAL_ID);
        newMaterial.setMaterialType("金属");
        newMaterial.setMaterialName("铝线");

        when(materialService.isMaterialDuplicated(eq(MATERIAL_ID), eq("金属"), eq("铜线"))).thenReturn(true);
        when(materialService.isMaterialDuplicated(eq(MATERIAL_ID), eq("金属"), eq("铝线"))).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, duplicatedMaterial::checkMaterialUnique);
        Assertions.assertEquals(Business.MATERIAL_TYPE_AND_NAME_IS_NOT_UNIQUE, exception.getErrorCode());
        Assertions.assertDoesNotThrow(newMaterial::checkMaterialUnique);
    }

}
