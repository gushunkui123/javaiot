package com.agileboot.domain.business.material.command;

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
@Schema(name = "更新原料命令")
public class UpdateMaterialCommand extends AddMaterialCommand {

    @Schema(description = "原料ID")
    @NotNull(message = "原料ID不能为空")
    @Positive(message = "原料ID必须为正数")
    private Long materialId;

}
