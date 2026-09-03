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
import com.agileboot.domain.factorylink.shootmachine.service.StationMoldModelResponse;
import com.agileboot.domain.factorylink.shootmachine.service.BatchCreateStationScheduleRequest;
import com.agileboot.domain.factorylink.shootmachine.service.BatchCreateStationScheduleResult;
import com.agileboot.domain.factorylink.shootmachine.service.ShootStationScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public List<StationMoldModelResponse> listStationMoldModels(Long machineId) {
        shootMachineService.getByIdOrThrow(machineId);
        List<ShootStationScheduleEntity> rows = baseMapper.selectListCurrentByMachineIdWithMold(machineId, LocalDateTime.now());

        Map<Integer, String> stationNameMap = new HashMap<>();
        shootMachineStationService.listByMachineId(machineId)
                .forEach(s -> stationNameMap.put(s.getStationNo(), s.getStationName()));

        Map<Integer, StationMoldModelResponse> map = new LinkedHashMap<>();
        for (ShootStationScheduleEntity row : rows) {
            Integer stationNo = row.getStationNo();
            StationMoldModelResponse resp = map.computeIfAbsent(stationNo, k -> {
                StationMoldModelResponse r = new StationMoldModelResponse();
                r.setStationNo(stationNo);
                r.setStationName(stationNameMap.getOrDefault(stationNo, "站台" + stationNo));
                return r;
            });
            if ("LEFT".equals(row.getMoldSide())) {
                resp.setLeftMoldModel(row.getMoldModel());
            } else if ("RIGHT".equals(row.getMoldSide())) {
                resp.setRightMoldModel(row.getMoldModel());
            }
        }
        return new ArrayList<>(map.values());
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
        shootMoldService.getEnabledOrThrow(entity.getMoldId());
        validateSchedule(entity);
        checkNoOverlap(entity.getStationId(), entity.getMoldSide(),
                entity.getStartTime(), entity.getEndTime(), null);
        fillFromStation(entity, station);
        applyCrossDayCount(entity);
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
        shootMoldService.getEnabledOrThrow(entity.getMoldId());
        validateSchedule(entity);
        checkNoOverlap(entity.getStationId(), entity.getMoldSide(),
                entity.getStartTime(), entity.getEndTime(), id);
        entity.setId(id);
        entity.setMachineId(existing.getMachineId());
        entity.setStationId(existing.getStationId());
        entity.setStationNo(existing.getStationNo());
        if (StrUtil.isBlank(entity.getStatus())) {
            entity.setStatus(existing.getStatus());
        }
        applyCrossDayCount(entity);
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
        ShootStationScheduleEntity existing = requireExists(id);
        if (ShootStationScheduleEntity.STATUS_RUNNING.equals(existing.getStatus())
                || ShootStationScheduleEntity.STATUS_FINISHED.equals(existing.getStatus())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "已开始或已完成的生产计划不能取消");
        }
        existing.setStatus(ShootStationScheduleEntity.STATUS_CANCELLED);
        updateById(existing);
    }

    private ShootStationScheduleEntity requireExists(Long id) {
        ShootStationScheduleEntity entity = getById(id);
        if (entity == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "生产计划");
        }
        return entity;
    }

    // 获取站位信息，不存在则抛异常
    private ShootMachineStationEntity getStationOrThrow(Long stationId) {
        ShootMachineStationEntity station = shootMachineStationService.getById(stationId);
        if (station == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, stationId, "站台");
        }
        return station;
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
        shootMoldService.getEnabledOrThrow(request.getMoldId());

        List<ShootStationScheduleEntity> successItems = new ArrayList<>();
        List<BatchCreateStationScheduleResult.Failure> failures = new ArrayList<>();
        // 同批次已成功写入的站位+模向组合，避免同批次内重复写入
        Set<String> batchUsedKeys = new HashSet<>();

        // 每条独立提交，互不回滚；冲突或异常的项记入 failures 后继续
        for (BatchCreateStationScheduleRequest.Item item : items) {
            try {
                if (item.getStationId() == null) {
                    throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "请选择站位");
                }
                validateMoldSide(item.getMoldSide());
                String key = item.getStationId() + "_" + item.getMoldSide();
                if (batchUsedKeys.contains(key)) {
                    failures.add(buildFailure(item, "同批次内该站位该模向重复选择，已跳过"));
                    continue;
                }
                ShootMachineStationEntity station = getStationOrThrow(item.getStationId());
                boolean conflict = baseMapper.countOverlapping(item.getStationId(), item.getMoldSide(),
                        request.getStartTime(), request.getEndTime(), null) > 0;
                if (conflict) {
                    failures.add(buildFailure(item, "该站位该模向在指定时间段内已存在生产计划，时间冲突"));
                    continue;
                }
                ShootStationScheduleEntity entity = new ShootStationScheduleEntity();
                entity.setMoldId(request.getMoldId());
                entity.setStartTime(request.getStartTime());
                entity.setEndTime(request.getEndTime());
                entity.setRemark(request.getRemark());
                entity.setMoldSide(item.getMoldSide());
                entity.setGunNo(item.getGunNo());
                entity.setStatus(ShootStationScheduleEntity.STATUS_PENDING);
                entity.setDeleted(false);
                fillFromStation(entity, station);
                applyCrossDayCount(entity);
                save(entity);
                successItems.add(entity);
                batchUsedKeys.add(key);
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

    private void fillFromStation(ShootStationScheduleEntity entity, ShootMachineStationEntity station) {
        entity.setStationId(station.getId());
        entity.setMachineId(station.getMachineId());
        entity.setStationNo(station.getStationNo());
    }

    /**
     * 计算并写入"跨自然日数"：仅取 start_time/end_time 的年月日之差（忽略时分秒）。
     * 该值为派生字段，始终以起止时间为准重算，忽略任何客户端传入的值。
     */
    private void applyCrossDayCount(ShootStationScheduleEntity entity) {
        if (entity.getStartTime() != null && entity.getEndTime() != null) {
            long days = ChronoUnit.DAYS.between(
                    entity.getStartTime().toLocalDate(),
                    entity.getEndTime().toLocalDate());
            entity.setCrossDayCount((int) days);
        }
    }

    private void checkNoOverlap(Long stationId, String moldSide,
                                LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        long count = baseMapper.countOverlapping(stationId, moldSide, startTime, endTime, excludeId);
        if (count > 0) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID,
                    "该站台该模向在指定时间段内已存在生产计划，时间冲突");
        }
    }

    private void validateSchedule(ShootStationScheduleEntity entity) {
        if (entity.getMoldId() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "请选择模具");
        }
        validateMoldSide(entity.getMoldSide());
        if (entity.getStartTime() == null || entity.getEndTime() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "开始时间和结束时间不能为空");
        }
        if (!entity.getStartTime().isBefore(entity.getEndTime())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "结束时间必须晚于开始时间");
        }
    }

    private void validateMoldSide(String moldSide) {
        if (StrUtil.isBlank(moldSide)) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "请选择模向（左模/右模）");
        }
        if (!"LEFT".equals(moldSide) && !"RIGHT".equals(moldSide)) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "模向值非法，必须为 LEFT 或 RIGHT");
        }
    }
}
