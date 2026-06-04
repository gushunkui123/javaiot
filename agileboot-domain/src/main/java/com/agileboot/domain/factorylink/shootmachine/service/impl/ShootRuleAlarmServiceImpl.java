package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.common.mail.EmailService;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.domain.factorylink.shootmachine.entity.*;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootRuleAlarmMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineStationService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldRuleService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootStationScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShootRuleAlarmServiceImpl extends ServiceImpl<ShootRuleAlarmMapper, ShootRuleAlarmEntity>
        implements ShootRuleAlarmService {

    private static final int ALARM_DEDUP_MINUTES = 1;

    private final ShootStationScheduleService shootStationScheduleService;
    private final ShootMoldRuleService shootMoldRuleService;
    private final ShootMachineService shootMachineService;
    private final ShootMachineStationService shootMachineStationService;
    private final EmailService emailService;

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
        updateById(entity);
        return entity;
    }

    @Override
    public Map<String, Long> getStatisticsOverview(Long machineId) {
        return baseMapper.selectStatisticsOverview(machineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void detectAndCreateAlarms(Long machineId, String jsonPayload) {
        JSONObject root = parseJson(jsonPayload);
        if (root == null) {
            return;
        }
        // 获取当前排期信息
        List<ShootStationScheduleEntity> schedules =
                shootStationScheduleService.listCurrentByMachineId(machineId);

        for (ShootStationScheduleEntity schedule : schedules) {
            if (schedule.getMoldId() == null || schedule.getStationId() == null) {
                continue;
            }
            // 获取模具规则信息
            List<ShootMoldRuleEntity> rules = shootMoldRuleService.listByMoldId(schedule.getMoldId());
            for (ShootMoldRuleEntity rule : rules) {
                if (Boolean.FALSE.equals(rule.getEnabled())) {
                    continue;
                }
                checkRuleAndCreateAlarm(machineId, schedule.getStationId(), rule, root);
            }
        }
    }

    private JSONObject parseJson(String json) {
        try {
            return JSONUtil.parseObj(json);
        } catch (Exception e) {
            log.warn("PLC JSON parse failed: {}", e.getMessage());
            return null;
        }
    }

    private void checkRuleAndCreateAlarm(Long machineId, Long stationId, ShootMoldRuleEntity rule, JSONObject root) {
        // PLC 字段带站位号后缀（如 kai_mo_1），规则 fieldCode 是基础名（如 kai_mo），需要匹配
        String targetBaseKey = rule.getFieldCode();
        String matchedValue = root.keySet().stream()
                .filter(key -> PlcFieldKeyDisplayNames.extractBaseKey(key).equals(targetBaseKey))
                .map(root::getStr)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse(null);
        if (matchedValue == null) {
            return;
        }
        String fieldValue = matchedValue;
        BigDecimal currentValue = Convert.toBigDecimal(fieldValue, null);
        if (currentValue == null) {
            log.warn("Field value convert failed: fieldCode={}, value={}", rule.getFieldCode(), fieldValue);
            return;
        }
        boolean isOutOfRange = currentValue.compareTo(rule.getMinValue()) < 0
                || currentValue.compareTo(rule.getMaxValue()) > 0;
        if (isOutOfRange) {
            createAlarmFromDetection(machineId, stationId, rule, currentValue);
        }
    }

    private void createAlarmFromDetection(Long machineId, Long stationId, ShootMoldRuleEntity rule, BigDecimal currentValue) {
        ShootRuleAlarmEntity alarm = new ShootRuleAlarmEntity();
        alarm.setMachineId(machineId);
        alarm.setStationId(stationId);
        alarm.setMoldId(rule.getMoldId());
        alarm.setRuleId(rule.getId());
        alarm.setFieldCode(rule.getFieldCode());
        alarm.setFieldName(rule.getFieldName());
        alarm.setMinValue(rule.getMinValue());
        alarm.setMaxValue(rule.getMaxValue());
        alarm.setCurrentValue(currentValue);
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setHandleStatus("false");
        alarm.setCreatedAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());
        try {
            ShootRuleAlarmEntity createdAlarm = create(alarm);
            if (createdAlarm != null) {
                // 发送告警邮件
//                sendAlarmEmail(createdAlarm);
            }
        } catch (Exception e) {
            log.error("Create alarm failed: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getId(), rule.getFieldCode(), e);
        }
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

    private void sendAlarmEmail(ShootRuleAlarmEntity alarm) {
        try {
            // 查询设备名和站位名
            String machineName = String.valueOf(alarm.getMachineId());
            String stationName = String.valueOf(alarm.getStationId());
            ShootMachineEntity machine = shootMachineService.getById(alarm.getMachineId());
            if (machine != null) {
                machineName = machine.getMachineName();
            }
            ShootMachineStationEntity station = shootMachineStationService.getById(alarm.getStationId());
            if (station != null) {
                stationName = station.getStationName();
            }
            
            String alarmTitle = "设备告警通知 - " + alarm.getFieldName();
            String alarmContent = String.format(
                "设备名: %s<br>站位号: %s<br>字段名称: %s<br>当前值: %s<br>正常范围: [%s, %s]<br>告警时间: %s",
                machineName, stationName, alarm.getFieldName(),
                alarm.getCurrentValue(), alarm.getMinValue(), alarm.getMaxValue(),
                alarm.getAlarmTime());
            emailService.sendAlarmEmail(alarmTitle, alarmContent);
            log.info("告警邮件发送成功: machineId={}, fieldCode={}", alarm.getMachineId(), alarm.getFieldCode());
        } catch (Exception e) {
            log.error("告警邮件发送失败: machineId={}, fieldCode={}", alarm.getMachineId(), alarm.getFieldCode(), e);
        }
    }
}
