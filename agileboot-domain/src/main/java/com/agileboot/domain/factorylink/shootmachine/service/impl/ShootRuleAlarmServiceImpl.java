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
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShootRuleAlarmServiceImpl extends ServiceImpl<ShootRuleAlarmMapper, ShootRuleAlarmEntity>
        implements ShootRuleAlarmService {

    private static final int ALARM_DEDUP_MINUTES = 1;      // 报警去重：1分钟内

    // ====== 报警阈值常量 ======
    private static final int DATA_STALE_MINUTES = 15;       // 数据超时停机：15分钟未更新
    private static final int MOLD_TIMEOUT_SECONDS = 55;     // 操作超时：合模止=OFF（生产中）持续≥55秒未变成ON
    private static final int MOLD_STOP_MINUTES = 5;         // 停机报警：合模止=OFF（生产中）持续≥5分钟未变成ON
    private static final int AUTO_HANDLE_SECONDS = 10;      // 操作超时自动处理：报警超过10秒后自动关闭

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
    public AlarmExportResult listAllForExport(Long machineId, Integer days) {
        List<ShootRuleAlarmEntity> alarms = baseMapper.selectAllWithRelation(machineId, days);
        List<ShootRuleAlarmExportDTO> redList = new ArrayList<>();
        List<ShootRuleAlarmExportDTO> yellowList = new ArrayList<>();
        for (ShootRuleAlarmEntity alarm : alarms) {
            boolean isYellow = "yellow".equals(alarm.getAlarmLevel());
            ShootRuleAlarmExportDTO dto = convertToExportDto(alarm, isYellow);
            (isYellow ? yellowList : redList).add(dto);
        }
        return new AlarmExportResult(redList, yellowList);
    }

    /**
     * 单个报警转导出 DTO：黄色报警显示超时时间不显示阈值，红色报警显示阈值不显示超时时间
     */
    private ShootRuleAlarmExportDTO convertToExportDto(ShootRuleAlarmEntity alarm, boolean isYellow) {
        ShootRuleAlarmExportDTO dto = new ShootRuleAlarmExportDTO();
        dto.setMachineName(alarm.getMachineName());
        dto.setStationName(alarm.getStationName());
        dto.setFieldName(alarm.getFieldName());
        dto.setAlarmLevel(isYellow ? "黄色" : "红色");
        if (isYellow) {
            // 黄色报警（操作超时/停机）：不显示阈值、当前值与模具信息，仅显示超时时间
            // 超时时间 = 经过秒数 - 阈值，显示实际超出的秒数
            dto.setMinValue("");
            dto.setMaxValue("");
            long rawSeconds = alarm.getCurrentValue() != null ? alarm.getCurrentValue().longValue() : 0;
            long threshold = 0;
            if ("operation_timeout".equals(alarm.getFieldCode())) {
                threshold = MOLD_TIMEOUT_SECONDS;
            } else if ("stop_no_mold_close".equals(alarm.getFieldCode())) {
                threshold = MOLD_STOP_MINUTES * 60L;
            }
            long displaySeconds = Math.max(rawSeconds - threshold, 0);
            dto.setTimeoutSeconds(String.valueOf(displaySeconds));
            dto.setMoldModel("");
            dto.setMoldColor("");
        } else {
            // 红色报警（阈值超标）：显示阈值与模具信息，不显示超时时间
            dto.setMinValue(alarm.getMinValue() != null ? alarm.getMinValue().toString() : "");
            dto.setCurrentValue(alarm.getCurrentValue() != null ? alarm.getCurrentValue().toString() : "");
            dto.setMaxValue(alarm.getMaxValue() != null ? alarm.getMaxValue().toString() : "");
            dto.setTimeoutSeconds("");
            dto.setMoldModel(alarm.getMoldModel());
            dto.setMoldColor(alarm.getMoldColor());
        }
        dto.setAlarmTime(alarm.getAlarmTime() != null ? alarm.getAlarmTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
        dto.setHandleStatus("true".equals(alarm.getHandleStatus()) ? "已处理" : "未处理");
        return dto;
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

        // 优先执行5分钟停机检测（先创建 stop_no_mold_close）
        // 这样后续 detectDataStaleAlarm 检查时能正确判断优先级
        detectMoldStateAlarmsFromPlc(machineId);

        // 15分钟停机检测（会检查是否已有5分钟停机，有则跳过）
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
        Set<String> insertedKeys = new HashSet<>();

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
                log.info("[RedAlarm] 规则解析: ruleId={}, fieldCode={}, moldSide={}, plcFieldKeys={}, queryCategoryName={}",
                        rule.getId(), rule.getFieldCode(), moldSide, plcFieldKeys, queryCategoryName);

                for (String plcFieldKey : plcFieldKeys) {
                    PlcDataLatestEntity fieldData = queryPlcFieldByCategory(machineId, queryCategoryName, plcFieldKey);
                    if (fieldData == null) {
                        log.info("[RedAlarm] 未匹配PLC数据，跳过: machineId={}, stationNo={}, ruleId={}, fieldCode={}, queryCategoryName={}, plcFieldKey={}",
                                machineId, stationNo, rule.getId(), rule.getFieldCode(), queryCategoryName, plcFieldKey);
                        continue;
                    }

                    BigDecimal currentValue = Convert.toBigDecimal(fieldData.getFieldValue(), null);
                    if (currentValue == null) {
                        log.info("PLC字段值为空: machineId={}, plcFieldKey={}, fieldValue={}", machineId, plcFieldKey, fieldData.getFieldValue());
                        continue;
                    }

                    log.info("PLC字段值: machineId={}, plcFieldKey={}, currentValue={}, minValue={}, maxValue={}", 
                            machineId, plcFieldKey, currentValue, rule.getMinValue(), rule.getMaxValue());

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

    /**
     * 按分类（categoryName）查询PLC数据
     * 射枪温度字段：严格按指定温度组查询，不回退到全局（避免4射枪温度组数据污染2射枪温度组）
     * 其他字段：先按分类查，查不到再回退到全局（用于站台级别字段）
     */
    private PlcDataLatestEntity queryPlcFieldByCategory(Long machineId, String categoryName, String fieldKey) {
        // trim防止第三方传入的field_key带空格导致查询失败
        if (fieldKey != null) fieldKey = fieldKey.trim();
        log.info("[PLC查询] 精确查询: machineId={}, fieldKey={}, categoryName={}", machineId, fieldKey, categoryName);
        PlcDataLatestEntity fieldData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(
                machineId, fieldKey, categoryName);
        if (fieldData != null) {
            log.info("[PLC查询] 精确查询命中: machineId={}, fieldKey={}, categoryName={}, fieldValue={}, timestamp={}",
                    machineId, fieldKey, categoryName, fieldData.getFieldValue(), fieldData.getDataTimestamp());
            return fieldData;
        }
        log.info("[PLC查询] 精确查询未命中: machineId={}, fieldKey={}, categoryName={}", machineId, fieldKey, categoryName);
        // 射枪温度字段不回退到全局查询，严格按指定温度组匹配
        if (categoryName.contains("射枪温度")) {
            log.info("[PLC查询] 射枪温度按分类未找到，不回退全局: machineId={}, categoryName={}, fieldKey={}", machineId, categoryName, fieldKey);
            return null;
        }
        // 其他字段回退到全局查询
        log.info("[PLC查询] 回退全局查询: machineId={}, fieldKey={}", machineId, fieldKey);
        fieldData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyForGlobal(machineId, fieldKey);
        if (fieldData == null) {
            log.info("[PLC查询] 全局查询也未找到: machineId={}, fieldKey={}", machineId, fieldKey);
        } else {
            log.info("[PLC查询] 全局查询命中: machineId={}, fieldKey={}, categoryName={}, fieldValue={}, timestamp={}",
                    machineId, fieldKey, fieldData.getCategoryName(), fieldData.getFieldValue(), fieldData.getDataTimestamp());
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

            // 一次查出该站位需要的字段
            PlcDataLatestEntity settingTimeData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(
                    machineId, "设定加硫时间", categoryName);
            if (settingTimeData == null) {
                log.info("[YellowAlarm] 未找到设定加硫时间: machineId={}, stationNo={}, categoryName={}, fieldKey=设定加硫时间", machineId, stationNo, categoryName);
            } else {
                log.info("[YellowAlarm] 设定加硫时间: machineId={}, stationNo={}, categoryName={}, fieldValue={}, timestamp={}", machineId, stationNo, categoryName, settingTimeData.getFieldValue(), settingTimeData.getDataTimestamp());
            }
            PlcDataLatestEntity heMoData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(
                    machineId, "合模止", categoryName);
            if (heMoData == null) {
                log.info("[YellowAlarm] 未找到合模止: machineId={}, stationNo={}, categoryName={}, fieldKey=合模止", machineId, stationNo, categoryName);
            } else {
                log.info("[YellowAlarm] 合模止: machineId={}, stationNo={}, categoryName={}, fieldValue={}, timestamp={}", machineId, stationNo, categoryName, heMoData.getFieldValue(), heMoData.getDataTimestamp());
            }

            // ====== 1. 数据超时停机：设定加硫时间 15分钟未更新 → 黄色报警（一次停机一条，按fieldCode去重） ======
            detectDataStaleAlarm(machineId, stationId, stationNo, categoryName, settingTimeData, staleThreshold, dedupSince, now);

            // ====== 2. 生产超时：合模止=OFF（在生产）持续≥5分钟/55秒未变成ON（一次停机一条） ======
            detectMoldStateAlarms(machineId, stationId, stationNo, categoryName, moldId, fieldCodeToRuleIdMap, heMoData, now, dedupSince);
        }
    }

    /**
     * 检测数据超时：设定加硫时间超过15分钟未更新 → 黄色报警（15分钟未加硫停机）
     * 不走模具阈值和排期，直接从PLC数据检测
     */
    private void detectDataStaleAlarm(Long machineId, Long stationId, Integer stationNo, String categoryName,
                                      PlcDataLatestEntity latestData, LocalDateTime staleThreshold,
                                      LocalDateTime dedupSince, LocalDateTime now) {
        if (latestData == null) {
            return;
        }

        LocalDateTime updateTime = latestData.getCreateTime() != null ? latestData.getCreateTime() : latestData.getDataTimestamp();
        if (updateTime == null) {
            return;
        }

        boolean isStale = updateTime.isBefore(staleThreshold);
        String fieldCode = "she_ding_jia_liu_time";

        if (isStale) {
            // 优先级：5分钟未合模停机 > 15分钟未加硫停机
            // 如果该站位已有未处理的5分钟停机报警，则不创建15分钟报警
            long stopAlarmCount = baseMapper.countRecentSameYellowAlarmByFieldCode(machineId, stationId, "stop_no_mold_close");
            if (stopAlarmCount > 0) {
                return;
            }
            // 按 fieldCode 去重：同站位已有未处理的15分钟报警则不创建
            long sameYellowAlarmCount = baseMapper.countRecentSameYellowAlarmByFieldCode(machineId, stationId, fieldCode);
            if (sameYellowAlarmCount == 0) {
                BigDecimal fieldValue = Convert.toBigDecimal(latestData.getFieldValue(), null);
                long currentValue = fieldValue != null ? fieldValue.longValue() : 0;
                baseMapper.insertYellowAlarm(machineId, stationId, null, null, fieldCode, "15分钟未加硫停机", currentValue);
                log.info("创建数据超时黄色报警: machineId={}, stationId={}, categoryName={}, lastUpdateTime={}",
                        machineId, stationId, categoryName, updateTime);
            }
        } else {
            // 数据已更新，取消该站位的15分钟未加硫停机报警，不影响其他黄色报警
            baseMapper.handleYellowAlarmsByFieldCodes(machineId, stationId, List.of(fieldCode));
        }
    }

    /**
     * 检测模具生产超时：
     * - 合模止=OFF（在生产）且持续≥55秒未变成ON → 黄色报警（操作超时）
     * - 合模止=OFF（在生产）且持续≥5分钟未变成ON → 黄色报警（5分钟未合模停机）
     * - 合模止恢复ON → 自动关闭该站位的停机报警（5分钟未合模）
     * - 一次停机事件 = 一条报警，不反复重建
     */
    private void detectMoldStateAlarms(Long machineId, Long stationId, Integer stationNo, String categoryName,
                                       Long moldId, Map<String, Long> fieldCodeToRuleIdMap,
                                       PlcDataLatestEntity heMoData,
                                       LocalDateTime now, LocalDateTime dedupSince) {
        if (heMoData == null) {
            return;
        }

        String heMoValue = heMoData.getFieldValue();
        // 用 value_changed_at：值首次变为 OFF 的时间，而非每次同步覆盖的 timestamp
        LocalDateTime heMoTime = heMoData.getValueChangedAt() != null
                ? heMoData.getValueChangedAt() : heMoData.getDataTimestamp();

        if (heMoTime == null) {
            return;
        }

        // 合模止=OFF 表示在生产
        boolean isProducing = "OFF".equalsIgnoreCase(heMoValue);

        if (isProducing) {
            long elapsedSeconds = java.time.Duration.between(heMoTime, now).getSeconds();

            // 5分钟未合模 → 停机黄色报警（只产生一条，按 fieldCode 去重）
            if (elapsedSeconds >= MOLD_STOP_MINUTES * 60) {
                String fieldCode = "stop_no_mold_close";
                Long ruleId = fieldCodeToRuleIdMap.get(fieldCode);
                long sameYellowAlarmCount = baseMapper.countRecentSameYellowAlarmByFieldCode(machineId, stationId, fieldCode);
                if (sameYellowAlarmCount == 0) {
                    baseMapper.insertYellowAlarm(machineId, stationId, moldId, ruleId,
                            fieldCode, "5分钟未合模停机", elapsedSeconds);
                    log.info("创建5分钟未合模停机黄色报警: stationId={}, stationNo={}, 已过{}秒",
                            stationId, stationNo, elapsedSeconds);
                }
            }
        } else {
            // 合模止恢复ON → 自动关闭该站位的5分钟未合模停机报警
            baseMapper.handleYellowAlarmsByFieldCodes(machineId, stationId, List.of("stop_no_mold_close"));
        }
    }

    /**
     * 直接从PLC数据检测生产超时（不依赖排期和模具阈值）
     * 查询所有站位的合模止数据，合模止=OFF（在生产）且数据时间戳超过60秒未更新 → 黄色报警
     */
    private void detectMoldStateAlarmsFromPlc(Long machineId) {
        LocalDateTime now = LocalDateTime.now();

        // 查询该机器的所有站位
        List<ShootMachineStationEntity> stations = shootMachineStationService.lambdaQuery()
                .eq(ShootMachineStationEntity::getMachineId, machineId)
                .list();

        for (ShootMachineStationEntity station : stations) {
            Long stationId = station.getId();
            Integer stationNo = station.getStationNo();
            if (stationId == null || stationNo == null) {
                continue;
            }

            String categoryName = "站台" + stationNo;

            // 查询合模止数据
            PlcDataLatestEntity heMoData = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(
                    machineId, "合模止", categoryName);
            if (heMoData == null) {
                log.info("[MoldState] 未找到合模止: machineId={}, stationNo={}, categoryName={}, fieldKey=合模止", machineId, stationNo, categoryName);
                continue;
            }
            log.info("[MoldState] 合模止: machineId={}, stationNo={}, categoryName={}, fieldValue={}, timestamp={}", machineId, stationNo, categoryName, heMoData.getFieldValue(), heMoData.getDataTimestamp());

            String heMoValue = heMoData.getFieldValue();
            // 用 value_changed_at：值首次变为 OFF 的时间，而非每次同步覆盖的 timestamp
            LocalDateTime heMoTime = heMoData.getValueChangedAt() != null
                    ? heMoData.getValueChangedAt() : heMoData.getDataTimestamp();
            if (heMoTime == null) {
                continue;
            }

            long elapsedSeconds = java.time.Duration.between(heMoTime, now).getSeconds();

            // 合模止=OFF 表示在生产
            boolean isProducing = "OFF".equalsIgnoreCase(heMoValue);
            if (!isProducing) {
                // 合模止恢复ON → 自动关闭该站位的5分钟未合模停机报警
                baseMapper.handleYellowAlarmsByFieldCodes(machineId, stationId, List.of("stop_no_mold_close"));
                log.info("合模止恢复ON，关闭停机报警: stationId={}, heMoValue={}", stationId, heMoValue);
                continue;
            }

            // 操作超时报警超过10秒自动关闭（仅操作超时，停机报警不自动关闭）
            int handledTimeout = baseMapper.handleExpiredYellowAlarmsByFieldCode(machineId, stationId, "operation_timeout", AUTO_HANDLE_SECONDS);
            if (handledTimeout > 0) {
                log.info("自动处理操作超时报警: stationId={}, count={}", stationId, handledTimeout);
            }

            log.info("操作超时检测: stationId={}, stationNo={}, heMoValue={}, heMoTime={}, elapsedSeconds={}, MOLD_TIMEOUT_SECONDS={}",
                    stationId, stationNo, heMoValue, heMoTime, elapsedSeconds, MOLD_TIMEOUT_SECONDS);

            // 5分钟未合模 → 停机黄色报警（只产生一条，按 fieldCode 去重）
            long stopThreshold = MOLD_STOP_MINUTES * 60;
            if (elapsedSeconds >= stopThreshold) {
                long sameYellowAlarmCount = baseMapper.countRecentSameYellowAlarmByFieldCode(machineId, stationId, "stop_no_mold_close");
                if (sameYellowAlarmCount == 0) {
                    baseMapper.insertYellowAlarm(machineId, stationId, null, null, "stop_no_mold_close", "5分钟未合模停机", elapsedSeconds);
                    log.info("创建5分钟未合模停机黄色报警: stationId={}, stationNo={}, elapsedSeconds={}", stationId, stationNo, elapsedSeconds);
                } else {
                    log.info("跳过创建停机报警(去重): stationId={}, stationNo={}", stationId, stationNo);
                }
            } else if (elapsedSeconds >= MOLD_TIMEOUT_SECONDS) {
                // 操作超时 → 黄色报警（未达到5分钟停机阈值时触发）
                long sameYellowAlarmCount = baseMapper.countRecentSameYellowAlarmByFieldCode(machineId, stationId, "operation_timeout");
                log.info("去重检查: stationId={}, sameYellowAlarmCount={}", stationId, sameYellowAlarmCount);
                if (sameYellowAlarmCount == 0) {
                    baseMapper.insertYellowAlarm(machineId, stationId, null, null, "operation_timeout", "操作超时", elapsedSeconds);
                    log.info("创建操作超时黄色报警: stationId={}, stationNo={}, elapsedSeconds={}", stationId, stationNo, elapsedSeconds);
                } else {
                    log.info("跳过创建操作超时报警(去重): stationId={}, stationNo={}", stationId, stationNo);
                }
            }
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
            log.info("检查红色报警: machineId={}, stationId={}, ruleId={}, fieldCode={}, unhandledCount={}",
                    machineId, stationId, rule.getId(), plcFieldKey, unhandledCount);
            if (unhandledCount > 0) {
                // 自动取消红色报警（plcFieldKey 为空取消该规则全部，非空仅取消该字段）
                int cancelledCount = baseMapper.autoCancelRedAlarms(machineId, stationId, rule.getId(), plcFieldKey);
                if (cancelledCount > 0) {
                    log.info("参数恢复正常，自动取消{}条红色报警: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                            cancelledCount, machineId, stationId, rule.getId(), plcFieldKey);
                } else {
                    log.info("未取消红色报警: machineId={}, stationId={}, ruleId={}, fieldCode={}, 可能field_code不匹配",
                            machineId, stationId, rule.getId(), plcFieldKey);
                }
            }
        } catch (Exception e) {
            log.error("自动取消红色报警失败: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getId(), rule.getFieldCode(), e);
        }
    }

}
