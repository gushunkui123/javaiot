package com.factorylink.domain.business.workorder.dto;

import cn.hutool.core.bean.BeanUtil;
import com.factorylink.common.annotation.ExcelColumn;
import com.factorylink.common.annotation.ExcelSheet;
import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.factorylink.domain.common.audit.AuditableDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "WorkOrderDTO", description = "工单信息")
public class WorkOrderDTO implements AuditableDTO {

    public WorkOrderDTO(BizWorkOrderEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    @Schema(description = "工单ID")
    @ExcelColumn(name = "工单ID")
    private Long workOrderId;

    @Schema(description = "工单编号")
    @ExcelColumn(name = "工单编号")
    private String workOrderNo;

    @Schema(description = "订单日期")
    @ExcelColumn(name = "订单日期")
    private Date orderDate;

    @Schema(description = "工厂别")
    @ExcelColumn(name = "工厂")
    private String plant;

    @Schema(description = "设备编号")
    @ExcelColumn(name = "设备编号")
    private Integer machineId;

    @Schema(description = "产线编号(A/B)")
    @ExcelColumn(name = "产线编号")
    private String lineNo;

    @Schema(description = "配方编号")
    @ExcelColumn(name = "配方编号")
    private String formulaCode;

    @Schema(description = "模具代号")
    @ExcelColumn(name = "模具代号")
    private String moldCode;

    @Schema(description = "型体颜色")
    @ExcelColumn(name = "型体颜色")
    private String modelColor;

    @Schema(description = "单批次重量(kg)")
    @ExcelColumn(name = "单批次重量")
    private BigDecimal batchWeight;

    @Schema(description = "配方ID")
    @ExcelColumn(name = "配方ID")
    private Long formulaId;

    @Schema(description = "计划批次数")
    @ExcelColumn(name = "计划批次数")
    private Integer orderBatchNum;

    @Schema(description = "计划重量(kg)，等于单批次重量×计划批次数")
    @ExcelColumn(name = "计划重量")
    private BigDecimal orderWeight;

    @Schema(description = "完工批次数")
    @ExcelColumn(name = "完成批次数")
    private Integer finishBatchNum;

    @Schema(description = "完工重量(kg)")
    @ExcelColumn(name = "完成重量")
    private BigDecimal finishWeight;

    @Schema(description = "生产开始时间")
    @ExcelColumn(name = "开始时间")
    private Date startTime;

    @Schema(description = "生产结束时间")
    @ExcelColumn(name = "完成时间")
    private Date finishTime;

    @Schema(description = "工单状态: 1-未生产, 2-生产中, 3-已完工, 4-已取消")
    @ExcelColumn(name = "工单状态")
    private Integer orderState;

    @Schema(description = "流程状态: 1-已创建, 2-已确认, 3-已添加配方, 4-生产中, 5-已完成, 6-已取消")
    @ExcelColumn(name = "流程状态")
    private Integer processStatus;

    @Schema(description = "备注")
    @ExcelColumn(name = "备注")
    private String remark;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建人姓名")
    @ExcelColumn(name = "创建人")
    private String creatorName;

    @Schema(description = "创建时间")
    @ExcelColumn(name = "创建时间")
    private Date createTime;

    @Schema(description = "更新人ID")
    private Long updaterId;

    @Schema(description = "更新人姓名")
    @ExcelColumn(name = "更新人")
    private String updaterName;

    @Schema(description = "更新时间")
    @ExcelColumn(name = "更新时间")
    private Date updateTime;

}
