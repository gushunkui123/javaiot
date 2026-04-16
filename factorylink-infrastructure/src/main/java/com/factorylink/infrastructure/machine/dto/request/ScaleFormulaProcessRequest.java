package com.factorylink.infrastructure.machine.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * 配方工艺写入请求（updateFormulaProcess）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScaleFormulaProcessRequest {

    private String option;
    private String plant;
    private Integer machineId;
    private String formulaCode;

    @JsonProperty("FormulaProcessEntryList")
    private List<ProcessEntry> formulaProcessEntryList;

    @Data
    public static class ProcessEntry {

        @JsonProperty("StepNo")
        private Integer stepNo;

        @JsonProperty("ActionID")
        private Integer actionId;

        @JsonProperty("MixingTime")
        private BigDecimal mixingTime;

        @JsonProperty("MixingCurrent")
        private BigDecimal mixingCurrent;

        @JsonProperty("MixingTemp")
        private BigDecimal mixingTemp;

        @JsonProperty("RotateSpeed")
        private BigDecimal rotateSpeed;

        @JsonProperty("Pressure")
        private BigDecimal pressure;

        @JsonProperty("ClosingConditionID")
        private Integer closingConditionId;

        @JsonProperty("TurningTimes")
        private Integer turningTimes;

        @JsonProperty("RisingTime")
        private BigDecimal risingTime;

        @JsonProperty("FallingTime")
        private BigDecimal fallingTime;
    }
}
