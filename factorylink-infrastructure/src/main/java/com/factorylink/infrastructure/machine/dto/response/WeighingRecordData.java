package com.factorylink.infrastructure.machine.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 设备端称量记录数据
 */
@Data
public class WeighingRecordData {

    @JsonProperty("WorkOrderNo")
    private String workOrderNo;

    @JsonProperty("FormulaCode")
    private String formulaCode;

    @JsonProperty("Batch")
    private Integer batch;

    @JsonProperty("MaterialNo")
    private String materialNo;

    @JsonProperty("WeighingValue")
    private BigDecimal weighingValue;

    @JsonProperty("WeighTime")
    private String weighTime;

    @JsonProperty("IsError")
    private Integer isError;
}
