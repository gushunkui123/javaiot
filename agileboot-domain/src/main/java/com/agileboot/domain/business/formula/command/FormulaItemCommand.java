package com.agileboot.domain.business.formula.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

/**
 * @author Codex
 */
@Data
@Schema(name = "配方明细行命令")
public class FormulaItemCommand {

    @Schema(description = "原料ID")
    @NotNull(message = "原料ID不能为空")
    @Positive(message = "原料ID必须为正数")
    private Long materialId;

    @Schema(description = "原料重量(kg)")
    @NotNull(message = "原料重量不能为空")
    @Positive(message = "原料重量必须为正数")
    private BigDecimal materialWeight;

    @Schema(description = "比率")
    private BigDecimal ratio;

    @Schema(description = "重量单位")
    private String weightUnit;

    @Schema(description = "下料段序")
    private Integer stepNo;

    @Schema(description = "排序号")
    private Integer sortOrder;

}
