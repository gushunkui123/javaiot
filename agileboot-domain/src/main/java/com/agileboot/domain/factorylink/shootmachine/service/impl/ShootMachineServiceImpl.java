package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.shootmachine.entity.MachineGroupEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.MachineGroupMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineStationMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootRuleAlarmMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootDeleteValidator;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
    private static final int DEFAULT_GUN_COUNT = 4;

    private final PlcDataService plcDataService;
    private final ShootDeleteValidator deleteValidator;
    private final ShootMachineStationMapper shootMachineStationMapper;
    private final ShootRuleAlarmMapper shootRuleAlarmMapper;
    private final MachineGroupMapper machineGroupMapper;

    @Override
    public PageDTO<ShootMachineEntity> list(int pageNum, int pageSize, Boolean enabled, String group) {
        Page<ShootMachineEntity> page = new Page<>(pageNum, pageSize);
        var query = lambdaQuery();
        // 不传 enabled 返回全部；传 true/false 按启用状态过滤
        if (enabled != null) {
            query.eq(ShootMachineEntity::getEnabled, enabled);
        }
        // 不传 group 返回全部；传分组编码按 machine_group 过滤
        if (StrUtil.isNotBlank(group)) {
            query.eq(ShootMachineEntity::getMachineGroup, group);
        }
        Page<ShootMachineEntity> result =
                query.orderByAsc(ShootMachineEntity::getSort)
                        .orderByDesc(ShootMachineEntity::getUpdatedAt)
                        .page(page);
        fillPlcRunStatus(result.getRecords());
        fillHasUnhandledAlarm(result.getRecords());
        return new PageDTO<>(result.getRecords(), result.getTotal());
    }

    @Override
    public List<MachineGroupEntity> listGroups() {
        return machineGroupMapper.selectList(
                Wrappers.<MachineGroupEntity>lambdaQuery()
                        .eq(MachineGroupEntity::getEnabled, true)
                        .orderByAsc(MachineGroupEntity::getSort)
                        .orderByAsc(MachineGroupEntity::getId));
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
        if (entity.getGunCount() == null || entity.getGunCount() < 1) {
            entity.setGunCount(DEFAULT_GUN_COUNT);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        entity.setDeleted(false);
        save(entity);

        // 自动生成站台：站台名称格式 = "站台{编号}"（如 站台1、站台10）
        for (int i = 1; i <= entity.getStationCount(); i++) {
            ShootMachineStationEntity station = new ShootMachineStationEntity();
            station.setMachineId(entity.getId());
            station.setStationNo(i);
            station.setStationName("站台" + i);
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
        if (entity.getGunCount() != null && entity.getGunCount() < 1) {
            entity.setGunCount(old.getGunCount() != null && old.getGunCount() >= 1
                    ? old.getGunCount() : DEFAULT_GUN_COUNT);
        }
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        deleteValidator.assertNoMachineActiveSchedule(id);
        deleteValidator.assertNoAlarm(id, null, null, null);
        // 删除机台时一并软删除其下所有站台（@TableLogic 置 deleted=1，不物理删，外键安全）
        shootMachineStationMapper.delete(
                Wrappers.<ShootMachineStationEntity>lambdaQuery()
                        .eq(ShootMachineStationEntity::getMachineId, id));
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

    /**
     * 根据未处理报警填充机台是否有未处理报警标记（单次 IN 查询批量判定）
     */
    private void fillHasUnhandledAlarm(List<ShootMachineEntity> machines) {
        if (CollUtil.isEmpty(machines)) {
            return;
        }
        List<Long> machineIds =
                machines.stream().map(ShootMachineEntity::getId).filter(id -> id != null).distinct().toList();
        if (machineIds.isEmpty()) {
            return;
        }
        List<Long> alarmMachineIds = shootRuleAlarmMapper.selectMachineIdsWithUnhandledAlarm(machineIds);
        for (ShootMachineEntity machine : machines) {
            machine.setHasUnhandledAlarm(alarmMachineIds.contains(machine.getId()));
        }
    }
}
