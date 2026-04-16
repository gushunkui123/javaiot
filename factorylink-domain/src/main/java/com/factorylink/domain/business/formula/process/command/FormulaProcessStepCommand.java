package com.factorylink.domain.business.formula.process.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

/**
 * @author Codex
 */
@Data
@Schema(name = "工艺步骤命令")
public class FormulaProcessStepCommand {

    @Schema(description = "排序号")
    @NotNull(message = "排序号不能为空")
    @PositiveOrZero(message = "排序号不能为负数")
    private Integer sortOrder;

    @Schema(description = "步骤号")
    @NotNull(message = "步骤号不能为空")
    @PositiveOrZero(message = "步骤号不能为负数")
    private Integer stepNo;

    @Schema(description = "动作ID")
    @NotNull(message = "动作ID不能为空")
    @Positive(message = "动作ID必须为正数")
    private Integer actionId;

    @Schema(description = "混炼时间")
    @NotNull(message = "混炼时间不能为空")
    @PositiveOrZero(message = "混炼时间不能为负数")
    private BigDecimal mixingTime;

    @Schema(description = "混炼电流")
    @NotNull(message = "混炼电流不能为空")
    @PositiveOrZero(message = "混炼电流不能为负数")
    private BigDecimal mixingCurrent;

    @Schema(description = "混炼温度")
    @NotNull(message = "混炼温度不能为空")
    @PositiveOrZero(message = "混炼温度不能为负数")
    private BigDecimal mixingTemp;

    @Schema(description = "转速")
    @NotNull(message = "转速不能为空")
    @PositiveOrZero(message = "转速不能为负数")
    private BigDecimal rotateSpeed;

    @Schema(description = "压力")
    @NotNull(message = "压力不能为空")
    @PositiveOrZero(message = "压力不能为负数")
    private BigDecimal pressure;

    @Schema(description = "结束条件ID")
    @NotNull(message = "结束条件ID不能为空")
    @PositiveOrZero(message = "结束条件ID不能为负数")
    private Integer closingConditionId;

    @Schema(description = "翻转次数")
    @NotNull(message = "翻转次数不能为空")
    @PositiveOrZero(message = "翻转次数不能为负数")
    private Integer turningTimes;

    @Schema(description = "升降时间")
    @NotNull(message = "升降时间不能为空")
    @PositiveOrZero(message = "升降时间不能为负数")
    private BigDecimal risingTime;

    @Schema(description = "落料时间")
    @NotNull(message = "落料时间不能为空")
    @PositiveOrZero(message = "落料时间不能为负数")
    private BigDecimal fallingTime;

}
