package com.factorylink.domain.business.formula.process.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.formula.process.command.AddFormulaProcessCommand;
import com.factorylink.domain.business.formula.process.command.FormulaProcessStepCommand;
import com.factorylink.domain.business.formula.process.command.UpdateFormulaProcessCommand;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessService;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepEntity;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepService;
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
public class FormulaProcessModel extends BizFormulaProcessEntity {

    private List<FormulaProcessStepCommand> steps;

    private BizFormulaProcessService formulaProcessService;
    private BizFormulaProcessStepService formulaProcessStepService;

    public FormulaProcessModel(BizFormulaProcessService formulaProcessService,
            BizFormulaProcessStepService formulaProcessStepService) {
        this.formulaProcessService = formulaProcessService;
        this.formulaProcessStepService = formulaProcessStepService;
    }

    public FormulaProcessModel(BizFormulaProcessEntity entity, BizFormulaProcessService formulaProcessService,
            BizFormulaProcessStepService formulaProcessStepService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.formulaProcessService = formulaProcessService;
        this.formulaProcessStepService = formulaProcessStepService;
    }

    public void loadFromAddCommand(AddFormulaProcessCommand addCommand) {
        if (addCommand != null) {
            BeanUtil.copyProperties(addCommand, this, "processId");
            setProcessCode(StrUtil.trim(getProcessCode()));
            setProcessName(StrUtil.trim(getProcessName()));
            this.steps = addCommand.getSteps();
        }
    }

    public void loadFromUpdateCommand(UpdateFormulaProcessCommand updateCommand) {
        if (updateCommand != null) {
            loadFromAddCommand(updateCommand);
        }
    }

    public void checkProcessCodeUnique() {
        if (formulaProcessService.isProcessCodeDuplicated(getProcessId(), getProcessCode())) {
            throw new ApiException(Business.FORMULA_PROCESS_CODE_IS_NOT_UNIQUE, getProcessCode());
        }
    }

    @Override
    public boolean insert() {
        super.insert();
        return saveSteps();
    }

    @Override
    public boolean updateById() {
        cleanOldSteps();
        saveSteps();
        return super.updateById();
    }

    @Override
    public boolean deleteById() {
        cleanOldSteps();
        formulaProcessService.deleteHistoryByProcessCode(getProcessCode());
        return super.deleteById();
    }

    private void cleanOldSteps() {
        LambdaQueryWrapper<BizFormulaProcessStepEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizFormulaProcessStepEntity::getProcessId, getProcessId());
        formulaProcessStepService.remove(queryWrapper);
    }

    private boolean saveSteps() {
        List<BizFormulaProcessStepEntity> entities = new ArrayList<>();
        if (steps != null) {
            int index = 1;
            for (FormulaProcessStepCommand step : steps) {
                BizFormulaProcessStepEntity entity = new BizFormulaProcessStepEntity();
                entity.setProcessId(getProcessId());
                entity.setSortOrder(step.getSortOrder() != null ? step.getSortOrder() : index);
                entity.setStepNo(step.getStepNo());
                entity.setActionId(step.getActionId());
                entity.setMixingTime(step.getMixingTime());
                entity.setMixingCurrent(step.getMixingCurrent());
                entity.setMixingTemp(step.getMixingTemp());
                entity.setRotateSpeed(step.getRotateSpeed());
                entity.setPressure(step.getPressure());
                entity.setClosingConditionId(step.getClosingConditionId());
                entity.setTurningTimes(step.getTurningTimes());
                entity.setRisingTime(step.getRisingTime());
                entity.setFallingTime(step.getFallingTime());
                entities.add(entity);
                index++;
            }
            return formulaProcessStepService.saveBatch(entities);
        }
        return false;
    }

}
