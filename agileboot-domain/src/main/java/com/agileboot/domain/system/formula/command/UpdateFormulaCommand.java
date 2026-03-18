package com.agileboot.domain.system.formula.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Codex
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "更新配方命令")
public class UpdateFormulaCommand extends AddFormulaCommand {

    @Schema(description = "配方ID")
    @NotNull(message = "配方ID不能为空")
    @Positive(message = "配方ID必须为正数")
    private Long formulaId;

}
