package com.agileboot.domain.business.formula.model;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.business.formula.db.BizFormulaEntity;
import com.agileboot.domain.business.formula.db.BizFormulaItemService;
import com.agileboot.domain.business.formula.db.BizFormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class FormulaModelFactory {

    private final BizFormulaService formulaService;
    private final BizFormulaItemService formulaItemService;

    public FormulaModel loadById(Long formulaId) {
        BizFormulaEntity byId = formulaService.getById(formulaId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, formulaId, "配方");
        }
        return new FormulaModel(byId, formulaService, formulaItemService);
    }

    public FormulaModel create() {
        return new FormulaModel(formulaService, formulaItemService);
    }

}
