package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineStationMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootDeleteValidator;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ShootMachineServiceImpl extends ServiceImpl<ShootMachineMapper, ShootMachineEntity>
        implements ShootMachineService {

    private static final int PLC_STOP_MINUTES = 2;
    private static final int DEFAULT_STATION_COUNT = 10;

    private final PlcDataService plcDataService;
    private final ShootDeleteValidator deleteValidator;
    private final ShootMachineStationMapper shootMachineStationMapper;

    @Override
    public PageDTO<ShootMachineEntity> list(int pageNum, int pageSize) {
        Page<ShootMachineEntity> page = new Page<>(pageNum, pageSize);
        Page<ShootMachineEntity> result =
                lambdaQuery().orderByDesc(ShootMachineEntity::getUpdatedAt).page(page);
        fillPlcRunStatus(result.getRecords());
        return new PageDTO<>(result.getRecords(), result.getTotal());
    }

    @Override
    public ShootMachineEntity getByIdOrThrow(Long id) {
        ShootMachineEntity entity = getById(id);
        if (entity == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "机台");
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMachineEntity create(ShootMachineEntity entity) {
        validateMachineName(entity.getMachineName());
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        if (entity.getStationCount() == null || entity.getStationCount() < 1) {
            entity.setStationCount(DEFAULT_STATION_COUNT);
        }
        entity.setDeleted(false);
        save(entity);

        // 自动生成站位：站位名称格式 = "{机台名称}-站位{编号}"
        String machineName = entity.getMachineName();
        for (int i = 1; i <= entity.getStationCount(); i++) {
            ShootMachineStationEntity station = new ShootMachineStationEntity();
            station.setMachineId(entity.getId());
            station.setStationNo(i);
            station.setStationName(machineName + "-站位" + i);
            station.setEnabled(true);
            station.setDeleted(false);
            station.setCreatedAt(LocalDateTime.now());
            station.setUpdatedAt(LocalDateTime.now());
            shootMachineStationMapper.insert(station);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMachineEntity update(Long id, ShootMachineEntity entity) {
        ShootMachineEntity old = getByIdOrThrow(id);
        validateMachineName(entity.getMachineName());
        entity.setId(id);
        updateById(entity);

        // 名称变更时，同步更新站位名称
        String oldName = old.getMachineName();
        String newName = entity.getMachineName();
        if (oldName != null && !oldName.equals(newName)) {
            shootMachineStationMapper.updateStationName(id, oldName, newName);
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        deleteValidator.assertNoMachineStations(id);
        deleteValidator.assertNoMachineActiveSchedule(id);
        deleteValidator.assertNoAlarm(id, null, null, null);
        removeById(id);
    }

    private void validateMachineName(String machineName) {
        if (StrUtil.isBlank(machineName)) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "机台名称不能为空");
        }
    }

    /**
     * 根据 plc_data 最新采集时间填充运行状态。
     */
    private void fillPlcRunStatus(List<ShootMachineEntity> machines) {
        if (CollUtil.isEmpty(machines)) {
            return;
        }
        List<Long> machineIds =
                machines.stream().map(ShootMachineEntity::getId).filter(id -> id != null).distinct().toList();
        if (machineIds.isEmpty()) {
            return;
        }
        Map<Long, LocalDateTime> latestMap =
                plcDataService.mapLatestDataTimestampByMachineIds(machineIds);
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(PLC_STOP_MINUTES);
        for (ShootMachineEntity machine : machines) {
            LocalDateTime latest = latestMap.get(machine.getId());
            machine.setLatestPlcDataTime(latest);
            machine.setRunning(latest != null && !latest.isBefore(cutoff));
        }
    }
}
