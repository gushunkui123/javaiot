package com.factorylink.domain.business.workorder.db;

import com.factorylink.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 工单信息表
 *
 * @author Codex
 */
@Getter
@Setter
@TableName("biz_work_order")
@Schema(name = "BizWorkOrderEntity对象", description = "工单信息表")
public class BizWorkOrderEntity extends BaseEntity<BizWorkOrderEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "工单ID")
    @TableId(value = "work_order_id", type = IdType.AUTO)
    private Long workOrderId;

    @Schema(description = "工单编号")
    @TableField("work_order_no")
    private String workOrderNo;

    @Schema(description = "工单日期")
    @TableField("order_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date orderDate;

    @Schema(description = "工厂别")
    @TableField("plant")
    private String plant;

    @Schema(description = "设备编号")
    @TableField("machine_id")
    private Integer machineId;

    @Schema(description = "产线编号(A/B)")
    @TableField("line_no")
    private String lineNo;

    @Schema(description = "配方编号")
    @TableField("formula_code")
    private String formulaCode;

    @Schema(description = "模具代号")
    @TableField("mold_code")
    private String moldCode;

    @Schema(description = "型体颜色")
    @TableField("model_color")
    private String modelColor;

    @Schema(description = "单批次重量(kg)")
    @TableField("batch_weight")
    private BigDecimal batchWeight;

    @Schema(description = "配方ID")
    @TableField("formula_id")
    private Long formulaId;

    @Schema(description = "计划批次数")
    @TableField("order_batch_num")
    private Integer orderBatchNum;

    @Schema(description = "工单总重(kg)")
    @TableField("order_weight")
    private BigDecimal orderWeight;

    @Schema(description = "完工批次数")
    @TableField("finish_batch_num")
    private Integer finishBatchNum;

    @Schema(description = "完工重量(kg)")
    @TableField("finish_weight")
    private BigDecimal finishWeight;

    @Schema(description = "生产开始时间")
    @TableField("start_time")
    private Date startTime;

    @Schema(description = "生产结束时间")
    @TableField("finish_time")
    private Date finishTime;

    @Schema(description = "工单状态: 1-未生产, 2-生产中, 3-已完工, 4-已取消")
    @TableField("order_state")
    private Integer orderState;

    @Schema(description = "流程状态: 1-已创建, 2-已添加配方, 3-生产中, 4-已完成, 5-已取消")
    @TableField("process_status")
    private Integer processStatus;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Override
    public Serializable pkVal() {
        return this.workOrderId;
    }

}
