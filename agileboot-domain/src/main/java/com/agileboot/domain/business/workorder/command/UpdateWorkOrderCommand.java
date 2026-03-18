package com.agileboot.domain.business.workorder.command;

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
@Schema(name = "更新工单命令")
public class UpdateWorkOrderCommand extends AddWorkOrderCommand {

    @Schema(description = "工单ID")
    @NotNull(message = "工单ID不能为空")
    @Positive(message = "工单ID必须为正数")
    private Long workOrderId;

}
