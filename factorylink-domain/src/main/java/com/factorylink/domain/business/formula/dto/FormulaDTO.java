package com.factorylink.domain.business.formula.dto;

import cn.hutool.core.bean.BeanUtil;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.common.audit.AuditableDTO;
import java.util.Date;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@Data
@NoArgsConstructor
public class FormulaDTO implements AuditableDTO {

    public FormulaDTO(BizFormulaEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long formulaId;

    private String formulaCode;

    private String formulaName;

    private String formulaDate;

    private String moldCode;

    private String batch;

    private String batchCount;

    private String orderNo;

    private Long creatorId;

    private String creatorName;

    private Date createTime;

    private Long updaterId;

    private String updaterName;

    private Date updateTime;

    private List<FormulaItemDTO> items;

}
