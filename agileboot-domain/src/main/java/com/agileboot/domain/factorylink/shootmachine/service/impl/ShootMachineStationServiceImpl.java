package com.agileboot.domain.factorylink.shootmachine.service.impl;


import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;

import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineStationMapper;

import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineStationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShootMachineStationServiceImpl
        extends ServiceImpl<ShootMachineStationMapper, ShootMachineStationEntity>
        implements ShootMachineStationService {


    private final ShootMachineService shootMachineService;
    private final PlcDataService plcDataService;


    @Override
    public List<ShootMachineStationEntity> listByMachineId(Long machineId) {
        shootMachineService.getByIdOrThrow(machineId);
        List<ShootMachineStationEntity> list = lambdaQuery()
                .eq(ShootMachineStationEntity::getMachineId, machineId)
                .orderByAsc(ShootMachineStationEntity::getStationNo)
                .list();
        int gunCount = ShootMachineStationEntity.resolveGunCount(list.size());
        list.forEach(s -> s.setGunCount(gunCount));
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromPlc(Long machineId) {
        ShootMachineEntity machine = shootMachineService.getByIdOrThrow(machineId);
        // 从 PLC 数据中提取站位编号集合
        Set<Integer> stationNos =
                PlcFieldKeyDisplayNames.parseDistinctStationNos(
                        plcDataService.listLatestSameTimestampByMachineId(machineId).stream()
                                .map(PlcDataEntity::getFieldKey)
                                .toList());
        if (stationNos.isEmpty()) {
            return 0;
        }
        // 找出已有站位编号，排除已存在的
        Set<Integer> existing =
                lambdaQuery()
                        .eq(ShootMachineStationEntity::getMachineId, machineId)
                        .in(ShootMachineStationEntity::getStationNo, stationNos)
                        .list()
                        .stream()
                        .map(ShootMachineStationEntity::getStationNo)
                        .collect(Collectors.toSet());
        // 构建新增的站位实体
        List<ShootMachineStationEntity> added =
                stationNos.stream()
                        .filter(no -> !existing.contains(no))
                        .map(no -> buildStation(machine, no))
                        .toList();
        if (!added.isEmpty()) {
            saveBatch(added);
        }
        return added.size();
    }

    private static ShootMachineStationEntity buildStation(ShootMachineEntity machine, int stationNo) {
        ShootMachineStationEntity station = new ShootMachineStationEntity();
        station.setMachineId(machine.getId());
        station.setStationNo(stationNo);
        station.setStationName(machine.getMachineName() + "-站台" + stationNo);
        station.setEnabled(true);
        station.setDeleted(false);
        return station;
    }
}
