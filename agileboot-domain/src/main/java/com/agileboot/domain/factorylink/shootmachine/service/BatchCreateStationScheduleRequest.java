package com.agileboot.domain.factorylink.shootmachine.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 批量新增模具投产计划请求。
 * 共用一个模具与时间段，套到多个「站位 + 模向(左/右)」组合上，生成多条计划。
 */
@Data
public class BatchCreateStationScheduleRequest implements Serializable {

    private Long moldId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String remark;

    /** 站位模向组合：每个元素 = 一个站位 + 一个模向(LEFT/RIGHT) */
    private List<Item> items;

    @Data
    public static class Item implements Serializable {
        private Long stationId;
        private String moldSide;
        /** 射枪编号（计划级选枪，1枪/2枪...），用于射枪温度等按枪比较 */
        private Integer gunNo;
    }
}
