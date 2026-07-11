package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootStationScheduleMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootDeleteValidator;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineStationService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import com.agileboot.domain.factorylink.shootmachine.service.BatchCreateStationScheduleRequest;
import com.agileboot.domain.factorylink.shootmachine.service.BatchCreateStationScheduleResult;
import com.agileboot.domain.factorylink.shootmachine.service.ShootStationScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShootStationScheduleServiceImpl extends ServiceImpl<ShootStationScheduleMapper, ShootStationScheduleEntity> implements ShootStationScheduleService {


    private final ShootMachineService shootMachineService;
    private final ShootMachineStationService shootMachineStationService;

    private final ShootMoldService shootMoldService;
    private final ShootDeleteValidator deleteValidator;

    @Override
    public List<ShootStationScheduleEntity> listByStationId(Long stationId, LocalDateTime startDate, LocalDateTime endDate, String moldSide) {
        getStationOrThrow(stationId);
        return baseMapper.selectListByStationIdWithMoldAndDateRange(stationId, startDate, endDate, moldSide);
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
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "生产计划");
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootStationScheduleEntity create(ShootStationScheduleEntity entity) {
        ShootMachineStationEntity station = getStationOrThrow(entity.getStationId());
        shootMoldService.getByIdOrThrow(entity.getMoldId());
        validateSchedule(entity);
        fillFromStation(entity, station);
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus(ShootStationScheduleEntity.STATUS_PENDING);
        }
        entity.setDeleted(false);
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootStationScheduleEntity update(Long id, ShootStationScheduleEntity entity) {
        ShootStationScheduleEntity existing = requireExists(id);
        if (ShootStationScheduleEntity.STATUS_CANCELLED.equals(existing.getStatus())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "已取消的生产计划不能编辑");
        }
        shootMoldService.getByIdOrThrow(entity.getMoldId());
        validateSchedule(entity);
        entity.setId(id);
        entity.setMachineId(existing.getMachineId());
        entity.setStationId(existing.getStationId());
        entity.setStationNo(existing.getStationNo());
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus(existing.getStatus());
        }
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ShootStationScheduleEntity schedule = requireExists(id);
        deleteValidator.assertNoAlarm(null, null, schedule.getStationId(), null);
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        ShootStationScheduleEntity entity = requireExists(id);
        entity.setStatus(ShootStationScheduleEntity.STATUS_CANCELLED);
        updateById(entity);
    }

    @Override
    public BatchCreateStationScheduleResult batchCreate(BatchCreateStationScheduleRequest request) {
        if (request.getMoldId() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "请选择模具");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "开始时间和结束时间不能为空");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "结束时间必须晚于开始时间");
        }
        List<BatchCreateStationScheduleRequest.Item> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "请至少选择一个站位模向");
        }
        shootMoldService.getByIdOrThrow(request.getMoldId());

        List<ShootStationScheduleEntity> successItems = new ArrayList<>();
        List<BatchCreateStationScheduleResult.Failure> failures = new ArrayList<>();

        // 每条独立提交，互不回滚；冲突或异常的项记入 failures 后继续
        for (BatchCreateStationScheduleRequest.Item item : items) {
            try {
                ShootMachineStationEntity station = getStationOrThrow(item.getStationId());
                boolean conflict = lambdaQuery()
                        .eq(ShootStationScheduleEntity::getStationId, item.getStationId())
                        .eq(ShootStationScheduleEntity::getMoldSide, item.getMoldSide())
                        .ne(ShootStationScheduleEntity::getStatus, ShootStationScheduleEntity.STATUS_CANCELLED)
                        .exists();
                if (conflict) {
                    failures.add(buildFailure(item, "该站位该模向已存在未取消的生产计划"));
                    continue;
                }
                ShootStationScheduleEntity entity = new ShootStationScheduleEntity();
                entity.setMoldId(request.getMoldId());
                entity.setStartTime(request.getStartTime());
                entity.setEndTime(request.getEndTime());
                entity.setRemark(request.getRemark());
                entity.setMoldSide(item.getMoldSide());
                entity.setStatus(ShootStationScheduleEntity.STATUS_PENDING);
                entity.setDeleted(false);
                fillFromStation(entity, station);
                save(entity);
                successItems.add(entity);
            } catch (ApiException e) {
                failures.add(buildFailure(item, e.getMessage()));
            }
        }

        BatchCreateStationScheduleResult result = new BatchCreateStationScheduleResult();
        result.setSuccessItems(successItems);
        result.setFailures(failures);
        return result;
    }

    private BatchCreateStationScheduleResult.Failure buildFailure(
            BatchCreateStationScheduleRequest.Item item, String reason) {
        BatchCreateStationScheduleResult.Failure failure = new BatchCreateStationScheduleResult.Failure();
        failure.setStationId(item.getStationId());
        failure.setMoldSide(item.getMoldSide());
        failure.setReason(reason);
        return failure;
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
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "生产计划");
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
}
