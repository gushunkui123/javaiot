package com.factorylink.infrastructure.machine.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 设备端原料耗用数据
 */
@Data
public class MaterialConsumptionData {

    @JsonProperty("ConsumptionTime")
    private String consumptionTime;

    @JsonProperty("MaterialNo")
    private String materialNo;

    @JsonProperty("WeighingValue")
    private BigDecimal weighingValue;
}
