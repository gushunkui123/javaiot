package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.common.exception.error.ErrorCode.Client;
import com.agileboot.common.mail.EmailService;
import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private static final int STOP_TIME_THRESHOLD_SECONDS = 900; // 停机阈值：900秒 = 15分钟

    private final ShootStationScheduleService shootStationScheduleService;
    private final ShootMoldRuleService shootMoldRuleService;
    private final ShootMachineService shootMachineService;
    private final ShootMachineStationService shootMachineStationService;
    private final EmailService emailService;
    private final PlcDataLatestMapper plcDataLatestMapper;

    @Override
    public List<ShootRuleAlarmEntity> listUnhandledWithRelation(Long machineId) {
        // 直接查询未处理报警（自动取消逻辑已移至定时任务）
        return baseMapper.selectUnhandledListWithRelation(machineId);
    }

    @Override
    public List<ShootRuleAlarmExportDTO> listAllForExport(Long machineId, Integer days) {
        List<ShootRuleAlarmEntity> alarms = baseMapper.selectAllWithRelation(machineId, days);
        List<ShootRuleAlarmExportDTO> exportList = new ArrayList<>();
        for (ShootRuleAlarmEntity alarm : alarms) {
            ShootRuleAlarmExportDTO dto = new ShootRuleAlarmExportDTO();
            dto.setMachineName(alarm.getMachineName());
            dto.setStationName(alarm.getStationName());
            dto.setFieldName(alarm.getFieldName());
            dto.setAlarmLevel("yellow".equals(alarm.getAlarmLevel()) ? "黄色" : "红色");
            dto.setMinValue(alarm.getMinValue() != null ? alarm.getMinValue().toString() : "");
            dto.setCurrentValue(alarm.getCurrentValue() != null ? alarm.getCurrentValue().toString() : "");
            dto.setMaxValue(alarm.getMaxValue() != null ? alarm.getMaxValue().toString() : "");
            dto.setAlarmTime(alarm.getAlarmTime() != null ? alarm.getAlarmTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            dto.setHandleStatus("true".equals(alarm.getHandleStatus()) ? "已处理" : "未处理");
            dto.setMoldModel(alarm.getMoldModel());
            dto.setMoldColor(alarm.getMoldColor());
            exportList.add(dto);
        }
        return exportList;
    }

    /**
     * 定时任务：每2秒自动处理超过10秒的黄色报警（基于updated_at判断，使用数据库时间）
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 2000)
    public void autoHandleExpiredYellowAlarms() {
        int handledCount = baseMapper.handleExpiredYellowAlarms(10);
        if (handledCount > 0) {
            log.info("自动取消{}条超时黄色报警", handledCount);
        }
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
    public Map<String, Object> getDetailByStationNo(Integer stationNo) {
        List<Map<String, Object>> results = baseMapper.selectAlarmDetailGroupByField(stationNo);
        if (results.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> detail = new LinkedHashMap<>();
        List<Map<String, Object>> fieldAlarms = new ArrayList<>();
        for (Map<String, Object> row : results) {
            Map<String, Object> fieldAlarm = new LinkedHashMap<>();
            fieldAlarm.put("fieldName", row.get("fieldName"));
            fieldAlarm.put("alarmCount", row.get("alarmCount"));
            fieldAlarm.put("minValue", row.get("minValue"));
            fieldAlarm.put("maxValue", row.get("maxValue"));
            fieldAlarm.put("currentValue", row.get("currentValue"));
            fieldAlarm.put("minAlarmTime", row.get("minAlarmTime"));
            fieldAlarm.put("maxAlarmTime", row.get("maxAlarmTime"));
            fieldAlarms.add(fieldAlarm);

            if (!detail.containsKey("stationName")) {
                detail.put("stationName", row.get("stationName"));
                detail.put("machineName", row.get("machineName"));
                detail.put("moldModel", row.get("moldModel"));
                detail.put("moldColor", row.get("moldColor"));
            }
        }

        detail.put("fieldAlarms", fieldAlarms);
        return detail;
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
    public void handleByStationNoAndField(Integer stationNo, String fieldName, String handleRemark) {
        baseMapper.updateHandleByStationNoAndField(stationNo, fieldName, StrUtil.isBlank(handleRemark) ? "" : handleRemark);
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
        // 获取当前生产计划信息
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
                checkRuleAndCreateAlarm(machineId, schedule.getStationId(), schedule.getStationNo(), rule, root);
            }
        }

        // 检测停机状态（黄色报警）
        detectStopAlarm(machineId, schedules, root);
    }

    private JSONObject parseJson(String json) {
        try {
            return JSONUtil.parseObj(json);
        } catch (Exception e) {
            log.warn("PLC JSON parse failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检测停机状态：当 dang_qian_jia_liu_time 字段数据更新时间超过15分钟未更新时触发黄色报警
     */
    private void detectStopAlarm(Long machineId, List<ShootStationScheduleEntity> schedules, JSONObject root) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thresholdTime = now.minusMinutes(15); // 15分钟阈值
        
        for (ShootStationScheduleEntity schedule : schedules) {
            if (schedule.getStationId() == null || schedule.getStationNo() == null) {
                continue;
            }

            // 查找当前加硫时间字段
            String timeFieldKey = "dang_qian_jia_liu_time_" + schedule.getStationNo();
            
            // 从数据库查询该字段的最新更新时间
            PlcDataLatestEntity latestData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKey(machineId, timeFieldKey);
            
            if (latestData == null) {
                // 没有数据，跳过
                continue;
            }
            
            // 使用create_time作为数据更新时间
            LocalDateTime dataUpdateTime = latestData.getCreateTime();
            if (dataUpdateTime == null) {
                dataUpdateTime = latestData.getDataTimestamp();
            }
            
            if (dataUpdateTime == null) {
                continue;
            }
            
            // 判断数据是否超过15分钟未更新
            boolean isStale = dataUpdateTime.isBefore(thresholdTime);
            
            // 查询当前站位是否有未处理的黄色报警
            long yellowAlarmCount = baseMapper.countRecentYellowAlarm(
                    machineId,
                    schedule.getStationId(),
                    now.minusMinutes(ALARM_DEDUP_MINUTES));

            if (isStale && yellowAlarmCount == 0) {
                // 数据超过15分钟未更新，创建黄色报警
                // 从数据库获取最新的字段值
                String fieldValueStr = latestData.getFieldValue();
                BigDecimal fieldValue = Convert.toBigDecimal(fieldValueStr, null);
                long currentValue = fieldValue != null ? fieldValue.longValue() : 0;
                
                baseMapper.insertYellowAlarm(machineId, schedule.getStationId(), currentValue);
                log.info("创建数据超时黄色报警: machineId={}, stationId={}, lastUpdateTime={}", machineId, schedule.getStationId(), dataUpdateTime);
            } else if (!isStale && yellowAlarmCount > 0) {
                // 数据恢复正常更新，自动处理黄色报警
                baseMapper.handleYellowAlarms(machineId, schedule.getStationId());
            }
        }
    }

    private void checkRuleAndCreateAlarm(Long machineId, Long stationId, Integer stationNo, ShootMoldRuleEntity rule, JSONObject root) {
        // PLC 字段带站位号后缀（如 kai_mo_1），规则 fieldCode 是基础名（如 kai_mo），需要匹配
        String targetBaseKey = rule.getFieldCode();
        String matchedValue = root.keySet().stream()
                .filter(key -> PlcFieldKeyDisplayNames.extractBaseKey(key).equals(targetBaseKey))
                .filter(key -> stationNo == null || stationNo.equals(PlcFieldKeyDisplayNames.parseStationNo(key)))
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
            // 值超出范围，创建报警
            createAlarmFromDetection(machineId, stationId, rule, currentValue);
        } else {
            // 值恢复正常，自动取消该规则的红色报警
            autoCancelRedAlarmWhenNormal(machineId, stationId, rule);
        }
    }

    private void createAlarmFromDetection(Long machineId, Long stationId, ShootMoldRuleEntity rule, BigDecimal currentValue) {
        try {
            baseMapper.insertRedAlarm(machineId, stationId, rule.getMoldId(), rule.getId(),
                    rule.getFieldCode(), rule.getFieldName(), rule.getMinValue(), rule.getMaxValue(), currentValue);
        } catch (Exception e) {
            log.error("Create alarm failed: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getId(), rule.getFieldCode(), e);
        }
    }

    /**
     * 当参数恢复正常时，自动取消该规则的红色报警
     */
    private void autoCancelRedAlarmWhenNormal(Long machineId, Long stationId, ShootMoldRuleEntity rule) {
        try {
            // 查询是否有未处理的红色报警
            long unhandledCount = baseMapper.countUnhandledRedAlarms(machineId, stationId, rule.getId());
            if (unhandledCount > 0) {
                // 自动取消红色报警
                int cancelledCount = baseMapper.autoCancelRedAlarms(machineId, stationId, rule.getId());
                if (cancelledCount > 0) {
                    log.info("参数恢复正常，自动取消{}条红色报警: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                            cancelledCount, machineId, stationId, rule.getId(), rule.getFieldCode());
                }
            }
        } catch (Exception e) {
            log.error("自动取消红色报警失败: machineId={}, stationId={}, ruleId={}, fieldCode={}",
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
