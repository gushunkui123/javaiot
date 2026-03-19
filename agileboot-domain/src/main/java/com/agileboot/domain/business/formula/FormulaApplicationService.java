package com.agileboot.domain.business.formula;

import cn.hutool.core.bean.BeanUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.common.audit.AuditUserEnricher;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.business.formula.command.AddFormulaCommand;
import com.agileboot.domain.business.formula.command.UpdateFormulaCommand;
import com.agileboot.domain.business.formula.db.BizFormulaEntity;
import com.agileboot.domain.business.formula.db.BizFormulaItemEntity;
import com.agileboot.domain.business.formula.db.BizFormulaItemService;
import com.agileboot.domain.business.formula.db.BizFormulaService;
import com.agileboot.domain.business.formula.dto.FormulaDTO;
import com.agileboot.domain.business.formula.dto.FormulaItemDTO;
import com.agileboot.domain.business.formula.model.FormulaModel;
import com.agileboot.domain.business.formula.model.FormulaModelFactory;
import com.agileboot.domain.business.formula.query.FormulaQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class FormulaApplicationService {

    private final FormulaModelFactory formulaModelFactory;

    private final BizFormulaService formulaService;

    private final BizFormulaItemService formulaItemService;

    private final AuditUserEnricher auditUserEnricher;

    public PageDTO<FormulaDTO> getFormulaList(FormulaQuery query) {
        Page<BizFormulaEntity> page = formulaService.page(query.toPage(), query.toQueryWrapper());
        List<FormulaDTO> records = page.getRecords().stream().map(FormulaDTO::new).toList();
        auditUserEnricher.enrich(records);
        return new PageDTO<>(records, page.getTotal());
    }

    public FormulaDTO getFormulaInfo(Long formulaId) {
        FormulaModel model = formulaModelFactory.loadById(formulaId);
        FormulaDTO dto = new FormulaDTO(model);
        auditUserEnricher.enrich(dto);

        LambdaQueryWrapper<BizFormulaItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizFormulaItemEntity::getFormulaId, formulaId)
            .orderByAsc(BizFormulaItemEntity::getSortOrder);
        List<BizFormulaItemEntity> itemEntities = formulaItemService.list(queryWrapper);

        List<FormulaItemDTO> itemDTOs = itemEntities.stream().map(entity -> {
            FormulaItemDTO itemDTO = new FormulaItemDTO();
            BeanUtil.copyProperties(entity, itemDTO);
            return itemDTO;
        }).toList();
        dto.setItems(itemDTOs);

        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFormula(AddFormulaCommand addCommand) {
        FormulaModel formulaModel = formulaModelFactory.create();
        formulaModel.loadFromAddCommand(addCommand);
        formulaModel.checkFormulaCodeUnique();
        formulaModel.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFormula(UpdateFormulaCommand updateCommand) {
        FormulaModel formulaModel = formulaModelFactory.loadById(updateCommand.getFormulaId());
        formulaModel.loadFromUpdateCommand(updateCommand);
        formulaModel.checkFormulaCodeUnique();
        formulaModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFormula(BulkOperationCommand<Long> deleteCommand) {
        for (Long id : deleteCommand.getIds()) {
            FormulaModel formulaModel = formulaModelFactory.loadById(id);
            formulaModel.deleteById();
        }
    }

}
