package com.factorylink.domain.business.formula.process.dto;

import cn.hutool.core.bean.BeanUtil;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessStepEntity;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@Data
@NoArgsConstructor
public class FormulaProcessStepDTO {

    public FormulaProcessStepDTO(BizFormulaProcessStepEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long stepId;

    private Long processId;

    private Integer sortOrder;

    private Integer stepNo;

    private Integer actionId;

    private BigDecimal mixingTime;

    private BigDecimal mixingCurrent;

    private BigDecimal mixingTemp;

    private BigDecimal rotateSpeed;

    private BigDecimal pressure;

    private Integer closingConditionId;

    private Integer turningTimes;

    private BigDecimal risingTime;

    private BigDecimal fallingTime;

}
