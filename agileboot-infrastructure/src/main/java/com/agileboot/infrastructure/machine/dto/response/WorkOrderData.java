package com.agileboot.infrastructure.machine.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 设备端工单数据
 */
@Data
public class WorkOrderData {

    @JsonProperty("WorkOrderNo")
    private String workOrderNo;

    @JsonProperty("OrderDate")
    private String orderDate;

    @JsonProperty("LineNo")
    private String lineNo;

    @JsonProperty("FormulaCode")
    private String formulaCode;

    @JsonProperty("OrderBatchNum")
    private Integer orderBatchNum;

    @JsonProperty("OrderWeight")
    private BigDecimal orderWeight;

    @JsonProperty("FinishBatchNum")
    private Integer finishBatchNum;

    @JsonProperty("FinishWeight")
    private BigDecimal finishWeight;

    @JsonProperty("StartTime")
    private String startTime;

    @JsonProperty("FinishTime")
    private String finishTime;

    @JsonProperty("OrderState")
    private Integer orderState;
}
