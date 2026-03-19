package com.agileboot.domain.business.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.business.formula.db.BizFormulaEntity;
import com.agileboot.domain.business.formula.db.BizFormulaItemEntity;
import com.agileboot.domain.business.formula.db.BizFormulaItemService;
import com.agileboot.domain.business.formula.db.BizFormulaService;
import com.agileboot.domain.business.formula.dto.FormulaDTO;
import com.agileboot.domain.business.formula.model.FormulaModel;
import com.agileboot.domain.business.formula.model.FormulaModelFactory;
import com.agileboot.domain.business.formula.query.FormulaQuery;
import com.agileboot.domain.common.audit.AuditUserEnricher;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormulaApplicationServiceTest {

    private final FormulaModelFactory formulaModelFactory = mock(FormulaModelFactory.class);
    private final BizFormulaService formulaService = mock(BizFormulaService.class);
    private final BizFormulaItemService formulaItemService = mock(BizFormulaItemService.class);
    private final AuditUserEnricher auditUserEnricher = mock(AuditUserEnricher.class);
    private final FormulaApplicationService applicationService =
        new FormulaApplicationService(formulaModelFactory, formulaService, formulaItemService, auditUserEnricher);

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

}
