package com.agileboot.domain.factorylink.plc.dto;

import lombok.Data;

/** 下发给第三方的单条告警规则：dataCode（第三方唯一点位码）+ 阈值边界 */
@Data
public class RulePushItem {

    /** 第三方唯一点位码（来自 plc_data_latest.data_code） */
    private String dataCode;

    /** 上限阈值（第三方契约要求字符串） */
    private String max;

    /** 下限阈值（第三方契约要求字符串） */
    private String min;
}
