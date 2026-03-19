package com.agileboot.domain.business.workorder.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agileboot.common.annotation.ExcelColumn;
import com.agileboot.common.annotation.ExcelSheet;
import com.agileboot.domain.business.workorder.db.BizWorkOrderEntity;
import com.agileboot.domain.common.audit.AuditableDTO;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@Data
@NoArgsConstructor
@ExcelSheet(name = "工单数据")
public class WorkOrderDTO implements AuditableDTO {

    public WorkOrderDTO(BizWorkOrderEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    @ExcelColumn(name = "工单ID")
    private Long workOrderId;

    @ExcelColumn(name = "工单编号")
    private String workOrderNo;

    @ExcelColumn(name = "订单日期")
    private Date orderDate;

    @ExcelColumn(name = "工厂")
    private String plant;

    @ExcelColumn(name = "设备编号")
    private Integer machineId;

    @ExcelColumn(name = "产线编号")
    private String lineNo;

    @ExcelColumn(name = "配方编号")
    private String formulaCode;

    @ExcelColumn(name = "计划批次数")
    private Integer orderBatchNum;

    @ExcelColumn(name = "计划重量")
    private BigDecimal orderWeight;

    @ExcelColumn(name = "完成批次数")
    private Integer finishBatchNum;

    @ExcelColumn(name = "完成重量")
    private BigDecimal finishWeight;

    @ExcelColumn(name = "开始时间")
    private Date startTime;

    @ExcelColumn(name = "完成时间")
    private Date finishTime;

    @ExcelColumn(name = "工单状态")
    private Integer orderState;

    private Long creatorId;

    @ExcelColumn(name = "创建人")
    private String creatorName;

    @ExcelColumn(name = "创建时间")
    private Date createTime;

    private Long updaterId;

    @ExcelColumn(name = "更新人")
    private String updaterName;

    @ExcelColumn(name = "更新时间")
    private Date updateTime;

}
