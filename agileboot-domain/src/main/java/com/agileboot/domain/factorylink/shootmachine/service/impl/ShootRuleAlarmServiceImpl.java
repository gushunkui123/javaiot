package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootRuleAlarmMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShootRuleAlarmServiceImpl extends ServiceImpl<ShootRuleAlarmMapper, ShootRuleAlarmEntity>
        implements ShootRuleAlarmService {

    private static final int ALARM_DEDUP_MINUTES = 1;

    @Override
    public List<ShootRuleAlarmEntity> listUnhandledWithRelation(Long machineId) {
        return baseMapper.selectUnhandledListWithRelation(machineId);
    }

    @Override
    public ShootRuleAlarmEntity getByIdOrThrow(Long id) {
        ShootRuleAlarmEntity entity = getById(id);
        if (entity == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, id, "报警记录");
        }
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootRuleAlarmEntity create(ShootRuleAlarmEntity entity) {
        validateAlarm(entity);

        // 检查是否重复报警，一分钟内不允许重复创建同一个站位的同一规则的报警
        LocalDateTime sinceTime = LocalDateTime.now().minusMinutes(ALARM_DEDUP_MINUTES);
        // 查询最近1分钟内是否有相同报警记录
        long count = baseMapper.countRecentSameAlarm(
                entity.getMachineId(),
                entity.getStationId(),
                entity.getRuleId(),
                sinceTime);
        
        if (count > 0) {
            return null;
        }

        entity.setAlarmTime(entity.getAlarmTime() != null ? entity.getAlarmTime() : LocalDateTime.now());
        entity.setHandleStatus("false");
        entity.setHandleRemark(null);
        entity.setDeleted(false);
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootRuleAlarmEntity handle(Long id, String handleRemark) {
        ShootRuleAlarmEntity entity = getByIdOrThrow(id);
        if ("true".equals(entity.getHandleStatus())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "该报警已处理，请勿重复操作");
        }
        entity.setHandleStatus("true");
        entity.setHandleRemark(StrUtil.isBlank(handleRemark) ? "" : handleRemark);
        entity.setUpdatedAt(LocalDateTime.now());
        updateById(entity);
        return entity;
    }

    @Override
    public Map<String, Long> getStatisticsOverview(Long machineId) {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("totalCount", lambdaQuery()
                .eq(machineId != null, ShootRuleAlarmEntity::getMachineId, machineId)
                .count());
        statistics.put("alarmCount", lambdaQuery()
                .eq(machineId != null, ShootRuleAlarmEntity::getMachineId, machineId)
                .eq(ShootRuleAlarmEntity::getHandleStatus, "false")
                .count());
        return statistics;
    }

    private void validateAlarm(ShootRuleAlarmEntity entity) {
        if (entity.getMachineId() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "机器ID不能为空");
        }
        if (entity.getStationId() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "站位ID不能为空");
        }
        if (entity.getMoldId() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "模具ID不能为空");
        }
        if (entity.getRuleId() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "规则ID不能为空");
        }
        if (StrUtil.isBlank(entity.getFieldCode())) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "字段编码不能为空");
        }
        if (entity.getCurrentValue() == null) {
            throw new ApiException(Client.COMMON_REQUEST_PARAMETERS_INVALID, "当前值不能为空");
        }
    }
}
