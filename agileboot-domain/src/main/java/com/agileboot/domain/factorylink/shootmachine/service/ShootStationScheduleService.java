package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ShootStationScheduleService extends IService<ShootStationScheduleEntity> {

    List<ShootStationScheduleEntity> listByStationId(Long stationId);

    List<ShootStationScheduleEntity> listCurrentByMachineId(Long machineId);

    ShootStationScheduleEntity getByIdOrThrow(Long id);

    ShootStationScheduleEntity create(ShootStationScheduleEntity entity);

    ShootStationScheduleEntity update(Long id, ShootStationScheduleEntity entity);

    void delete(Long id);

    void cancel(Long id);
}
