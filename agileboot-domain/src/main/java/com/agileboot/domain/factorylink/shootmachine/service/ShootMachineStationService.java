package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ShootMachineStationService extends IService<ShootMachineStationEntity> {

    // 查询机台下的站位列表
    List<ShootMachineStationEntity> listByMachineId(Long machineId);

    // 从 PLC 最新数据同步站位，返回新增的站位数量
    int syncFromPlc(Long machineId);


}
