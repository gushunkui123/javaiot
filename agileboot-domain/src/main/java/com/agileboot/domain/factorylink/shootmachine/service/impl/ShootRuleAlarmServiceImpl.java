package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.domain.factorylink.shootmachine.dto.AlarmPageResponse;
import com.agileboot.domain.factorylink.shootmachine.entity.*;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootRuleAlarmMapper;
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

    // ====== 报警阈值常量 ======
    private static final int DATA_STALE_MINUTES = 15;       // 数据超时停机：15分钟未更新
    private static final int MOLD_TIMEOUT_SECONDS = 60;     // 操作超时：开模止=ON后60秒未合模
    private static final int MOLD_STOP_MINUTES = 5;         // 停机报警：开模止=ON后5分钟未合模

    private final ShootStationScheduleService shootStationScheduleService;
    private final ShootMachineStationService shootMachineStationService;
    private final ShootMoldRuleService shootMoldRuleService;
    private final PlcDataLatestMapper plcDataLatestMapper;

    @Override
    public List<ShootRuleAlarmEntity> listUnhandledWithRelation(Long machineId) {
        // 直接查询未处理报警（自动取消逻辑已移至定时任务）
        return baseMapper.selectUnhandledListWithRelation(machineId);
    }

    @Override
    public List<Map<String, Object>> listUnhandledSummary(Long machineId) {
        return baseMapper.selectUnhandledSummary(machineId);
    }

    @Override
    public AlarmPageResponse listUnhandledPaged(Long machineId, Long stationId, long page, long pageSize) {
        long offset = (page - 1) * pageSize;
        List<ShootRuleAlarmEntity> items = baseMapper.selectUnhandledPaged(machineId, stationId, offset, pageSize);
        long total = baseMapper.countUnhandled(machineId, stationId);
        AlarmPageResponse response = new AlarmPageResponse();
        response.setItems(items);
        response.setTotal(total);
        return response;
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
     * 定时任务：每5秒自动处理超过10秒的黄色报警（基于updated_at判断，使用数据库时间）
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 5000)
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
    public void handleByStationIdAndField(List<Long> ids, String handleRemark) {
        baseMapper.updateHandleByStationIdAndField(ids, StrUtil.isBlank(handleRemark) ? "" : handleRemark);
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

        // 红色报警检测已统一由 detectAlarmsByPlcData → detectRedAlarmsFromPlcData 处理
        // 此处仅保留黄色报警检测
        detectYellowAndStopAlarms(machineId, schedules);
    }

    /**
     * 根据 plc_data_latest 表数据检测黄色/红色报警
     * 第三方接口同步 PLC 数据后调用，无需 JSON 参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void detectAlarmsByPlcData(Long machineId) {
        List<ShootStationScheduleEntity> schedules =
                shootStationScheduleService.listCurrentByMachineId(machineId);
        detectYellowAndStopAlarms(machineId, schedules);

        // 从PLC数据检测红色报警（基于规则值范围，含射枪温度等全局字段）
        detectRedAlarmsFromPlcData(machineId, schedules);
    }
    

    /**
     * 从plc_data_latest表检测红色报警（基于规则值范围）
     * 通过 PlcFieldKeyDisplayNames 将规则 fieldCode 解析为 PLC 实际字段名（可能多个，如射出压力→5个阶段），逐条比对。
     */
    private void detectRedAlarmsFromPlcData(Long machineId, List<ShootStationScheduleEntity> schedules) {
        // 获取机器站位数，推导枪数
        long stationCount = shootMachineStationService.lambdaQuery()
                .eq(ShootMachineStationEntity::getMachineId, machineId)
                .count();
        int gunCount = ShootMachineStationEntity.resolveGunCount((int) stationCount);

        // 内存去重：同一事务内相同的 (stationId, ruleId, fieldCode, currentValue) 只插入一次，
        // 解决同站位多 schedule（多枪别）时 DB 去重看不见未提交 INSERT 的问题
        java.util.Set<String> insertedKeys = new java.util.HashSet<>();

        for (ShootStationScheduleEntity schedule : schedules) {
            Long stationId = schedule.getStationId();
            Integer stationNo = schedule.getStationNo();
            Long moldId = schedule.getMoldId();
            String moldSide = schedule.getMoldSide();
            Integer gunNo = schedule.getGunNo();
            if (stationId == null || stationNo == null || moldId == null) {
                continue;
            }
            log.info("检测红色报警: schedule stationId={}, stationNo={}, moldId={}, moldSide={}, gunNo={}, gunCount={}",
                    stationId, stationNo, moldId, moldSide, gunNo, gunCount);

            String categoryName = "站台" + stationNo;

            // 获取该模具的规则
            List<ShootMoldRuleEntity> rules = shootMoldRuleService.listByMoldId(moldId);
            for (ShootMoldRuleEntity rule : rules) {
                if (Boolean.FALSE.equals(rule.getEnabled())) {
                    continue;
                }

                String[] result = resolveFieldKeysAndCategory(rule, moldSide, gunCount, gunNo, categoryName);
                List<String> plcFieldKeys = List.of(result[0].split(","));
                String queryCategoryName = result[1];

                for (String plcFieldKey : plcFieldKeys) {
                    PlcDataLatestEntity fieldData = queryPlcFieldByCategory(machineId, queryCategoryName, plcFieldKey);
                    if (fieldData == null) {
                        continue;
                    }

                    BigDecimal currentValue = Convert.toBigDecimal(fieldData.getFieldValue(), null);
                    if (currentValue == null) {
                        continue;
                    }

                    // 检查值是否超出范围
                    boolean isOutOfRange = currentValue.compareTo(rule.getMinValue()) < 0
                            || currentValue.compareTo(rule.getMaxValue()) > 0;

                    if (isOutOfRange) {
                        // 内存去重：同一事务内相同 (stationId, ruleId, fieldCode, currentValue) 已插入则跳过
                        String dedupKey = stationId + "_" + rule.getId() + "_" + plcFieldKey + "_" + currentValue;
                        if (insertedKeys.add(dedupKey)) {
                            createAlarmFromDetection(machineId, stationId, rule, currentValue, plcFieldKey);
                        }
                    } else {
                        autoCancelRedAlarmWhenNormal(machineId, stationId, rule, plcFieldKey);
                    }
                }
            }
        }
    }

    /** 先按分类（站台X）查，再回退到全局（不按分类，用于射枪温度等跨站位字段） */
    private PlcDataLatestEntity queryPlcFieldByCategory(Long machineId, String categoryName, String fieldKey) {
        PlcDataLatestEntity fieldData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(
                machineId, fieldKey, categoryName);
        if (fieldData == null) {
            fieldData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyForGlobal(machineId, fieldKey);
        }
        return fieldData;
    }

    /**
     * 根据规则解析PLC字段名列表和查询用的category_name
     * @return String[]{fieldKeysCsv, categoryName}
     */
    private String[] resolveFieldKeysAndCategory(ShootMoldRuleEntity rule, String moldSide, int gunCount, Integer gunNo, String defaultCategoryName) {
        String fieldCode = rule.getFieldCode();
        String normalizedFieldCode = fieldCode.replaceAll("\\s+", "");
        log.info("解析规则字段: ruleId={}, fieldCode='{}', normalized='{}', gunCount={}, gunNo={}", rule.getId(), fieldCode, normalizedFieldCode, gunCount, gunNo);
        
        if ("射枪温度".equals(normalizedFieldCode)) {
            List<String> keys = PlcFieldKeyDisplayNames.resolvePlcFieldKeysForGunTemperature(fieldCode, gunCount, gunNo);
            String categoryName = PlcFieldKeyDisplayNames.resolveGunTemperatureCategoryName(gunCount);
            log.info("射枪温度规则匹配: ruleId={}, keys={}, categoryName={}", rule.getId(), keys, categoryName);
            return new String[]{String.join(",", keys), categoryName};
        }
        
        // 阶段特定的射枪温度规则，如 "第一阶段 射枪温度"
        Integer stageNo = PlcFieldKeyDisplayNames.parseGunTemperatureStage(fieldCode);
        log.info("阶段射枪温度解析: ruleId={}, fieldCode='{}', stageNo={}", rule.getId(), fieldCode, stageNo);
        
        if (stageNo != null) {
            List<String> keys = PlcFieldKeyDisplayNames.resolvePlcFieldKeysForGunTemperatureByStage(stageNo, gunCount, gunNo);
            String categoryName = PlcFieldKeyDisplayNames.resolveGunTemperatureCategoryName(gunCount);
            log.info("阶段射枪温度规则匹配: ruleId={}, stageNo={}, keys={}, categoryName={}", rule.getId(), stageNo, keys, categoryName);
            return new String[]{String.join(",", keys), categoryName};
        }
        
        // 其他字段：使用原有逻辑
        String sidePrefix = "LEFT".equalsIgnoreCase(moldSide) ? "左模" : "右模";
        List<String> keys = PlcFieldKeyDisplayNames.resolvePlcFieldKeys(fieldCode, moldSide);
        if (keys.isEmpty()) {
            keys = new ArrayList<>(List.of(sidePrefix + fieldCode, fieldCode));
        }
        log.info("其他字段规则: ruleId={}, keys={}, categoryName={}", rule.getId(), keys, defaultCategoryName);
        return new String[]{String.join(",", keys), defaultCategoryName};
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
     * 统一检测：数据超时停机 + 操作超时 + 5分钟未合模停机
     * 遍历一次 schedules，对每个站位完成所有黄色/红色报警检测
     */
    private void detectYellowAndStopAlarms(Long machineId, List<ShootStationScheduleEntity> schedules) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime staleThreshold = now.minusMinutes(DATA_STALE_MINUTES);
        LocalDateTime dedupSince = now.minusMinutes(ALARM_DEDUP_MINUTES);

        for (ShootStationScheduleEntity schedule : schedules) {
            Long stationId = schedule.getStationId();
            Integer stationNo = schedule.getStationNo();
            Long moldId = schedule.getMoldId();
            if (stationId == null || stationNo == null || moldId == null) {
                continue;
            }

            String categoryName = "站台" + stationNo;

            // 查询该模具的规则列表，构建 field_code -> rule_id 映射
            Map<String, Long> fieldCodeToRuleIdMap = new java.util.HashMap<>();
            log.info("开始查询模具规则: moldId={}", moldId);
            List<ShootMoldRuleEntity> rules = shootMoldRuleService.listByMoldId(moldId);
            log.info("查询到模具规则数量: {}, moldId={}", rules.size(), moldId);
            for (ShootMoldRuleEntity rule : rules) {
                log.info("规则详情: id={}, fieldCode={}, fieldName={}, enabled={}, moldId={}",
                        rule.getId(), rule.getFieldCode(), rule.getFieldName(), rule.getEnabled(), rule.getMoldId());
                if (Boolean.TRUE.equals(rule.getEnabled())) {
                    fieldCodeToRuleIdMap.put(rule.getFieldCode(), rule.getId());
                }
            }
            log.info("fieldCodeToRuleIdMap: {}", fieldCodeToRuleIdMap);

            // 一次查出该站位需要的3个字段
            PlcDataLatestEntity settingTimeData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(
                    machineId, "设定加硫时间", categoryName);
            PlcDataLatestEntity kaiMoData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(
                    machineId, "开模止", categoryName);
            PlcDataLatestEntity heMoData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(
                    machineId, "合模止", categoryName);

            // 查询该站位已有的未处理黄色报警（复用）
            long yellowAlarmCount = baseMapper.countRecentYellowAlarm(machineId, stationId, dedupSince);

            // ====== 1. 数据超时停机：设定加硫时间 15分钟未更新 → 黄色报警 ======
            detectDataStaleAlarm(machineId, stationId, stationNo, categoryName, moldId, fieldCodeToRuleIdMap, settingTimeData, staleThreshold, yellowAlarmCount, now);

            // ====== 2. 操作超时 & 5分钟未合模：开模止/合模止状态检测 ======
            detectMoldStateAlarms(machineId, stationId, stationNo, categoryName, moldId, fieldCodeToRuleIdMap, kaiMoData, heMoData, yellowAlarmCount, now, dedupSince);
        }
    }

    /**
     * 检测数据超时：设定加硫时间超过15分钟未更新 → 黄色报警（15分钟未加硫停机）
     */
    private void detectDataStaleAlarm(Long machineId, Long stationId, Integer stationNo, String categoryName,
                                      Long moldId, Map<String, Long> fieldCodeToRuleIdMap,
                                      PlcDataLatestEntity latestData, LocalDateTime staleThreshold,
                                      long yellowAlarmCount, LocalDateTime now) {
        if (latestData == null) {
            return;
        }

        LocalDateTime updateTime = latestData.getCreateTime() != null ? latestData.getCreateTime() : latestData.getDataTimestamp();
        if (updateTime == null) {
            return;
        }

        boolean isStale = updateTime.isBefore(staleThreshold);

        if (isStale && yellowAlarmCount == 0) {
            BigDecimal fieldValue = Convert.toBigDecimal(latestData.getFieldValue(), null);
            long currentValue = fieldValue != null ? fieldValue.longValue() : 0;
            String fieldCode = "she_ding_jia_liu_time";
            Long ruleId = fieldCodeToRuleIdMap.get(fieldCode);
            if (ruleId == null) {
                log.error("未找到数据超时规则! moldId={}, fieldCode={}, fieldCodeToRuleIdMap={}", moldId, fieldCode, fieldCodeToRuleIdMap);
                return;
            }
            baseMapper.insertYellowAlarm(machineId, stationId, moldId, ruleId, fieldCode, "15分钟未加硫停机", currentValue);
            log.info("创建数据超时黄色报警: machineId={}, stationId={}, ruleId={}, categoryName={}, lastUpdateTime={}",
                    machineId, stationId, ruleId, categoryName, updateTime);
        } else if (!isStale && yellowAlarmCount > 0) {
            baseMapper.handleYellowAlarms(machineId, stationId);
        }
    }

    /**
     * 检测模具状态：
     * - 开模止=ON 且合模止≠ON 持续≥60秒 → 黄色报警（操作超时，10秒自动关闭）
     * - 开模止=ON 且合模止≠ON 持续≥5分钟 → 红色报警（停机）
     * - 状态恢复正常 → 自动取消黄色报警
     */
    private void detectMoldStateAlarms(Long machineId, Long stationId, Integer stationNo, String categoryName,
                                       Long moldId, Map<String, Long> fieldCodeToRuleIdMap,
                                       PlcDataLatestEntity kaiMoData, PlcDataLatestEntity heMoData,
                                       long yellowAlarmCount, LocalDateTime now, LocalDateTime dedupSince) {
        if (kaiMoData == null || heMoData == null) {
            return;
        }

        String kaiMoValue = kaiMoData.getFieldValue();
        String heMoValue = heMoData.getFieldValue();
        LocalDateTime kaiMoTime = kaiMoData.getCreateTime() != null ? kaiMoData.getCreateTime() : kaiMoData.getDataTimestamp();

        if (kaiMoTime == null) {
            return;
        }

        boolean isOpenAndNotClosed = "ON".equalsIgnoreCase(kaiMoValue) && !"ON".equalsIgnoreCase(heMoValue);

        if (isOpenAndNotClosed) {
            long elapsedSeconds = java.time.Duration.between(kaiMoTime, now).getSeconds();

            // 60秒操作超时 → 黄色报警
            if (elapsedSeconds >= MOLD_TIMEOUT_SECONDS && yellowAlarmCount == 0) {
                String fieldCode = "operation_timeout";
                Long ruleId = fieldCodeToRuleIdMap.get(fieldCode);
                if (ruleId == null) {
                    log.error("未找到操作超时规则! moldId={}, fieldCode={}, fieldCodeToRuleIdMap={}", moldId, fieldCode, fieldCodeToRuleIdMap);
                    return;
                }
                baseMapper.insertYellowAlarm(machineId, stationId, moldId, ruleId, fieldCode, "操作超时", 0L);
                log.info("创建操作超时黄色报警: stationId={}, stationNo={}, ruleId={}, 已过{}秒", stationId, stationNo, ruleId, elapsedSeconds);
            }

            // 5分钟未合模 → 停机黄色报警
            if (elapsedSeconds >= MOLD_STOP_MINUTES * 60) {
                String fieldCode = "stop_no_mold_close";
                Long ruleId = fieldCodeToRuleIdMap.get(fieldCode);
                if (ruleId == null) {
                    log.error("未找到停机规则! moldId={}, fieldCode={}, fieldCodeToRuleIdMap={}", moldId, fieldCode, fieldCodeToRuleIdMap);
                    return;
                }
                long sameYellowAlarmCount = baseMapper.countRecentSameYellowAlarm(machineId, stationId, ruleId, dedupSince);
                if (sameYellowAlarmCount == 0) {
                    baseMapper.insertYellowAlarm(machineId, stationId, moldId, ruleId,
                            fieldCode, "5分钟未合模停机", 0L);
                    log.info("创建5分钟未合模停机黄色报警: stationId={}, stationNo={}, ruleId={}, 已过{}秒",
                            stationId, stationNo, ruleId, elapsedSeconds);
                }
            }
        } else if (yellowAlarmCount > 0) {
            // 状态恢复正常，自动取消黄色报警
            baseMapper.handleYellowAlarms(machineId, stationId);
        }
    }

    private void createAlarmFromDetection(Long machineId, Long stationId, ShootMoldRuleEntity rule,
                                          BigDecimal currentValue, String plcFieldKey) {
        try {
            String alarmFieldCode = StrUtil.isNotBlank(plcFieldKey) ? plcFieldKey : rule.getFieldCode();

            // 去重：同机器+站位+规则(+字段) 已存在未处理且当前值相等的红色报警时，不再重复插入。
            // 即同一超标值只报一次；值变化（如60→70）才新增一条；已报过的值不再重复报。
            long existingCount = baseMapper.existsUnhandledRedAlarmWithValue(
                    machineId, stationId, rule.getId(), plcFieldKey, currentValue);
            if (existingCount > 0) {
                return;
            }

            String alarmFieldName = StrUtil.isNotBlank(plcFieldKey) ? plcFieldKey : rule.getFieldName();
            baseMapper.insertRedAlarm(machineId, stationId, rule.getMoldId(), rule.getId(),
                    alarmFieldCode, alarmFieldName, rule.getMinValue(), rule.getMaxValue(), currentValue);
            log.info("创建红色报警: machineId={}, stationId={}, ruleId={}, fieldCode={}, currentValue={}",
                    machineId, stationId, rule.getId(), alarmFieldCode, currentValue);
        } catch (Exception e) {
            log.error("Create alarm failed: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getId(), rule.getFieldCode(), e);
        }
    }

    /**
     * 当参数恢复正常时，自动取消该规则（或具体字段）的红色报警
     */
    private void autoCancelRedAlarmWhenNormal(Long machineId, Long stationId, ShootMoldRuleEntity rule, String plcFieldKey) {
        try {
            long unhandledCount = baseMapper.countUnhandledRedAlarms(machineId, stationId, rule.getId());
            if (unhandledCount > 0) {
                // 自动取消红色报警（plcFieldKey 为空取消该规则全部，非空仅取消该字段）
                int cancelledCount = baseMapper.autoCancelRedAlarms(machineId, stationId, rule.getId(), plcFieldKey);
                if (cancelledCount > 0) {
                    log.info("参数恢复正常，自动取消{}条红色报警: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                            cancelledCount, machineId, stationId, rule.getId(), plcFieldKey);
                }
            }
        } catch (Exception e) {
            log.error("自动取消红色报警失败: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getId(), rule.getFieldCode(), e);
        }
    }

}
