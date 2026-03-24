package com.factorylink.infrastructure.machine.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 原料基本资料写入请求（addParts / updateParts / deleteParts）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScalePartsRequest {

    private String option;
    private String plant;
    private Integer machineId;
    private String partNo;
    private String partName;
    private String partClass;
    private BigDecimal unitBarcodeWeight;
}
