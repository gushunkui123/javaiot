package com.agileboot.domain.factorylink.shootmachine.service.impl;


import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;

import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineStationMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootStationScheduleMapper;

import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineStationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final ShootStationScheduleMapper shootStationScheduleMapper;


    @Override
    public List<ShootMachineStationEntity> listByMachineId(Long machineId) {
        shootMachineService.getByIdOrThrow(machineId);
        List<ShootMachineStationEntity> list = lambdaQuery()
                .eq(ShootMachineStationEntity::getMachineId, machineId)
                .orderByAsc(ShootMachineStationEntity::getStationNo)
                .list();
        int gunCount = ShootMachineStationEntity.resolveGunCount(list.size());
        list.forEach(s -> s.setGunCount(gunCount));

        // 填充各站位左右模在产型号（直接查排期 mapper，避免与排期 service 形成循环依赖）
        Map<Integer, String> leftMap = new HashMap<>();
        Map<Integer, String> rightMap = new HashMap<>();
        for (ShootStationScheduleEntity row :
                shootStationScheduleMapper.selectListCurrentByMachineIdWithMold(machineId, LocalDateTime.now())) {
            if ("LEFT".equals(row.getMoldSide())) {
                leftMap.put(row.getStationNo(), row.getMoldModel());
            } else if ("RIGHT".equals(row.getMoldSide())) {
                rightMap.put(row.getStationNo(), row.getMoldModel());
            }
        }
        for (ShootMachineStationEntity s : list) {
            Integer no = s.getStationNo();
            s.setLeftMoldModel(leftMap.get(no));
            s.setRightMoldModel(rightMap.get(no));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncFromPlc(Long machineId) {
        ShootMachineEntity machine = shootMachineService.getByIdOrThrow(machineId);
        // 从 PLC 数据的 categoryName（如"站台9"）中提取站位编号集合
        Set<Integer> stationNos =
                plcDataService.listLatestSameTimestampByMachineId(machineId).stream()
                        .map(PlcDataEntity::getCategoryName)
                        .filter(java.util.Objects::nonNull)
                        .map(ShootMachineStationServiceImpl::parseStationNoFromCategory)
                        .filter(java.util.Objects::nonNull)
                        .collect(Collectors.toSet());
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

    /** 从 categoryName 解析站位号，例如"站台9" -> 9 */
    private static Integer parseStationNoFromCategory(String categoryName) {
        if (categoryName == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("站[台位](\\d+)").matcher(categoryName);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
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
