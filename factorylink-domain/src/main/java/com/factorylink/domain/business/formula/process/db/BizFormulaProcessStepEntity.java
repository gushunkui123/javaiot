package com.factorylink.domain.business.formula.process.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 工艺步骤表
 *
 * @author Codex
 */
@Getter
@Setter
@TableName("biz_formula_process_step")
@Schema(name = "BizFormulaProcessStepEntity对象", description = "工艺步骤表")
public class BizFormulaProcessStepEntity extends Model<BizFormulaProcessStepEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "步骤ID")
    @TableId(value = "step_id", type = IdType.AUTO)
    private Long stepId;

    @Schema(description = "工艺ID")
    @TableField("process_id")
    private Long processId;

    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "步骤号")
    @TableField("step_no")
    private Integer stepNo;

    @Schema(description = "动作ID")
    @TableField("action_id")
    private Integer actionId;

    @Schema(description = "混炼时间")
    @TableField("mixing_time")
    private BigDecimal mixingTime;

    @Schema(description = "混炼电流")
    @TableField("mixing_current")
    private BigDecimal mixingCurrent;

    @Schema(description = "混炼温度")
    @TableField("mixing_temp")
    private BigDecimal mixingTemp;

    @Schema(description = "转速")
    @TableField("rotate_speed")
    private BigDecimal rotateSpeed;

    @Schema(description = "压力")
    @TableField("pressure")
    private BigDecimal pressure;

    @Schema(description = "结束条件ID")
    @TableField("closing_condition_id")
    private Integer closingConditionId;

    @Schema(description = "翻转次数")
    @TableField("turning_times")
    private Integer turningTimes;

    @Schema(description = "升降时间")
    @TableField("rising_time")
    private BigDecimal risingTime;

    @Schema(description = "落料时间")
    @TableField("falling_time")
    private BigDecimal fallingTime;

    @Override
    public Serializable pkVal() {
        return this.stepId;
    }

}
