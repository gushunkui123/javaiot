package com.agileboot.domain.factorylink.plc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 * 下发给第三方（安冬硬件平台）的单台设备告警规则报文：设备编码 + 模具 + 该模具的规则列表。
 * <p>
 * 字段名与第三方契约对齐：模具ID 使用下划线命名 {@code mold_id}。
 */
@Data
public class MoldRulePushDTO {

    /** 设备编码（来自 plc_data_latest.device_code，即 plc_machine_data_config.data_code） */
    private String deviceCode;

    /** 模具ID（第三方契约要求下划线命名） */
    @JsonProperty("mold_id")
    private String moldId;

    /** 规则列表（datacode + 阈值边界） */
    private List<RulePushItem> rules;
}
