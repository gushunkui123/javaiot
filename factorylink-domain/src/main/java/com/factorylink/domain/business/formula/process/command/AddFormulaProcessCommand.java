package com.factorylink.domain.business.formula.process.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

/**
 * @author Codex
 */
@Data
@Schema(name = "新增工艺命令")
public class AddFormulaProcessCommand {

    @Schema(description = "工艺编号")
    @NotBlank(message = "工艺编号不能为空")
    @Size(max = 50, message = "工艺编号长度不能超过50个字符")
    protected String processCode;

    @Schema(description = "工艺名称")
    @NotBlank(message = "工艺名称不能为空")
    @Size(max = 50, message = "工艺名称长度不能超过50个字符")
    protected String processName;

    @Schema(description = "工艺步骤列表")
    @NotEmpty(message = "工艺步骤不能为空")
    @Valid
    protected List<FormulaProcessStepCommand> steps;

}
