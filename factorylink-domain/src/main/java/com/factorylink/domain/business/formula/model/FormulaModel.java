package com.factorylink.domain.business.formula.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.formula.command.AddFormulaCommand;
import com.factorylink.domain.business.formula.command.FormulaItemCommand;
import com.factorylink.domain.business.formula.command.UpdateFormulaCommand;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class FormulaModel extends BizFormulaEntity {

    private List<FormulaItemCommand> items;

    private BizFormulaService formulaService;
    private BizFormulaItemService formulaItemService;
    private BizMaterialService materialService;

    public FormulaModel(BizFormulaService formulaService, BizFormulaItemService formulaItemService,
            BizMaterialService materialService) {
        this.formulaService = formulaService;
        this.formulaItemService = formulaItemService;
        this.materialService = materialService;
    }

    public FormulaModel(BizFormulaEntity entity, BizFormulaService formulaService,
            BizFormulaItemService formulaItemService, BizMaterialService materialService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.formulaService = formulaService;
        this.formulaItemService = formulaItemService;
        this.materialService = materialService;
    }

    public void loadFromAddCommand(AddFormulaCommand addCommand) {
        if (addCommand != null) {
            BeanUtil.copyProperties(addCommand, this, "formulaId");
            setFormulaCode(StrUtil.trim(getFormulaCode()));
            setFormulaName(StrUtil.trim(getFormulaName()));
            this.items = addCommand.getItems();
        }
    }

    public void loadFromUpdateCommand(UpdateFormulaCommand updateCommand) {
        if (updateCommand != null) {
            loadFromAddCommand(updateCommand);
        }
    }

    public void checkFormulaCodeUnique() {
        if (formulaService.isFormulaCodeDuplicated(getFormulaId(), getFormulaCode())) {
            throw new ApiException(Business.FORMULA_CODE_IS_NOT_UNIQUE, getFormulaCode());
        }
    }

    public void checkMaterialsExist() {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> materialIds = items.stream()
            .map(FormulaItemCommand::getMaterialId)
            .collect(Collectors.toSet());
        Set<Long> existingIds = materialService.listByIds(materialIds).stream()
            .map(m -> m.getMaterialId())
            .collect(Collectors.toSet());
        for (Long materialId : materialIds) {
            if (!existingIds.contains(materialId)) {
                throw new ApiException(Business.FORMULA_MATERIAL_NOT_FOUND, materialId);
            }
        }
    }

    @Override
    public boolean insert() {
        super.insert();
        return saveItems();
    }

    @Override
    public boolean updateById() {
        cleanOldItems();
        saveItems();
        return super.updateById();
    }

    @Override
    public boolean deleteById() {
        cleanOldItems();
        formulaService.deleteHistoryByFormulaCode(getFormulaCode());
        return super.deleteById();
    }

    private void cleanOldItems() {
        LambdaQueryWrapper<BizFormulaItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizFormulaItemEntity::getFormulaId, getFormulaId());
        formulaItemService.remove(queryWrapper);
    }

    private boolean saveItems() {
        List<BizFormulaItemEntity> list = new ArrayList<>();
        if (items != null) {
            for (FormulaItemCommand item : items) {
                BizFormulaItemEntity entity = new BizFormulaItemEntity();
                entity.setFormulaId(getFormulaId());
                entity.setMaterialId(item.getMaterialId());
                entity.setMaterialWeight(item.getMaterialWeight());
                entity.setRatio(item.getRatio());
                entity.setWeightUnit(item.getWeightUnit() != null ? item.getWeightUnit() : "g");
                entity.setStepNo(item.getStepNo());
                entity.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0);
                list.add(entity);
            }
            return formulaItemService.saveBatch(list);
        }
        return false;
    }

}
