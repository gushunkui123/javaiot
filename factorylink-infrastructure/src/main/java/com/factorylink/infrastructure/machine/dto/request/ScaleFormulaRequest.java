package com.factorylink.infrastructure.machine.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 配方写入请求（addFormula / updateFormula / deleteFormula）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScaleFormulaRequest {

    private String option;
    private String plant;
    private Integer machineId;
    private String formulaCode;
    private String formulaName;

    @JsonProperty("FormulaEntryList")
    private List<FormulaEntry> formulaEntryList;

    @Data
    public static class FormulaEntry {

        @JsonProperty("MaterialNo")
        private String materialNo;

        @JsonProperty("MaterialWeight")
        private BigDecimal materialWeight;

        @JsonProperty("StepNo")
        private Integer stepNo;
    }
}
