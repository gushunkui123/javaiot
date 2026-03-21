package com.agileboot.infrastructure.machine.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * 工单写入请求（addWorkOrder / updateWorkOrder / deleteWorkOrder）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScaleWorkOrderRequest {

    private String option;
    private String plant;
    private Integer machineId;
    private String workOrderNo;
    private String workOrderDate;
    private String formulaCode;
    private Integer batch;
    private String lineNo;
}
