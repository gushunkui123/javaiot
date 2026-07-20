package com.agileboot.domain.factorylink.plc.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

/** 射出机最新采集时间响应 */
@Data
public class PlcLatestCollectTimeResponse {

    /** 射出机 ID（shoot_machine.id） */
    private Long machineId;

    /** 最新采集时间，取 plc_data_latest.timestamp 的最大值 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectTime;
}
