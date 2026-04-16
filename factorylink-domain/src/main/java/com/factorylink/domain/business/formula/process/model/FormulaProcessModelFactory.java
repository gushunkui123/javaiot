package com.factorylink.domain.business.formula.process.model;

import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessService;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class FormulaProcessModelFactory {

    private final BizFormulaProcessService formulaProcessService;
    private final BizFormulaProcessStepService formulaProcessStepService;

    public FormulaProcessModel loadById(Long processId) {
        BizFormulaProcessEntity byId = formulaProcessService.getById(processId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, processId, "工艺");
        }
        return new FormulaProcessModel(byId, formulaProcessService, formulaProcessStepService);
    }

    public FormulaProcessModel create() {
        return new FormulaProcessModel(formulaProcessService, formulaProcessStepService);
    }

}
