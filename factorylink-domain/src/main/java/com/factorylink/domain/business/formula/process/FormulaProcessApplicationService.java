package com.factorylink.domain.business.formula.process;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.formula.process.command.AddFormulaProcessCommand;
import com.factorylink.domain.business.formula.process.command.UpdateFormulaProcessCommand;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessService;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepService;
import com.factorylink.domain.business.formula.process.dto.FormulaProcessDTO;
import com.factorylink.domain.business.formula.process.dto.FormulaProcessStepDTO;
import com.factorylink.domain.business.formula.process.model.FormulaProcessModel;
import com.factorylink.domain.business.formula.process.model.FormulaProcessModelFactory;
import com.factorylink.domain.business.formula.process.query.FormulaProcessQuery;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.common.command.BulkOperationCommand;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class FormulaProcessApplicationService {

    private final FormulaProcessModelFactory formulaProcessModelFactory;

    private final BizFormulaProcessService formulaProcessService;

    private final BizFormulaProcessStepService formulaProcessStepService;

    private final BizFormulaService formulaService;

    private final AuditUserEnricher auditUserEnricher;

    public PageDTO<FormulaProcessDTO> getFormulaProcessList(FormulaProcessQuery query) {
        Page<BizFormulaProcessEntity> page = formulaProcessService.page(query.toPage(), query.toQueryWrapper());
        List<FormulaProcessDTO> records = page.getRecords().stream().map(FormulaProcessDTO::new).toList();
        auditUserEnricher.enrich(records);
        return new PageDTO<>(records, page.getTotal());
    }

    public FormulaProcessDTO getFormulaProcessInfo(Long processId) {
        FormulaProcessModel model = formulaProcessModelFactory.loadById(processId);
        FormulaProcessDTO dto = new FormulaProcessDTO(model);
        auditUserEnricher.enrich(dto);
        dto.setSteps(loadStepDTOs(processId));
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFormulaProcess(AddFormulaProcessCommand addCommand) {
        FormulaProcessModel model = formulaProcessModelFactory.create();
        model.loadFromAddCommand(addCommand);
        model.checkProcessCodeUnique();
        model.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFormulaProcess(UpdateFormulaProcessCommand updateCommand) {
        FormulaProcessModel model = formulaProcessModelFactory.loadById(updateCommand.getProcessId());
        model.loadFromUpdateCommand(updateCommand);
        model.checkProcessCodeUnique();
        model.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFormulaProcess(BulkOperationCommand<Long> deleteCommand) {
        boolean referenced = formulaService.exists(
            new LambdaQueryWrapper<BizFormulaEntity>()
                .in(BizFormulaEntity::getProcessId, deleteCommand.getIds()));
        if (referenced) {
            throw new ApiException(Business.FORMULA_PROCESS_ALREADY_BOUND_TO_FORMULA_CAN_NOT_BE_DELETED);
        }
        for (Long id : deleteCommand.getIds()) {
            FormulaProcessModel model = formulaProcessModelFactory.loadById(id);
            model.deleteById();
        }
    }

    private List<FormulaProcessStepDTO> loadStepDTOs(Long processId) {
        List<BizFormulaProcessStepEntity> stepEntities = formulaProcessStepService.list(
            new LambdaQueryWrapper<BizFormulaProcessStepEntity>()
                .eq(BizFormulaProcessStepEntity::getProcessId, processId)
                .orderByAsc(BizFormulaProcessStepEntity::getSortOrder)
                .orderByAsc(BizFormulaProcessStepEntity::getStepId));

        return stepEntities.stream()
            .sorted(Comparator
                .comparing(BizFormulaProcessStepEntity::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(BizFormulaProcessStepEntity::getStepId, Comparator.nullsLast(Long::compareTo)))
            .map(entity -> {
                FormulaProcessStepDTO dto = new FormulaProcessStepDTO();
                BeanUtil.copyProperties(entity, dto);
                return dto;
            })
            .toList();
    }

}
