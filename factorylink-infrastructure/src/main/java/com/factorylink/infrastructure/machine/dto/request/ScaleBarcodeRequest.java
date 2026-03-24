package com.factorylink.infrastructure.machine.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 原料条码写入请求（addMaterialBarcode / updateMaterialBarcode / deleteMaterialBarcode）
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScaleBarcodeRequest {

    private String option;
    private String plant;
    private Integer machineId;
    private String barcode;
    private String materialNo;
    private BigDecimal unitBarcodeWeight;
    private String expirationDate;
    private String lotNo;
}
