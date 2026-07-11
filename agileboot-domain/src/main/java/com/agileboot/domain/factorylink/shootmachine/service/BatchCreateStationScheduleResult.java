package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * 批量新增结果。部分失败（如冲突）不回滚，成功与失败分别返回。
 */
@Data
public class BatchCreateStationScheduleResult implements Serializable {

    /** 成功创建的计划列表 */
    private List<ShootStationScheduleEntity> successItems;

    /** 失败明细：哪条站位模向、因何原因未创建 */
    private List<Failure> failures;

    @Data
    public static class Failure implements Serializable {
        private Long stationId;
        private String moldSide;
        private String reason;
    }
}
