package com.factorylink.domain.business.formula.model;

import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.material.db.BizMaterialService;
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
    private final BizMaterialService materialService;

    public FormulaModel loadById(Long formulaId) {
        BizFormulaEntity byId = formulaService.getById(formulaId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, formulaId, "配方");
        }
        return new FormulaModel(byId, formulaService, formulaItemService, materialService);
    }

    public FormulaModel create() {
        return new FormulaModel(formulaService, formulaItemService, materialService);
    }

}
