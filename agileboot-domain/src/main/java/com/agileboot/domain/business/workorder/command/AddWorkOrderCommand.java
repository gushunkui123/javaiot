package com.agileboot.domain.business.workorder.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @author Codex
 */
@Data
@Schema(name = "新增工单命令")
public class AddWorkOrderCommand {

    @Schema(description = "工单编号")
    @NotBlank(message = "工单编号不能为空")
    @Size(max = 50, message = "工单编号长度不能超过50个字符")
    protected String workOrderNo;

    @Schema(description = "工单日期")
    @NotNull(message = "工单日期不能为空")
    protected Date orderDate;

    @Schema(description = "工厂别")
    @Size(max = 50, message = "工厂别长度不能超过50个字符")
    protected String plant;

    @Schema(description = "设备编号")
    @NotNull(message = "设备编号不能为空")
    @Positive(message = "设备编号必须为正数")
    protected Integer machineId;

    @Schema(description = "产线编号(A/B)")
    @Size(max = 10, message = "产线编号长度不能超过10个字符")
    protected String lineNo;

    @Schema(description = "配方编号")
    @NotBlank(message = "配方编号不能为空")
    @Size(max = 50, message = "配方编号长度不能超过50个字符")
    protected String formulaCode;

    @Schema(description = "计划批次数")
    @NotNull(message = "计划批次数不能为空")
    @Positive(message = "计划批次数必须为正数")
    protected Integer orderBatchNum;

    @Schema(description = "工单总重(kg)")
    protected BigDecimal orderWeight;

}
