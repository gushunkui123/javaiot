package com.factorylink.domain.business.formula.process.command;

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
@Schema(name = "更新工艺命令")
public class UpdateFormulaProcessCommand extends AddFormulaProcessCommand {

    @Schema(description = "工艺ID")
    @NotNull(message = "工艺ID不能为空")
    @Positive(message = "工艺ID必须为正数")
    private Long processId;

}
