package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDateTime;
import java.util.List;

public interface ShootStationScheduleService extends IService<ShootStationScheduleEntity> {

    /**
     * 查询机台当前在产排期中，各站位的左模/右模生产产品型号。
     *
     * @param machineId 机台ID
     * @return 按站位聚合的左右模在产型号列表
     */
    List<StationMoldModelResponse> listStationMoldModels(Long machineId);

    List<ShootStationScheduleEntity> listByStationId(Long stationId, LocalDateTime startDate, LocalDateTime endDate, String moldSide);

    List<ShootStationScheduleEntity> listCurrentByMachineId(Long machineId);

    ShootStationScheduleEntity getByIdOrThrow(Long id);

    ShootStationScheduleEntity create(ShootStationScheduleEntity entity);

    BatchCreateStationScheduleResult batchCreate(BatchCreateStationScheduleRequest request);

    ShootStationScheduleEntity update(Long id, ShootStationScheduleEntity entity);

    void delete(Long id);

    void cancel(Long id);
}
