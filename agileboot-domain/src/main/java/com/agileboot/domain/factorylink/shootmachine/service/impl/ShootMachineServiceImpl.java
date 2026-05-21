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
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ShootMachineServiceImpl extends ServiceImpl<ShootMachineMapper, ShootMachineEntity>
        implements ShootMachineService {

    /** 超过该时长未收到 PLC 数据视为停机（2 分钟） */
    private static final long PLC_STOP_THRESHOLD_MS = 2 * 60 * 1000L;

    private final PlcDataService plcDataService;
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
        entity.setDeleted(false);
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootMachineEntity update(Long id, ShootMachineEntity entity) {
        ShootMachineEntity existing = getByIdOrThrow(id);
        validateMachineName(entity.getMachineName());
        entity.setId(id);
        entity.setCreatedAt(existing.getCreatedAt());
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getByIdOrThrow(id);
        removeById(id);
        deleteStationsByMachineId(id);
    }

    /** 删机台时级联逻辑删除其下站位 */
    private void deleteStationsByMachineId(Long machineId) {
        shootMachineStationMapper.delete(
                Wrappers.<ShootMachineStationEntity>lambdaQuery()
                        .eq(ShootMachineStationEntity::getMachineId, machineId));
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
        Map<Long, Date> latestByMachineId = plcDataService.mapLatestDataTimestampByMachineIds(machineIds);
        long now = System.currentTimeMillis();
        for (ShootMachineEntity machine : machines) {
            Date latest = latestByMachineId.get(machine.getId());
            machine.setLatestPlcDataTime(latest);
            machine.setRunning(latest != null && now - latest.getTime() <= PLC_STOP_THRESHOLD_MS);
        }
    }
}
