package com.agileboot.domain.business.formula.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agileboot.domain.business.formula.db.BizFormulaEntity;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@Data
@NoArgsConstructor
public class FormulaDTO {

    public FormulaDTO(BizFormulaEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long formulaId;

    private String formulaCode;

    private String formulaName;

    private Long creatorId;

    private Date createTime;

    private Long updaterId;

    private Date updateTime;

    private List<FormulaItemDTO> items;

}
