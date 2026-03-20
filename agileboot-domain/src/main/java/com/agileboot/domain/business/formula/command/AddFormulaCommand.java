package com.agileboot.domain.business.formula.command;

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
@Schema(name = "新增配方命令")
public class AddFormulaCommand {

    @Schema(description = "配方编号")
    @NotBlank(message = "配方编号不能为空")
    @Size(max = 50, message = "配方编号长度不能超过50个字符")
    protected String formulaCode;

    @Schema(description = "配方名称")
    @NotBlank(message = "配方名称不能为空")
    @Size(max = 50, message = "配方名称长度不能超过50个字符")
    protected String formulaName;

    @Schema(description = "日期")
    protected String formulaDate;

    @Schema(description = "模具代号")
    protected String moldCode;

    @Schema(description = "批次")
    protected String batch;

    @Schema(description = "手数")
    protected String batchCount;

    @Schema(description = "生产订单号")
    protected String orderNo;

    @Schema(description = "配方明细列表")
    @NotEmpty(message = "配方明细不能为空")
    @Valid
    protected List<FormulaItemCommand> items;

}
