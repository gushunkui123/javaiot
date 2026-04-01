package com.factorylink.domain.business.material.model;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.material.command.AddMaterialCommand;
import com.factorylink.domain.business.material.db.BizMaterialService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MaterialModelTest {

    private static final long MATERIAL_ID = 1L;

    private final BizMaterialService materialService = mock(BizMaterialService.class);
    private final MaterialModelFactory materialModelFactory = new MaterialModelFactory(materialService);

    @Test
    void loadFromAddCommandShouldTrimFields() {
        AddMaterialCommand command = new AddMaterialCommand();
        command.setMaterialCode("  M001  ");
        command.setMaterialType("  金属  ");
        command.setMaterialName("  铜线  ");
        MaterialModel materialModel = materialModelFactory.create();

        materialModel.loadFromAddCommand(command);

        Assertions.assertEquals("M001", materialModel.getMaterialCode());
        Assertions.assertEquals("金属", materialModel.getMaterialType());
        Assertions.assertEquals("铜线", materialModel.getMaterialName());
    }

    @Test
    void checkMaterialUniqueShouldRejectDuplicatedCode() {
        MaterialModel material = materialModelFactory.create();
        material.setMaterialId(MATERIAL_ID);
        material.setMaterialCode("M001");
        material.setMaterialType("金属");
        material.setMaterialName("铜线");

        when(materialService.isMaterialCodeDuplicated(eq(MATERIAL_ID), eq("M001"))).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, material::checkMaterialUnique);
        Assertions.assertEquals(Business.MATERIAL_CODE_IS_NOT_UNIQUE, exception.getErrorCode());
    }

    @Test
    void checkMaterialUniqueShouldRejectDuplicatedTypeAndName() {
        MaterialModel duplicatedMaterial = materialModelFactory.create();
        duplicatedMaterial.setMaterialId(MATERIAL_ID);
        duplicatedMaterial.setMaterialCode("M001");
        duplicatedMaterial.setMaterialType("金属");
        duplicatedMaterial.setMaterialName("铜线");
        MaterialModel newMaterial = materialModelFactory.create();
        newMaterial.setMaterialId(MATERIAL_ID);
        newMaterial.setMaterialCode("M002");
        newMaterial.setMaterialType("金属");
        newMaterial.setMaterialName("铝线");

        when(materialService.isMaterialCodeDuplicated(eq(MATERIAL_ID), eq("M001"))).thenReturn(false);
        when(materialService.isMaterialCodeDuplicated(eq(MATERIAL_ID), eq("M002"))).thenReturn(false);
        when(materialService.isMaterialDuplicated(eq(MATERIAL_ID), eq("金属"), eq("铜线"))).thenReturn(true);
        when(materialService.isMaterialDuplicated(eq(MATERIAL_ID), eq("金属"), eq("铝线"))).thenReturn(false);

        ApiException exception = assertThrows(ApiException.class, duplicatedMaterial::checkMaterialUnique);
        Assertions.assertEquals(Business.MATERIAL_TYPE_AND_NAME_IS_NOT_UNIQUE, exception.getErrorCode());
        Assertions.assertDoesNotThrow(newMaterial::checkMaterialUnique);
    }

}
