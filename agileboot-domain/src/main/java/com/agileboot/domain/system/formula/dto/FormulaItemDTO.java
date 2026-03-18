package com.agileboot.domain.system.formula.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@Data
@NoArgsConstructor
public class FormulaItemDTO {

    private Long itemId;

    private Long materialId;

    private BigDecimal materialWeight;

    private Integer stepNo;

    private Integer sortOrder;

}
