package com.factorylink.domain.business.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factorylink.common.core.page.PageDTO;
import com.factorylink.domain.business.formula.command.AddFormulaCommand;
import com.factorylink.domain.business.formula.command.FormulaItemCommand;
import com.factorylink.domain.business.formula.command.UpdateFormulaCommand;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.formula.dto.FormulaDTO;
import com.factorylink.domain.business.formula.model.FormulaModel;
import com.factorylink.domain.business.formula.model.FormulaModelFactory;
import com.factorylink.domain.business.formula.query.FormulaQuery;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormulaApplicationServiceTest {

    private final FormulaModelFactory formulaModelFactory = mock(FormulaModelFactory.class);
    private final BizFormulaService formulaService = mock(BizFormulaService.class);
    private final BizFormulaItemService formulaItemService = mock(BizFormulaItemService.class);
    private final AuditUserEnricher auditUserEnricher = mock(AuditUserEnricher.class);
    private final BizMaterialService materialService = mock(BizMaterialService.class);
    private final FormulaApplicationService applicationService =
        new FormulaApplicationService(formulaModelFactory, formulaService, formulaItemService, auditUserEnricher, materialService);

    @Test
    void getFormulaListShouldEnrichAuditUsers() {
        FormulaQuery query = mock(FormulaQuery.class);
        BizFormulaEntity entity = new BizFormulaEntity();
        entity.setFormulaId(1L);
        Page<BizFormulaEntity> page = new Page<>();
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(formulaService.page(any(), any())).thenReturn(page);

        PageDTO<FormulaDTO> pageDTO = applicationService.getFormulaList(query);

        assertEquals(1, pageDTO.getRows().size());
        verify(auditUserEnricher).enrich(pageDTO.getRows());
    }

    @Test
    void getFormulaInfoShouldEnrichAuditUsers() {
        FormulaModel model = new FormulaModel();
        model.setFormulaId(8L);
        model.setFormulaCode("F001");
        when(formulaModelFactory.loadById(8L)).thenReturn(model);
        when(formulaItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.<BizFormulaItemEntity>of());

        FormulaDTO dto = applicationService.getFormulaInfo(8L);

        assertEquals(8L, dto.getFormulaId());
        verify(auditUserEnricher).enrich(dto);
    }

    @Test
    void getFormulaInfoShouldReturnItemsSortedByOrder() {
        FormulaModel model = new FormulaModel();
        model.setFormulaId(8L);
        model.setFormulaCode("F001");
        when(formulaModelFactory.loadById(8L)).thenReturn(model);

        BizFormulaItemEntity item1 = new BizFormulaItemEntity();
        item1.setFormulaId(8L);
        item1.setMaterialId(1L);
        item1.setSortOrder(1);
        BizFormulaItemEntity item2 = new BizFormulaItemEntity();
        item2.setFormulaId(8L);
        item2.setMaterialId(2L);
        item2.setSortOrder(2);
        when(formulaItemService.list(any(LambdaQueryWrapper.class))).thenReturn(List.of(item1, item2));

        FormulaDTO dto = applicationService.getFormulaInfo(8L);

        assertEquals(2, dto.getItems().size());
        assertEquals(1L, dto.getItems().get(0).getMaterialId());
        assertEquals(2L, dto.getItems().get(1).getMaterialId());
    }

    @Test
    void addFormulaShouldValidateAndInsert() {
        AddFormulaCommand command = new AddFormulaCommand();
        command.setFormulaCode("F010");
        command.setFormulaName("新配方");
        FormulaItemCommand item = new FormulaItemCommand();
        item.setMaterialId(1L);
        item.setMaterialWeight(BigDecimal.TEN);
        command.setItems(List.of(item));

        FormulaModel formulaModel = mock(FormulaModel.class);
        when(formulaModelFactory.create()).thenReturn(formulaModel);

        applicationService.addFormula(command);

        verify(formulaModel).loadFromAddCommand(command);
        verify(formulaModel).checkFormulaCodeUnique();
        verify(formulaModel).insert();
    }

    @Test
    void updateFormulaShouldLoadAndPersist() {
        UpdateFormulaCommand command = new UpdateFormulaCommand();
        command.setFormulaId(5L);
        command.setFormulaCode("F005");
        command.setFormulaName("更新配方");
        command.setItems(List.of());

        FormulaModel formulaModel = mock(FormulaModel.class);
        when(formulaModelFactory.loadById(5L)).thenReturn(formulaModel);

        applicationService.updateFormula(command);

        verify(formulaModel).loadFromUpdateCommand(command);
        verify(formulaModel).checkFormulaCodeUnique();
        verify(formulaModel).updateById();
    }

    @Test
    void deleteFormulaShouldDeleteEachById() {
        FormulaModel model1 = mock(FormulaModel.class);
        FormulaModel model2 = mock(FormulaModel.class);
        when(formulaModelFactory.loadById(1L)).thenReturn(model1);
        when(formulaModelFactory.loadById(2L)).thenReturn(model2);

        applicationService.deleteFormula(new BulkOperationCommand<>(List.of(1L, 2L)));

        verify(model1).deleteById();
        verify(model2).deleteById();
    }

}
