package com.agileboot.domain.business.workorder.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agileboot.domain.business.workorder.db.BizWorkOrderEntity;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@Data
@NoArgsConstructor
public class WorkOrderDTO {

    public WorkOrderDTO(BizWorkOrderEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long workOrderId;

    private String workOrderNo;

    private Date orderDate;

    private String plant;

    private Integer machineId;

    private String lineNo;

    private String formulaCode;

    private Integer orderBatchNum;

    private BigDecimal orderWeight;

    private Integer finishBatchNum;

    private BigDecimal finishWeight;

    private Date startTime;

    private Date finishTime;

    private Integer orderState;

    private Long creatorId;

    private Date createTime;

    private Long updaterId;

    private Date updateTime;

}
