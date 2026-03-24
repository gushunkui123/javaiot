package com.factorylink.infrastructure.machine.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 设备端报警记录数据
 */
@Data
public class AlarmLogData {

    @JsonProperty("AlarmGroup")
    private String alarmGroup;

    @JsonProperty("AlarmGroupText")
    private String alarmGroupText;

    @JsonProperty("AlarmCode")
    private String alarmCode;

    @JsonProperty("AlarmMsg")
    private String alarmMsg;

    @JsonProperty("AlarmTime")
    private String alarmTime;

    @JsonProperty("WorkOrderNo")
    private String workOrderNo;

    @JsonProperty("Batch")
    private Integer batch;
}
