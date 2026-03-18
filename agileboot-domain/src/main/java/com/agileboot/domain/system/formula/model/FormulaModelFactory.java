package com.agileboot.domain.system.formula.model;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.system.formula.db.SysFormulaEntity;
import com.agileboot.domain.system.formula.db.SysFormulaItemService;
import com.agileboot.domain.system.formula.db.SysFormulaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class FormulaModelFactory {

    private final SysFormulaService formulaService;
    private final SysFormulaItemService formulaItemService;

    public FormulaModel loadById(Long formulaId) {
        SysFormulaEntity byId = formulaService.getById(formulaId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, formulaId, "配方");
        }
        return new FormulaModel(byId, formulaService, formulaItemService);
    }

    public FormulaModel create() {
        return new FormulaModel(formulaService, formulaItemService);
    }

}
