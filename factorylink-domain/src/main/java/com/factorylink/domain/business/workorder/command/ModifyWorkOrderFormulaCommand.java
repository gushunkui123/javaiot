package com.factorylink.domain.business.workorder.command;

import com.factorylink.domain.business.formula.command.FormulaItemCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
@Schema(name = "工单配方修改命令")
public class ModifyWorkOrderFormulaCommand {

    @Schema(description = "工单ID", hidden = true)
    private Long workOrderId;

    @Schema(description = "配方明细列表")
    @NotNull(message = "配方明细不能为空")
    @NotEmpty(message = "配方明细不能为空")
    @Valid
    private List<FormulaItemCommand> items;

    @Schema(description = "生产中工单修改确认标识，生产中修改时必须传true")
    private Boolean confirmed;
}
