package com.factorylink.domain.business.formula.dto;

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

    private String materialCode;

    private String materialType;

    private String materialName;

    private Integer weighingMethod;

    private BigDecimal materialWeight;

    private BigDecimal ratio;

    private String weightUnit;

    private Integer stepNo;

    private Integer sortOrder;

}
