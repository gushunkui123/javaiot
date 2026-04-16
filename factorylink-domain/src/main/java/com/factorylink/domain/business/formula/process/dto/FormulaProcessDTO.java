package com.factorylink.domain.business.formula.process.dto;

import cn.hutool.core.bean.BeanUtil;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessEntity;
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
public class FormulaProcessDTO implements AuditableDTO {

    public FormulaProcessDTO(BizFormulaProcessEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long processId;

    private String processCode;

    private String processName;

    private Long creatorId;

    private String creatorName;

    private Date createTime;

    private Long updaterId;

    private String updaterName;

    private Date updateTime;

    private List<FormulaProcessStepDTO> steps;

}
