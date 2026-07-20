package com.agileboot.domain.factorylink.shootmachine.service;

import lombok.Data;

/**
 * 机台当前在产排期中，各站位的左模/右模生产产品型号。
 * 数据来自 shoot_station_schedule 当前在产记录 JOIN shoot_mold 的 mold_model/color。
 */
@Data
public class StationMoldModelResponse {

    /** 站位号 */
    private Integer stationNo;

    /** 站位名称 */
    private String stationName;

    /** 左模在产产品型号，无在产排期为 null */
    private String leftMoldModel;

    /** 右模在产产品型号，无在产排期为 null */
    private String rightMoldModel;
}
