package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineStationMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldRuleMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootRuleAlarmMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootStationScheduleMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 删除前校验服务，集中管理各实体删除前的关联检查。
 * 只依赖 Mapper，不依赖其他 Service，避免循环依赖。
 */
@Service
@RequiredArgsConstructor
public class ShootDeleteValidator {

    private final ShootMachineStationMapper machineStationMapper;
    private final ShootStationScheduleMapper stationScheduleMapper;
    private final ShootMoldRuleMapper moldRuleMapper;
    private final ShootRuleAlarmMapper ruleAlarmMapper;

    /** 机台下存在站位时不允许删除 */
    public void assertNoMachineStations(Long machineId) {
        long count = machineStationMapper.selectCount(
                Wrappers.<ShootMachineStationEntity>lambdaQuery()
                        .eq(ShootMachineStationEntity::getMachineId, machineId));
        if (count > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "该机台下存在站位，请先删除站位后再删除机台");
        }
    }

    /** 机台下存在未取消排期时不允许删除 */
    public void assertNoMachineActiveSchedule(Long machineId) {
        long count = stationScheduleMapper.selectCount(
                Wrappers.<ShootStationScheduleEntity>lambdaQuery()
                        .eq(ShootStationScheduleEntity::getMachineId, machineId)
                        .ne(ShootStationScheduleEntity::getStatus, ShootStationScheduleEntity.STATUS_CANCELLED));
        if (count > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "该机台下存在排期，请先删除或取消排期后再删除机台");
        }
    }

    /** 模具下存在规则时不允许删除 */
    public void assertNoMoldRules(Long moldId) {
        long count = moldRuleMapper.selectCount(
                Wrappers.<ShootMoldRuleEntity>lambdaQuery()
                        .eq(ShootMoldRuleEntity::getMoldId, moldId));
        if (count > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "该模具下存在规则，请先删除规则后再删除模具");
        }
    }

    /** 模具下存在未取消排期时不允许删除 */
    public void assertNoMoldActiveSchedule(Long moldId) {
        long count = stationScheduleMapper.selectCount(
                Wrappers.<ShootStationScheduleEntity>lambdaQuery()
                        .eq(ShootStationScheduleEntity::getMoldId, moldId)
                        .ne(ShootStationScheduleEntity::getStatus, ShootStationScheduleEntity.STATUS_CANCELLED));
        if (count > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "该模具下存在排期，请先删除或取消排期后再删除模具");
        }
    }

    /** 存在报警时不允许删除 */
    public void assertNoAlarm(Long machineId, Long moldId, Long stationId, Long ruleId) {
        long count = ruleAlarmMapper.countAlarms(machineId, moldId, stationId, ruleId);
        if (count > 0) {
            throw new ApiException(
                    Client.COMMON_REQUEST_PARAMETERS_INVALID, "存在报警记录，请先删除报警后再删除");
        }
    }
}
