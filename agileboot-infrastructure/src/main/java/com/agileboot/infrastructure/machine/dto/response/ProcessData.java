package com.agileboot.infrastructure.machine.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 设备端密炼机工艺数据（仅主磅）
 */
@Data
public class ProcessData {

    @JsonProperty("Time")
    private String time;

    @JsonProperty("Pressure")
    private BigDecimal pressure;

    @JsonProperty("Speed")
    private BigDecimal speed;

    @JsonProperty("Temperature")
    private BigDecimal temperature;

    @JsonProperty("Power")
    private BigDecimal power;

    @JsonProperty("Current")
    private BigDecimal current;
}
