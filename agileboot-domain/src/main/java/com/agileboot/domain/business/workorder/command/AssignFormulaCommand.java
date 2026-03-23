package com.agileboot.domain.business.workorder.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author Codex
 */
@Data
@Schema(name = "分配配方命令")
public class AssignFormulaCommand {

    @Schema(description = "工单ID")
    @NotNull(message = "工单ID不能为空")
    @Positive(message = "工单ID必须为正数")
    private Long workOrderId;

    @Schema(description = "配方ID")
    @NotNull(message = "配方ID不能为空")
    @Positive(message = "配方ID必须为正数")
    private Long formulaId;

    @Schema(description = "配方编号")
    @NotBlank(message = "配方编号不能为空")
    @Size(max = 50, message = "配方编号长度不能超过50个字符")
    private String formulaCode;

}
