package com.agileboot.domain.factorylink.shootmachine.service.impl;

import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineStationMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineStationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
        return listStationsByMachineId(machineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromPlc(Long machineId) {
        shootMachineService.getByIdOrThrow(machineId);
        // 查询该机台最新的 PLC 数据
        List<String> fieldKeys = plcDataService.listLatestSameTimestampByMachineId(machineId).stream()
                .map(row -> row.getFieldKey())
                .toList();
        // 插入缺失的站位,用工具获取站位
        return insertMissingStations(machineId, PlcFieldKeyDisplayNames.parseDistinctStationNos(fieldKeys));
    }

    // 查询机台站位
    private List<ShootMachineStationEntity> listStationsByMachineId(Long machineId) {
        return lambdaQuery()
                .eq(ShootMachineStationEntity::getMachineId, machineId)
                .orderByAsc(ShootMachineStationEntity::getStationNo)
                .list();
    }

    // 插入缺失的站位，返回新增数量
    private int insertMissingStations(Long machineId, Set<Integer> stationNos) {
        if (stationNos.isEmpty()) {
            return 0;
        }
        
        // 获取机台名称
        String machineName = shootMachineService.getByIdOrThrow(machineId).getMachineName();
        
        // 查询已存在的站位号
        Set<Integer> existingNos = lambdaQuery()
                .eq(ShootMachineStationEntity::getMachineId, machineId)
                .in(ShootMachineStationEntity::getStationNo, stationNos)
                .list()
                .stream()
                .map(ShootMachineStationEntity::getStationNo)
                .collect(Collectors.toSet());
        
        // 过滤出需要新增的站位
        Set<Integer> toInsert = stationNos.stream()
                .filter(no -> !existingNos.contains(no))
                .collect(Collectors.toSet());
        
        if (toInsert.isEmpty()) {
            return 0;
        }
        
        // 批量插入
        List<ShootMachineStationEntity> newStations = toInsert.stream()
                .map(no -> createStation(machineId, machineName, no))
                .toList();
        saveBatch(newStations);
        
        return newStations.size();
    }
    private ShootMachineStationEntity createStation(Long machineId, String machineName, Integer stationNo) {
        ShootMachineStationEntity station = new ShootMachineStationEntity();
        station.setMachineId(machineId);
        station.setStationNo(stationNo);
        // 站位名称
        station.setStationName(machineName + "-站位" + stationNo);
        station.setEnabled(true);
        station.setDeleted(false);
        return station;
    }
}
