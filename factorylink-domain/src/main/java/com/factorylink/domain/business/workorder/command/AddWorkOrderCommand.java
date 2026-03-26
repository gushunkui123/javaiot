package com.factorylink.domain.business.workorder.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * @author Codex
 */
@Data
@Schema(name = "新增工单命令")
public class AddWorkOrderCommand {

    @Schema(description = "工单编号（选填，不传则由系统自动生成）")
    @Size(max = 50, message = "工单编号长度不能超过50个字符")
    protected String workOrderNo;

    @Schema(description = "工单日期（选填，不传则自动取当天日期）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date orderDate;

    @Schema(description = "工厂别（选填）")
    @Size(max = 50, message = "工厂别长度不能超过50个字符")
    protected String plant;

    @Schema(description = "设备编号（选填）")
    @Positive(message = "设备编号必须为正数")
    protected Integer machineId;

    @Schema(description = "产线编号(A/B)（必填）")
    @NotBlank(message = "产线编号不能为空")
    @Size(max = 10, message = "产线编号长度不能超过10个字符")
    protected String lineNo;

    @Schema(description = "配方编号（选填）")
    @Size(max = 50, message = "配方编号长度不能超过50个字符")
    protected String formulaCode;

    @Schema(description = "模具代号（必填）")
    @NotBlank(message = "模具代号不能为空")
    @Size(max = 50, message = "模具代号长度不能超过50个字符")
    protected String moldCode;

    @Schema(description = "型体颜色（必填）")
    @NotBlank(message = "型体颜色不能为空")
    @Size(max = 50, message = "型体颜色长度不能超过50个字符")
    protected String modelColor;

    @Schema(description = "单批次重量kg（选填，默认75kg）")
    protected BigDecimal batchWeight;

    @Schema(description = "配方ID（选填）")
    protected Long formulaId;

    @Schema(description = "计划批次数（必填）")
    @NotNull(message = "计划批次数不能为空")
    @Positive(message = "计划批次数必须为正数")
    protected Integer orderBatchNum;

    @Schema(description = "备注（选填）")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    protected String remark;

}
