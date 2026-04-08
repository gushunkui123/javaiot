package com.factorylink.domain.business.formula.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.formula.command.AddFormulaCommand;
import com.factorylink.domain.business.formula.command.FormulaItemCommand;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.material.db.BizMaterialService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormulaModelTest {

    private final BizFormulaService formulaService = mock(BizFormulaService.class);
    private final BizFormulaItemService formulaItemService = mock(BizFormulaItemService.class);
    private final BizMaterialService materialService = mock(BizMaterialService.class);
    private final FormulaModelFactory formulaModelFactory = new FormulaModelFactory(formulaService, formulaItemService, materialService);

    @Test
    void loadFromAddCommandShouldTrimFields() {
        AddFormulaCommand command = new AddFormulaCommand();
        command.setFormulaCode("  F001  ");
        command.setFormulaName("  测试配方  ");
        command.setItems(List.of());
        FormulaModel model = formulaModelFactory.create();

        model.loadFromAddCommand(command);

        assertEquals("F001", model.getFormulaCode());
        assertEquals("测试配方", model.getFormulaName());
    }

    @Test
    void loadFromAddCommandShouldCopyItems() {
        FormulaItemCommand item = new FormulaItemCommand();
        item.setMaterialId(10L);
        item.setMaterialWeight(BigDecimal.valueOf(5.0));
        AddFormulaCommand command = new AddFormulaCommand();
        command.setFormulaCode("F002");
        command.setFormulaName("配方二");
        command.setItems(List.of(item));
        FormulaModel model = formulaModelFactory.create();

        model.loadFromAddCommand(command);

        assertEquals(1, model.getItems().size());
        assertEquals(10L, model.getItems().getFirst().getMaterialId());
    }

    @Test
    void loadFromAddCommandShouldDoNothingWhenNull() {
        FormulaModel model = formulaModelFactory.create();
        model.setFormulaCode("EXISTING");

        model.loadFromAddCommand(null);

        assertEquals("EXISTING", model.getFormulaCode());
    }

    @Test
    void checkFormulaCodeUniqueShouldThrowWhenDuplicated() {
        FormulaModel model = formulaModelFactory.create();
        model.setFormulaId(1L);
        model.setFormulaCode("F001");
        when(formulaService.isFormulaCodeDuplicated(1L, "F001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, model::checkFormulaCodeUnique);
        assertEquals(Business.FORMULA_CODE_IS_NOT_UNIQUE, exception.getErrorCode());
    }

    @Test
    void checkFormulaCodeUniqueShouldPassWhenNotDuplicated() {
        FormulaModel model = formulaModelFactory.create();
        model.setFormulaId(1L);
        model.setFormulaCode("F999");
        when(formulaService.isFormulaCodeDuplicated(1L, "F999")).thenReturn(false);

        assertDoesNotThrow(model::checkFormulaCodeUnique);
    }

    @Test
    void factoryLoadByIdShouldThrowWhenNotFound() {
        when(formulaService.getById(99L)).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class, () -> formulaModelFactory.loadById(99L));
        assertEquals(Business.COMMON_OBJECT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void factoryLoadByIdShouldReturnModelWhenFound() {
        BizFormulaEntity entity = new BizFormulaEntity();
        entity.setFormulaId(1L);
        entity.setFormulaCode("F001");
        when(formulaService.getById(1L)).thenReturn(entity);

        FormulaModel model = formulaModelFactory.loadById(1L);

        assertEquals(1L, model.getFormulaId());
        assertEquals("F001", model.getFormulaCode());
    }
}
