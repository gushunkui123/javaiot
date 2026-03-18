package com.agileboot.domain.system.formula.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.system.formula.command.AddFormulaCommand;
import com.agileboot.domain.system.formula.command.FormulaItemCommand;
import com.agileboot.domain.system.formula.command.UpdateFormulaCommand;
import com.agileboot.domain.system.formula.db.SysFormulaEntity;
import com.agileboot.domain.system.formula.db.SysFormulaItemEntity;
import com.agileboot.domain.system.formula.db.SysFormulaItemService;
import com.agileboot.domain.system.formula.db.SysFormulaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class FormulaModel extends SysFormulaEntity {

    private List<FormulaItemCommand> items;

    private SysFormulaService formulaService;
    private SysFormulaItemService formulaItemService;

    public FormulaModel(SysFormulaService formulaService, SysFormulaItemService formulaItemService) {
        this.formulaService = formulaService;
        this.formulaItemService = formulaItemService;
    }

    public FormulaModel(SysFormulaEntity entity, SysFormulaService formulaService, SysFormulaItemService formulaItemService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.formulaService = formulaService;
        this.formulaItemService = formulaItemService;
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
        return super.deleteById();
    }

    private void cleanOldItems() {
        LambdaQueryWrapper<SysFormulaItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysFormulaItemEntity::getFormulaId, getFormulaId());
        formulaItemService.remove(queryWrapper);
    }

    private boolean saveItems() {
        List<SysFormulaItemEntity> list = new ArrayList<>();
        if (items != null) {
            for (FormulaItemCommand item : items) {
                SysFormulaItemEntity entity = new SysFormulaItemEntity();
                entity.setFormulaId(getFormulaId());
                entity.setMaterialId(item.getMaterialId());
                entity.setMaterialWeight(item.getMaterialWeight());
                entity.setStepNo(item.getStepNo());
                entity.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : 0);
                list.add(entity);
            }
            return formulaItemService.saveBatch(list);
        }
        return false;
    }

}
