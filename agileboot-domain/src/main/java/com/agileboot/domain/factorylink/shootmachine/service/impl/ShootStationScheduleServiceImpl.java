package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootStationScheduleMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineStationService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootStationScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShootStationScheduleServiceImpl extends ServiceImpl<ShootStationScheduleMapper, ShootStationScheduleEntity> implements ShootStationScheduleService {

    private final ShootMachineService shootMachineService;
    private final ShootMachineStationService shootMachineStationService;
    private final ShootMoldService shootMoldService;

    @Override
    public List<ShootStationScheduleEntity> listByStationId(Long stationId) {
        getStationOrThrow(stationId);
        return baseMapper.selectListByStationIdWithMold(stationId);
    }

    @Override
    public List<ShootStationScheduleEntity> listCurrentByMachineId(Long machineId) {
        shootMachineService.getByIdOrThrow(machineId);
        return baseMapper.selectListCurrentByMachineIdWithMold(machineId, LocalDateTime.now());
    }

    @Override
    public ShootStationScheduleEntity getByIdOrThrow(Long id) {
        ShootStationScheduleEntity entity = baseMapper.selectByIdWithMold(id);
        if (entity == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "排期");
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootStationScheduleEntity create(Long stationId, ShootStationScheduleEntity entity) {
        ShootMachineStationEntity station = getStationOrThrow(stationId);
        shootMoldService.getByIdOrThrow(entity.getMoldId());
        validateSchedule(entity);
        fillFromStation(entity, station);
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus(ShootStationScheduleEntity.STATUS_PENDING);
        }
        entity.setDeleted(false);
        assertNoOverlap(stationId, entity.getStartTime(), entity.getEndTime(), null);
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootStationScheduleEntity update(Long id, ShootStationScheduleEntity entity) {
        ShootStationScheduleEntity existing = requireExists(id);
        if (ShootStationScheduleEntity.STATUS_CANCELLED.equals(existing.getStatus())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "已取消的排期不能编辑");
        }
        shootMoldService.getByIdOrThrow(entity.getMoldId());
        validateSchedule(entity);
        entity.setId(id);
        entity.setMachineId(existing.getMachineId());
        entity.setStationId(existing.getStationId());
        entity.setStationNo(existing.getStationNo());
        entity.setCreatedAt(existing.getCreatedAt());
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus(existing.getStatus());
        }
        assertNoOverlap(existing.getStationId(), entity.getStartTime(), entity.getEndTime(), id);
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireExists(id);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        ShootStationScheduleEntity entity = requireExists(id);
        entity.setStatus(ShootStationScheduleEntity.STATUS_CANCELLED);
        updateById(entity);
    }

    // 获取站位信息
    private ShootMachineStationEntity getStationOrThrow(Long stationId) {
        ShootMachineStationEntity station = shootMachineStationService.getById(stationId);
        if (station == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, stationId, "站位");
        }
        return station;
    }

    private ShootStationScheduleEntity requireExists(Long id) {
        ShootStationScheduleEntity entity = getById(id);
        if (entity == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "排期");
        }
        return entity;
    }

    private void fillFromStation(ShootStationScheduleEntity entity, ShootMachineStationEntity station) {
        entity.setStationId(station.getId());
        entity.setMachineId(station.getMachineId());
        entity.setStationNo(station.getStationNo());
    }

    private void validateSchedule(ShootStationScheduleEntity entity) {
        if (entity.getMoldId() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "请选择模具");
        }
        if (entity.getStartTime() == null || entity.getEndTime() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "开始时间和结束时间不能为空");
        }
        if (!entity.getStartTime().isBefore(entity.getEndTime())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "结束时间必须晚于开始时间");
        }
    }

    /** 同一站位下时间段不能重叠（已取消的排期不参与校验）。 */
    private void assertNoOverlap(Long stationId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        long overlapCount =
                lambdaQuery()
                        .eq(ShootStationScheduleEntity::getStationId, stationId)
                        .ne(
                                ShootStationScheduleEntity::getStatus,
                                ShootStationScheduleEntity.STATUS_CANCELLED)
                        .lt(ShootStationScheduleEntity::getStartTime, end)
                        .gt(ShootStationScheduleEntity::getEndTime, start)
                        .ne(excludeId != null, ShootStationScheduleEntity::getId, excludeId)
                        .count();
        if (overlapCount > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "该站位在该时间段已有排期，不能重叠");
        }
    }
}
