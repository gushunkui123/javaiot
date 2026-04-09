package com.factorylink.domain.business.machine.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新设备命令
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "更新设备命令")
public class UpdateMachineCommand extends AddMachineCommand {

    @Schema(description = "设备ID")
    @NotNull(message = "设备ID不能为空")
    @Positive(message = "设备ID必须为正数")
    private Long machineId;

}
