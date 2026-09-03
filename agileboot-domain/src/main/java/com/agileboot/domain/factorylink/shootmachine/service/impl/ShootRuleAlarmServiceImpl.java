package com.agileboot.domain.factorylink.shootmachine.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.factorylink.plc.entity.FieldMappingEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.mapper.FieldMappingMapper;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
import com.agileboot.domain.factorylink.plc.util.FieldMatchingEngine;
import com.agileboot.domain.factorylink.shootmachine.dto.AlarmPageResponse;
import com.agileboot.domain.factorylink.shootmachine.entity.AlarmExportResult;
import com.agileboot.domain.factorylink.shootmachine.entity.*;
import com.agileboot.domain.factorylink.shootmachine.mapper.AlarmStateRuleMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootRuleAlarmMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineStationService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldRuleService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootStationScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShootRuleAlarmServiceImpl extends ServiceImpl<ShootRuleAlarmMapper, ShootRuleAlarmEntity>
        implements ShootRuleAlarmService {

    private final ShootStationScheduleService shootStationScheduleService;
    private final ShootMachineStationService shootMachineStationService;
    private final ShootMoldRuleService shootMoldRuleService;
    private final PlcDataLatestMapper plcDataLatestMapper;
    private final ShootMachineMapper shootMachineMapper;
    private final AlarmStateRuleMapper alarmStateRuleMapper;
    private final FieldMappingMapper fieldMappingMapper;

    /** 黄色报警导出上限：超过则只保留最近 3000 条 */
    private static final int YELLOW_EXPORT_LIMIT = 3000;

    /** 报警字段的阈值规则被删除/清空后自动取消的处理备注 */
    private static final String RULE_REMOVED_CANCEL_REMARK = "阈值已删除自动取消";

    private Map<String, AlarmStateRuleEntity> alarmStateRuleCache = new LinkedHashMap<>();
    private Map<String, List<FieldMappingEntity>> fieldMappingByInternalKey = new HashMap<>();
    private Map<String, FieldMappingEntity> fieldMappingByMatchPattern = new HashMap<>();

    @PostConstruct
    public void initAlarmCaches() {
        List<AlarmStateRuleEntity> rules = alarmStateRuleMapper.listEnabled();
        rules.sort(Comparator.comparing(AlarmStateRuleEntity::getPriority).reversed());
        for (AlarmStateRuleEntity r : rules) {
            alarmStateRuleCache.put(r.getRuleCode(), r);
        }
        log.debug("[AlarmInit] alarm_state_rule 加载完成: {} 条", rules.size());

        List<FieldMappingEntity> mappings = fieldMappingMapper.listEnabled();
        for (FieldMappingEntity m : mappings) {
            fieldMappingByInternalKey.computeIfAbsent(m.getInternalKey(), k -> new ArrayList<>()).add(m);
            if (StrUtil.isNotBlank(m.getMatchPattern())) {
                fieldMappingByMatchPattern.put(m.getMatchPattern(), m);
            }
        }
        log.debug("[AlarmInit] field_mapping 加载完成: {} 条", mappings.size());
    }

    @Override
    public List<ShootRuleAlarmEntity> listUnhandledWithRelation(Long machineId) {
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
        // 红色：预聚合导出；黄色：逐条如实导出（次数不累加、超时时间不取最大值）
        List<ShootRuleAlarmEntity> redAlarms = baseMapper.selectAllRedForExport(machineId, days);
        List<ShootRuleAlarmEntity> yellowAlarms = baseMapper.selectAllYellowForExport(machineId, days);
        // 黄色上限保护：超过 3000 条则截断，仅保留最近 3000 条，并标记已截断用于提示
        boolean yellowTruncated = yellowAlarms.size() > YELLOW_EXPORT_LIMIT;
        if (yellowTruncated) {
            yellowAlarms = yellowAlarms.subList(0, YELLOW_EXPORT_LIMIT);
        }
        List<ShootRuleAlarmExportDTO> redList = new ArrayList<>();
        List<ShootRuleAlarmExportDTO> yellowList = new ArrayList<>();
        for (ShootRuleAlarmEntity alarm : redAlarms) {
            redList.add(convertToExportDto(alarm, false));
        }
        for (ShootRuleAlarmEntity alarm : yellowAlarms) {
            yellowList.add(convertToExportDto(alarm, true));
        }
        return new AlarmExportResult(redList, yellowList, yellowTruncated);
    }

    private ShootRuleAlarmExportDTO convertToExportDto(ShootRuleAlarmEntity alarm, boolean isYellow) {
        ShootRuleAlarmExportDTO dto = new ShootRuleAlarmExportDTO();
        dto.setMachineName(alarm.getMachineName());
        dto.setStationName(alarm.getStationName());
        dto.setFieldName(alarm.getFieldName());
        dto.setAlarmLevel(isYellow ? "黄色" : "红色");
        // 黄色逐条导出：一次报警记录即一次出现，不累加
        dto.setOccurrenceCount(isYellow ? "1" : (alarm.getOccurrenceCount() != null ? String.valueOf(alarm.getOccurrenceCount()) : ""));
        String fmt = "yyyy-MM-dd HH:mm:ss";
        dto.setFirstAlarmTime(alarm.getFirstAlarmTime() != null ? alarm.getFirstAlarmTime().format(DateTimeFormatter.ofPattern(fmt)) : "");
        dto.setLastAlarmTime(alarm.getLastAlarmTime() != null ? alarm.getLastAlarmTime().format(DateTimeFormatter.ofPattern(fmt)) : "");
        if (isYellow) {
            // 黄色导出：首次报警 = 告警产生时间；末次报警 = 处理时间，未处理则不显示
            dto.setFirstAlarmTime(alarm.getAlarmTime() != null ? alarm.getAlarmTime().format(DateTimeFormatter.ofPattern(fmt)) : "");
            if ("true".equals(alarm.getHandleStatus()) && alarm.getUpdatedAt() != null) {
                dto.setLastAlarmTime(alarm.getUpdatedAt().format(DateTimeFormatter.ofPattern(fmt)));
            } else {
                dto.setLastAlarmTime("");
            }
            dto.setMinValue("");
            dto.setMaxValue("");
            long exceededSeconds = alarm.getCurrentValue() != null ? alarm.getCurrentValue().longValue() : 0;
            dto.setTimeoutSeconds(String.valueOf(exceededSeconds));
            dto.setMoldModel("");
            dto.setMoldColor("");
        } else {
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
        // 黄色报警为站台级状态监测，与排期无关：无论有无排期都统一按站台轮询评估
        detectYellowAlarmsByMachine(machineId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void detectAlarmsByPlcData(Long machineId) {
        List<ShootStationScheduleEntity> schedules =
                shootStationScheduleService.listCurrentByMachineId(machineId);

        ShootMachineEntity machine = shootMachineMapper.selectById(machineId);
        Integer gunCountVal = machine != null ? machine.getGunCount() : null;
        int gunCount = gunCountVal != null ? gunCountVal : 4;

        // 按 moldId 批量预加载规则，避免同一模具的多个站台重复查询
        Set<Long> moldIds = schedules.stream()
                .map(ShootStationScheduleEntity::getMoldId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, List<ShootMoldRuleEntity>> rulesByMold = new HashMap<>();
        for (Long moldId : moldIds) {
            rulesByMold.put(moldId, shootMoldRuleService.listByMoldId(moldId));
        }

        Set<String> insertedKeys = new HashSet<>();

        // 按站位分组排期，供 detectSideLessGlobalRedAlarms 聚合无 side 维度的 GLOBAL 字段
        Map<Long, List<ShootStationScheduleEntity>> schedulesByStation = schedules.stream()
                .collect(Collectors.groupingBy(ShootStationScheduleEntity::getStationId));

        // 黄色报警为站台级状态监测，排期无关：始终按机器所有站台轮询（无排期=停机，仍须监测/计时/恢复取消）
        detectYellowAlarmsByMachine(machineId);

        for (ShootStationScheduleEntity schedule : schedules) {
            detectRedAlarmsFromPlcDataSingle(machineId, schedule, gunCount, insertedKeys, rulesByMold);
        }

        // 无 side 维度的 GLOBAL 字段（如 SET_CURE_TIME）按站位聚合判断：
        // 任一模具规则超限即报警，所有模具规则都正常才取消
        detectSideLessGlobalRedAlarms(machineId, schedulesByStation, gunCount, rulesByMold, insertedKeys);

        int orphanCancelled = baseMapper.autoCancelAlarmsWithoutCurrentSchedule();
        if (orphanCancelled > 0) {
            log.info("自动取消{}条无当前排期的红色报警", orphanCancelled);
        }

        int recoveredCancelled = autoCancelRecoveredRedAlarmsJava();
        if (recoveredCancelled > 0) {
            log.info("自动取消{}条参数已恢复的红色报警", recoveredCancelled);
        }
    }

    /**
     * 兜底自动取消：遍历未处理红色报警，取该报警对应站台的最新 PLC 实时值，
     * 若已恢复到当前模具对应字段规则的阈值内则标记为已处理。
     * 用 Java 层实现而非复杂 UPDATE SQL——避免 MyBatis-Plus 的 JSqlParser 无法解析
     * 多表 JOIN / 窗口函数而抛异常，导致整个检测事务回滚、报警全部消失。
     * 注意：规则被删除重建后 rule_id 变化，报警仍引用旧 rule_id，故按 field_code 前缀匹配当前存活规则，
     * 不再依赖报警自身的 rule_id（保证「阈值修改后报警能自动消除」）。
     */
    private int autoCancelRecoveredRedAlarmsJava() {
        int cancelled = 0;
        List<Map<String, Object>> alarms = baseMapper.selectUnhandledRedAlarms();
        for (Map<String, Object> alarm : alarms) {
            try {
                Long id = Convert.toLong(alarm.get("id"));
                Long machineId = Convert.toLong(alarm.get("machineId"));
                Long stationId = Convert.toLong(alarm.get("stationId"));
                Long moldId = Convert.toLong(alarm.get("moldId"));
                String fieldCode = (String) alarm.get("fieldCode");
                if (id == null || machineId == null || moldId == null || StrUtil.isBlank(fieldCode)) {
                    continue;
                }

                // 取该报警对应站台的最新实时值（GUN_TEMP 不参与站台维度）
                String stationName = stationId != null ? baseMapper.selectStationNameById(stationId) : null;
                boolean isGunTemp = fieldCode.startsWith("GUN_TEMP");
                PlcDataLatestEntity data;
                if (isGunTemp) {
                    data = plcDataLatestMapper.selectLatestByMachineIdAndFieldKey(machineId, fieldCode);
                } else {
                    data = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(machineId, fieldCode, stationName);
                }
                if (data == null) {
                    continue;
                }
                BigDecimal currentValue = Convert.toBigDecimal(data.getFieldValue(), null);
                if (currentValue == null) {
                    continue;
                }

                // 查当前模具对应字段的有效规则阈值：
                //  - GLOBAL 字段（MOLD_SET_TEMP/SET_CURE_TIME/INJECT_PRESS）直接按 internal_key 前缀匹配；
                //  - STAGE 字段（GUN_TEMP/INJECT_SPEED）需按 field_code 末尾的 stage 匹配对应阶段规则，
                //    否则会误用别的 stage 的阈值（例如 stage=2 的值被拿 stage=1 阈值判断）。
                Integer fieldStage = extractStageFromFieldCode(fieldCode);
                ShootMoldRuleEntity matched = null;
                for (ShootMoldRuleEntity rule : shootMoldRuleService.listByMoldId(moldId)) {
                    if (Boolean.FALSE.equals(rule.getEnabled()) || StrUtil.isBlank(rule.getFieldCode())) {
                        continue;
                    }
                    if (!fieldCode.startsWith(rule.getFieldCode())) {
                        continue;
                    }
                    if ("GLOBAL".equals(rule.getDimensionType())) {
                        matched = rule;
                        break;
                    }
                    if ("STAGE".equals(rule.getDimensionType())
                            && fieldStage != null && Objects.equals(fieldStage, rule.getStage())) {
                        matched = rule;
                        break;
                    }
                }
                if (matched == null) {
                    // 该报警字段在当前模具已无存活阈值规则（阈值被清空/删除），
                    // 不再有判定依据，视为失去约束自动取消，避免死报警一直挂在看板
                    int n = baseMapper.cancelRedAlarmByIdWithRemark(id, RULE_REMOVED_CANCEL_REMARK);
                    if (n > 0) {
                        cancelled++;
                        log.info("阈值规则已删除，自动取消红色报警: alarmId={}, fieldCode={}", id, fieldCode);
                    }
                    continue;
                }

                boolean recovered = currentValue.compareTo(matched.getMinValue()) >= 0
                        && currentValue.compareTo(matched.getMaxValue()) <= 0;
                if (recovered) {
                    int n = baseMapper.cancelRedAlarmById(id);
                    if (n > 0) {
                        cancelled++;
                        log.info("参数恢复正常，自动取消红色报警: alarmId={}, fieldCode={}, currentValue={}, ruleId={}",
                                id, fieldCode, currentValue, matched.getId());
                    }
                }
            } catch (Exception e) {
                log.warn("Java 兜底自动取消红色报警失败: alarmId={}, fieldCode={}", alarm.get("id"), alarm.get("fieldCode"), e);
            }
        }
        return cancelled;
    }

    /**
     * 从 field_code 提取 stage（最后一段 "_数字"）。
     * STAGE 字段的 field_code 维度顺序为 side-gun-stage-idx（如 GUN_TEMP_1_1 / INJECT_SPEED_L_2），
     * stage 总是最后一段数字；GLOBAL 字段无 stage，返回 null。
     */
    private Integer extractStageFromFieldCode(String fieldCode) {
        if (StrUtil.isBlank(fieldCode)) {
            return null;
        }
        int idx = fieldCode.lastIndexOf('_');
        if (idx < 0 || idx == fieldCode.length() - 1) {
            return null;
        }
        String tail = fieldCode.substring(idx + 1);
        try {
            int stage = Integer.parseInt(tail);
            return stage > 0 ? stage : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 黄色（状态类）报警：站台级状态机，排期无关。
     * <p>
     * 遍历机器所有站台，每站台独立评估。无排期 = 停机，仍须监测并连续计时；
     * 状态恢复（如合模止 OFF→ON）即取消，不再依赖排期循环。
     * </p>
     */
    private void detectYellowAlarmsByMachine(Long machineId) {
        ShootMachineEntity machine = shootMachineMapper.selectById(machineId);
        if (machine == null || !Boolean.TRUE.equals(machine.getEnabled())) {
            // 机台未启用：不做黄色监管，并清理该机器既有未处理黄色报警，避免历史残留/停用机台误报
            int cleared = baseMapper.handleYellowAlarmsByMachine(machineId);
            if (cleared > 0) {
                log.info("[YellowAlarm] 机台未启用，取消 {} 条黄色报警: machineId={}", cleared, machineId);
            }
            return;
        }
        List<ShootMachineStationEntity> stations = shootMachineStationService.listByMachineId(machineId);
        if (stations == null || stations.isEmpty()) {
            log.debug("[YellowAlarm] 该机器无站台，跳过: machineId={}", machineId);
            return;
        }
        for (ShootMachineStationEntity station : stations) {
            detectYellowAlarmsByStation(machineId, station);
        }
    }

    private void detectYellowAlarmsByStation(Long machineId, ShootMachineStationEntity station) {
        Long stationId = station.getId();
        Integer stationNo = station.getStationNo();
        if (stationId == null || stationNo == null) {
            return;
        }

        // 站台级状态监测，黄色字段（如 MOLD_CLOSE / SET_CURE_TIME）无 side/gun 维度
        String moldSide = null;
        Integer gunNo = null;
        int gunCount = 4;
        ShootMachineEntity machine = shootMachineMapper.selectById(machineId);
        if (machine != null && machine.getGunCount() != null) {
            gunCount = machine.getGunCount();
        }

        LocalDateTime now = LocalDateTime.now();

        // 该站位当前仍未处理的黄色报警 rule_code（field_code 列复用存 rule_code）
        Set<String> unhandledRuleCodes = new HashSet<>(
                baseMapper.selectUnhandledYellowFieldCodes(machineId, stationId));

        // 按监测字段分组（同一字段的多条规则共用同一份 PLC 数据评估）
        Map<String, List<AlarmStateRuleEntity>> rulesBySourceKey = new LinkedHashMap<>();
        for (AlarmStateRuleEntity rule : alarmStateRuleCache.values()) {
            if (StrUtil.isBlank(rule.getSourceInternalKey())) {
                continue;
            }
            rulesBySourceKey.computeIfAbsent(rule.getSourceInternalKey(), k -> new ArrayList<>()).add(rule);
        }

        // 全局互斥：对所有"有 PLC 数据且能判定"的规则，挑 priority 最高的触发者作为唯一胜出者，
        // 其余已评估规则全部取消。互斥跨 source 字段生效（如 DATA_STALE_CURE 与 STOP_NO_MOLD_CLOSE 也互斥），
        // 不再局限于同一字段内部。无数据/无法判定的规则保持现状，不被误清。
        AlarmStateRuleEntity winner = null;
        long winnerElapsedSeconds = 0;
        Set<String> evaluatedRuleCodes = new HashSet<>();

        for (Map.Entry<String, List<AlarmStateRuleEntity>> group : rulesBySourceKey.entrySet()) {
            String sourceInternalKey = group.getKey();

            List<FieldMappingEntity> mappings = fieldMappingByInternalKey.get(sourceInternalKey);
            if (mappings == null || mappings.isEmpty()) {
                log.debug("[YellowAlarm] 未找到field_mapping: internalKey={}", sourceInternalKey);
                continue;
            }

            for (FieldMappingEntity mapping : mappings) {
                String categoryName = FieldMatchingEngine.buildCategoryName(mapping.getCategoryTemplate(), stationNo, gunCount);
                String fieldKey = renderFieldKeyForAlarm(mapping, moldSide, gunNo, null, stationNo);
                if (fieldKey == null) {
                    continue;
                }

                PlcDataLatestEntity data = queryPlcFieldExact(machineId, fieldKey, categoryName, mapping.getCategoryTemplate());
                if (data == null) {
                    log.debug("[YellowAlarm] 未找到PLC数据: internalKey={}, fieldKey={}, categoryName={}",
                            sourceInternalKey, fieldKey, categoryName);
                    continue;
                }

                for (AlarmStateRuleEntity rule : group.getValue()) {
                    RuleDetection detection = evaluateRule(rule, data, now);
                    if (!detection.evaluated()) {
                        continue; // 数据/配置异常，无法判定 → 保持现状
                    }
                    evaluatedRuleCodes.add(rule.getRuleCode());
                    if (detection.triggered() && (winner == null || rulePriority(rule) > rulePriority(winner))) {
                        winner = rule;
                        winnerElapsedSeconds = detection.elapsedSeconds();
                    }
                }
            }
        }

        // 互斥：除唯一胜出者外，所有本轮已评估的规则都要取消
        List<String> toCancel = new ArrayList<>();
        for (String ruleCode : evaluatedRuleCodes) {
            if (winner == null || !ruleCode.equals(winner.getRuleCode())) {
                toCancel.add(ruleCode);
            }
        }

        if (winner != null) {
            Integer warnAfterSeconds = winner.getWarnAfterSeconds() != null ? winner.getWarnAfterSeconds() : 0;
            long exceededSeconds = Math.max(0, winnerElapsedSeconds - warnAfterSeconds);
            if (unhandledRuleCodes.contains(winner.getRuleCode())) {
                baseMapper.updateCurrentValueByFieldCode(machineId, stationId,
                        winner.getRuleCode(), BigDecimal.valueOf(exceededSeconds));
                log.debug("更新黄色报警currentValue: machineId={}, stationId={}, ruleCode={}, exceeded={}s",
                        machineId, stationId, winner.getRuleCode(), exceededSeconds);
            } else {
                baseMapper.insertYellowAlarm(machineId, stationId, 0L, 0L,
                        winner.getRuleCode(), winner.getRuleName(), exceededSeconds);
                unhandledRuleCodes.add(winner.getRuleCode());
                log.info("创建黄色报警: machineId={}, stationId={}, ruleCode={}, elapsed={}s, exceeded={}s",
                        machineId, stationId, winner.getRuleCode(), winnerElapsedSeconds, exceededSeconds);
            }
        }

        cancelYellowAlarms(machineId, stationId, toCancel, unhandledRuleCodes);
    }

    /** 规则互斥优先级，null 视为 0（越大越优先） */
    private static int rulePriority(AlarmStateRuleEntity rule) {
        return rule.getPriority() != null ? rule.getPriority() : 0;
    }

    /** 单条规则的检测结果 */
    private record RuleDetection(boolean evaluated, boolean triggered, long elapsedSeconds) {
        /** 数据/配置异常，无法判定：调用方应保持现状，既不创建也不取消 */
        static RuleDetection skip() {
            return new RuleDetection(false, false, 0);
        }

        static RuleDetection of(boolean triggered, long elapsedSeconds) {
            return new RuleDetection(true, triggered, elapsedSeconds);
        }
    }

    /**
     * 评估单条 alarm_state_rule 对当前 PLC 数据是否触发。
     * 注意：OFF_DURATION 场景下"当前值 != 触发值"（如合模止已恢复 ON）属于<b>明确未触发</b>，
     * 必须返回 evaluated=true，否则已存在的报警永远无法被取消。
     */
    private RuleDetection evaluateRule(AlarmStateRuleEntity rule, PlcDataLatestEntity data, LocalDateTime now) {
        String triggerType = rule.getTriggerType();
        Integer warnAfterSeconds = rule.getWarnAfterSeconds();
        if (warnAfterSeconds == null) {
            warnAfterSeconds = 0;
        }

        if ("DATA_STALE".equals(triggerType)) {
            LocalDateTime updateTime = data.getDataTimestamp() != null
                    ? data.getDataTimestamp() : data.getCreateTime();
            if (updateTime == null) {
                return RuleDetection.skip();
            }
            long elapsedSeconds = Duration.between(updateTime, now).getSeconds();
            log.debug("[YellowAlarm] DATA_STALE检测: ruleCode={}, elapsed={}s, warnAfter={}s",
                    rule.getRuleCode(), elapsedSeconds, warnAfterSeconds);
            return RuleDetection.of(elapsedSeconds >= warnAfterSeconds, elapsedSeconds);
        }

        if ("OFF_DURATION".equals(triggerType)) {
            String currentValue = data.getFieldValue();
            String triggerValue = rule.getTriggerValue();
            if (!StrUtil.equalsIgnoreCase(currentValue, triggerValue)) {
                return RuleDetection.of(false, 0);
            }
            LocalDateTime changeTime = data.getValueChangedAt() != null
                    ? data.getValueChangedAt() : data.getDataTimestamp();
            if (changeTime == null) {
                return RuleDetection.skip();
            }
            long elapsedSeconds = Duration.between(changeTime, now).getSeconds();
            log.debug("[YellowAlarm] OFF_DURATION检测: ruleCode={}, value={}, elapsed={}s, warnAfter={}s",
                    rule.getRuleCode(), currentValue, elapsedSeconds, warnAfterSeconds);
            return RuleDetection.of(elapsedSeconds >= warnAfterSeconds, elapsedSeconds);
        }

        return RuleDetection.skip();
    }

    /** 取消仍未处理的黄色报警；只对"待取消 ∩ 现存"发 UPDATE，避免每轮空更新 */
    private void cancelYellowAlarms(Long machineId, Long stationId, List<String> toCancel, Set<String> unhandledRuleCodes) {
        if (toCancel.isEmpty() || unhandledRuleCodes.isEmpty()) {
            return;
        }
        List<String> targets = toCancel.stream()
                .filter(unhandledRuleCodes::contains)
                .distinct()
                .collect(Collectors.toList());
        if (targets.isEmpty()) {
            return;
        }
        baseMapper.handleYellowAlarmsByFieldCodes(machineId, stationId, targets);
        unhandledRuleCodes.removeAll(targets);
        log.info("取消黄色报警: machineId={}, stationId={}, ruleCodes={}", machineId, stationId, targets);
    }

    private String renderFieldKeyForAlarm(FieldMappingEntity mapping, String moldSide, Integer gunNo, Integer stage, Integer stationNo) {
        String pattern = mapping.getMatchPattern();
        if (pattern == null) {
            return null;
        }

        String internalKey = mapping.getInternalKey();
        boolean needsSide = pattern.contains("{side}");
        boolean needsGun = pattern.contains("{gun}");
        boolean needsStage = pattern.contains("{stage}");
        boolean needsIdx = pattern.contains("{idx}");

        // 生成英文 field_key（与 plc_data_latest.field_key 一致）
        // side：pattern 含 {side} 时按排期决定 L/R；pattern 以"左"/"右"开头时自动派生 L/R（与 buildFieldKey 一致）
        String sideStr = null;
        if (needsSide) {
            sideStr = "LEFT".equalsIgnoreCase(moldSide) ? "L" : "R";
        } else if (pattern.startsWith("左")) {
            sideStr = "L";
        } else if (pattern.startsWith("右")) {
            sideStr = "R";
        }

        Integer gunParam = needsGun ? gunNo : null;
        Integer stageParam = needsStage ? stage : null;
        Integer idxParam = needsIdx ? 1 : null;

        StringBuilder english = new StringBuilder(internalKey);
        if (sideStr != null) {
            english.append("_").append(sideStr);
        }
        if (gunParam != null) {
            english.append("_").append(gunParam);
        }
        if (stageParam != null && stageParam > 0) {
            english.append("_").append(stageParam);
        }
        if (idxParam != null && idxParam > 0) {
            english.append("_").append(idxParam);
        }
        return english.toString();
    }

    /**
     * 根据规则（抽象 internalKey + dimensionType + stage）结合排期维度（moldSide/gunNo/stationNo）
     * 实例化为具体 PLC field_key 列表。规则不再存储具体 L/R/枪号，由排期决定。
     */
    private List<ResolvedKey> resolveRuleToPlcKeys(ShootMoldRuleEntity rule, String side, int gunCount, Integer gunNo, int stationNo) {
        String internalKey = rule.getFieldCode();
        if (StrUtil.isBlank(internalKey)) {
            return List.of();
        }
        internalKey = internalKey.replaceAll("\\s+", "");

        Integer stage = "STAGE".equals(rule.getDimensionType()) ? rule.getStage() : null;
        List<FieldMappingEntity> mappings = fieldMappingByInternalKey.get(internalKey);
        if (mappings == null || mappings.isEmpty()) {
            return List.of();
        }
        return renderMappings(mappings, side, gunCount, gunNo, stationNo, stage);
    }

    private List<ResolvedKey> renderMappings(List<FieldMappingEntity> mappings, String side, int gunCount, Integer gunNo, int stationNo, Integer stage) {
        List<ResolvedKey> result = new ArrayList<>();
        String sideStr = "LEFT".equalsIgnoreCase(side) ? "左" : "右";

        for (FieldMappingEntity mapping : mappings) {
            String categoryName = FieldMatchingEngine.buildCategoryName(mapping.getCategoryTemplate(), stationNo, gunCount);
            String internalKey = mapping.getInternalKey();
            if (internalKey == null) {
                continue;
            }

            String pattern = mapping.getMatchPattern();
            boolean needsSide = pattern != null && pattern.contains("{side}");
            boolean needsGun = pattern != null && pattern.contains("{gun}");
            boolean needsStage = pattern != null && pattern.contains("{stage}");
            boolean needsIdx = pattern != null && pattern.contains("{idx}");

            // pattern 不含 {side} 占位符但以"左/右"开头时（如 "右模第{stage}阶段 射出压力"），
            // 必须与排期 side 匹配才处理；否则排期是 LEFT 时也会同时检测"右模..."的 mapping，
            // 导致右模也被创建告警（即使右模根本没排期）。
            if (!needsSide && pattern != null) {
                boolean patternIsLeft = pattern.startsWith("左");
                boolean patternIsRight = pattern.startsWith("右");
                if ((patternIsLeft && !"左".equals(sideStr))
                        || (patternIsRight && !"右".equals(sideStr))) {
                    continue;
                }
            }

            // 与 MatchResult 的派生逻辑保持一致：
            // 若 pattern 不含 {side} 占位符但以"左"/"右"开头（如 "右模第{stage}阶段 射出压力"），
            // 也要派生 side 维度，否则生成的 field_key 缺 _L_/_R_ 后缀，
            // 与 plc_data_latest.field_key（落库时已带 _L_/_R_）对不上，导致告警被静默跳过。
            String effectiveSide;
            if (needsSide) {
                effectiveSide = sideStr;
            } else if (pattern != null && pattern.startsWith("左")) {
                effectiveSide = "左";
            } else if (pattern != null && pattern.startsWith("右")) {
                effectiveSide = "右";
            } else {
                effectiveSide = null;
            }

            if (needsIdx) {
                Integer idxCountVal = mapping.getStageCount();
                if (idxCountVal == null || idxCountVal <= 0) {
                    continue;
                }
                int idxMax = idxCountVal;
                for (int idx = 1; idx <= idxMax; idx++) {
                    String englishKey = buildEnglishFieldKey(internalKey, effectiveSide, needsGun ? gunNo : null, needsStage ? stage : null, idx);
                    if (englishKey != null) {
                        String displayName = FieldMatchingEngine.renderPattern(mapping, stationNo, needsGun ? gunNo : null, needsStage ? stage : null, idx, effectiveSide);
                        result.add(new ResolvedKey(englishKey, categoryName, displayName, mapping.getCategoryTemplate(), effectiveSide == null));
                    }
                }
            } else if (needsStage && stage == null) {
                // 用 per-field stageCount（field_mapping.stage_count），无配置则跳过该字段
                Integer stageCountVal = mapping.getStageCount();
                if (stageCountVal == null || stageCountVal <= 0) {
                    continue;
                }
                int stageMax = stageCountVal;
                for (int s = 1; s <= stageMax; s++) {
                    String englishKey = buildEnglishFieldKey(internalKey, effectiveSide, needsGun ? gunNo : null, s, null);
                    if (englishKey != null) {
                        String displayName = FieldMatchingEngine.renderPattern(mapping, stationNo, needsGun ? gunNo : null, s, null, effectiveSide);
                        result.add(new ResolvedKey(englishKey, categoryName, displayName, mapping.getCategoryTemplate(), effectiveSide == null));
                    }
                }
            } else {
                String englishKey = buildEnglishFieldKey(internalKey, effectiveSide, needsGun ? gunNo : null, needsStage ? stage : null, null);
                if (englishKey != null) {
                    String displayName = FieldMatchingEngine.renderPattern(mapping, stationNo, needsGun ? gunNo : null, needsStage ? stage : null, null, effectiveSide);
                    result.add(new ResolvedKey(englishKey, categoryName, displayName, mapping.getCategoryTemplate(), effectiveSide == null));
                }
            }
        }
        return result;
    }

    /** 根据 internal_key + 维度构建英文 field_key（与 FieldMatchingEngine.buildFieldKey 一致） */
    private String buildEnglishFieldKey(String internalKey, String side, Integer gunNo, Integer stage, Integer idx) {
        List<String> suffixParts = new ArrayList<>();
        if (side != null) {
            suffixParts.add("左".equals(side) ? "L" : "R");
        }
        if (gunNo != null) {
            suffixParts.add(String.valueOf(gunNo));
        }
        if (stage != null && stage > 0) {
            suffixParts.add(String.valueOf(stage));
        }
        if (idx != null && idx > 0) {
            suffixParts.add(String.valueOf(idx));
        }
        if (suffixParts.isEmpty()) {
            return internalKey;
        }
        return internalKey + "_" + String.join("_", suffixParts);
    }

    private PlcDataLatestEntity queryPlcFieldExact(Long machineId, String fieldKey, String categoryName, String categoryTemplate) {
        if (fieldKey != null) {
            fieldKey = fieldKey.trim();
        }
        // STATION 类字段（categoryTemplate != GUN_TEMP_CAT）：同一 field_key 下不同站台会落多条数据（category_name=站台1~站台N）。
        // 必须按 category_name 过滤，才能取到排期所在站台的最新值，否则会取到其他站台的值导致报警错乱/缺少。
        // GUN_TEMP 不参与站台维度（category_name 为"N射枪温度"，按射枪数区分），保持只按 machine_id + field_key 取最新。
        if (!"GUN_TEMP_CAT".equals(categoryTemplate)) {
            return plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyAndCategory(machineId, fieldKey, categoryName);
        }
        return plcDataLatestMapper.selectLatestByMachineIdAndFieldKey(machineId, fieldKey);
    }

    private void detectRedAlarmsFromPlcDataSingle(Long machineId, ShootStationScheduleEntity schedule, int gunCount, Set<String> insertedKeys, Map<Long, List<ShootMoldRuleEntity>> rulesByMold) {
        Long stationId = schedule.getStationId();
        Integer stationNo = schedule.getStationNo();
        Long moldId = schedule.getMoldId();
        String moldSide = schedule.getMoldSide();
        Integer gunNo = schedule.getGunNo();
        if (stationId == null || stationNo == null || moldId == null) {
            return;
        }
        log.debug("检测红色报警: schedule stationId={}, stationNo={}, moldId={}, moldSide={}, gunNo={}, gunCount={}",
                stationId, stationNo, moldId, moldSide, gunNo, gunCount);

        List<ShootMoldRuleEntity> rules = rulesByMold.getOrDefault(moldId, Collections.emptyList());
        for (ShootMoldRuleEntity rule : rules) {
            if (Boolean.FALSE.equals(rule.getEnabled())) {
                continue;
            }

            List<ResolvedKey> resolvedKeys = resolveRuleToPlcKeys(rule, moldSide, gunCount, gunNo, stationNo);
            log.debug("[RedAlarm] 规则解析: ruleId={}, fieldCode={}, moldSide={}, resolvedKeys={}",
                    rule.getId(), rule.getFieldCode(), moldSide, resolvedKeys);

            for (ResolvedKey rk : resolvedKeys) {
                // 无 side 维度的 GLOBAL 字段（如 SET_CURE_TIME）一个站位只有一条 PLC 数据，
                // 但会被左右两个模具的排期分别遍历。若按单排期处理，会出现：
                //   1) 一侧建报警、另一侧认为「正常」立即按 field_code 取消掉（互相打架）
                //   2) 排期变更后报警 mold_id 卡在旧值被「无当前排期」孤儿取消
                // 故此处跳过，改由 detectSideLessGlobalRedAlarms 聚合同站位所有排期一起判断：
                // 任一模超限即报警，所有模都正常才取消。
                if (rk.isSideLess() && "GLOBAL".equals(rule.getDimensionType())) {
                    continue;
                }
                PlcDataLatestEntity fieldData = queryPlcFieldExact(machineId, rk.fieldKey(), rk.categoryName(), rk.categoryTemplate());
                if (fieldData == null) {
                    log.debug("[RedAlarm] 未匹配PLC数据，跳过: machineId={}, stationNo={}, ruleId={}, fieldCode={}, fieldKey={}, categoryName={}",
                            machineId, stationNo, rule.getId(), rule.getFieldCode(), rk.fieldKey(), rk.categoryName());
                    continue;
                }

                BigDecimal currentValue = Convert.toBigDecimal(fieldData.getFieldValue(), null);
                if (currentValue == null) {
                    log.debug("PLC字段值为空: machineId={}, fieldKey={}, fieldValue={}", machineId, rk.fieldKey(), fieldData.getFieldValue());
                    continue;
                }

                log.debug("PLC字段值: machineId={}, fieldKey={}, currentValue={}, minValue={}, maxValue={}",
                        machineId, rk.fieldKey(), currentValue, rule.getMinValue(), rule.getMaxValue());

                boolean isOutOfRange = currentValue.compareTo(rule.getMinValue()) < 0
                        || currentValue.compareTo(rule.getMaxValue()) > 0;

                if (isOutOfRange) {
                    String dedupKey = stationId + "_" + rule.getId() + "_" + rk.fieldKey() + "_" + currentValue;
                    if (insertedKeys.add(dedupKey)) {
                        createAlarmFromDetection(machineId, stationId, rule, currentValue, rk.fieldKey(), rk.displayName());
                    }
                } else {
                    autoCancelRedAlarmWhenNormal(machineId, stationId, rule, rk.fieldKey());
                }
            }
        }
    }

    /**
     * 检测无 side 维度的 GLOBAL 字段（如 SET_CURE_TIME）。
     * <p>
     * 这类字段的 match_pattern 不含 {side} 占位符（如 "设定加硫时间"），一个站位只有一条 PLC 数据，
     * 但排期可能给站位排了左右两个模具，各自有独立的 SET_CURE_TIME 阈值规则。
     * 若按单条排期逐个判断（detectRedAlarmsFromPlcDataSingle），会出现：
     * <ul>
     *   <li>左模规则超限建报警 → 右模规则认为正常 → autoCancelRedAlarmWhenNormal 按 field_code 把左右模报警一起取消</li>
     *   <li>报警 mold_id 卡在旧排期残留值 → autoCancelAlarmsWithoutCurrentSchedule 孤儿取消误杀</li>
     * </ul>
     * 正确语义：每个模具独立判断、独立建/取消报警（field_code 保持 "SET_CURE_TIME" 不变，
     * 由 mold_id 区分两条报警）。前端 board.vue 按 moldModel 分行展示，
     * 期望同站位 SET_CURE_TIME 有多条报警（每个模具一条）。
     * </p>
     */
    private void detectSideLessGlobalRedAlarms(Long machineId,
                                               Map<Long, List<ShootStationScheduleEntity>> schedulesByStation,
                                               int gunCount,
                                               Map<Long, List<ShootMoldRuleEntity>> rulesByMold,
                                               Set<String> insertedKeys) {
        for (Map.Entry<Long, List<ShootStationScheduleEntity>> stationEntry : schedulesByStation.entrySet()) {
            Long stationId = stationEntry.getKey();
            List<ShootStationScheduleEntity> stationSchedules = stationEntry.getValue();
            if (stationSchedules.isEmpty()) {
                continue;
            }

            Integer stationNo = stationSchedules.get(0).getStationNo();
            if (stationNo == null) {
                continue;
            }

            // 收集该站位所有排期下、所有 side-less GLOBAL 规则，按 field_key 聚合
            // field_key -> 关联规则列表（来自不同模具），PLC 值只查一次
            Map<String, List<ShootMoldRuleEntity>> sideLessRulesByFieldKey = new LinkedHashMap<>();
            Map<String, ResolvedKey> resolvedKeyInfo = new HashMap<>();

            for (ShootStationScheduleEntity schedule : stationSchedules) {
                Long moldId = schedule.getMoldId();
                String moldSide = schedule.getMoldSide();
                Integer gunNo = schedule.getGunNo();
                if (moldId == null) {
                    continue;
                }
                List<ShootMoldRuleEntity> rules = rulesByMold.getOrDefault(moldId, Collections.emptyList());
                for (ShootMoldRuleEntity rule : rules) {
                    if (Boolean.FALSE.equals(rule.getEnabled())) {
                        continue;
                    }
                    if (!"GLOBAL".equals(rule.getDimensionType())) {
                        continue;
                    }
                    List<ResolvedKey> resolvedKeys = resolveRuleToPlcKeys(rule, moldSide, gunCount, gunNo, stationNo);
                    for (ResolvedKey rk : resolvedKeys) {
                        if (!rk.isSideLess()) {
                            continue;
                        }
                        sideLessRulesByFieldKey.computeIfAbsent(rk.fieldKey(), k -> new ArrayList<>()).add(rule);
                        resolvedKeyInfo.putIfAbsent(rk.fieldKey(), rk);
                    }
                }
            }

            for (Map.Entry<String, List<ShootMoldRuleEntity>> fieldEntry : sideLessRulesByFieldKey.entrySet()) {
                String fieldKey = fieldEntry.getKey();
                List<ShootMoldRuleEntity> fieldRules = fieldEntry.getValue();
                ResolvedKey rk = resolvedKeyInfo.get(fieldKey);

                // 同一站位同一点位的 PLC 值只查一次
                PlcDataLatestEntity fieldData = queryPlcFieldExact(machineId, rk.fieldKey(), rk.categoryName(), rk.categoryTemplate());
                if (fieldData == null) {
                    log.debug("[SideLessRedAlarm] 未匹配PLC数据，跳过: machineId={}, stationNo={}, fieldKey={}, categoryName={}",
                            machineId, stationNo, rk.fieldKey(), rk.categoryName());
                    continue;
                }
                BigDecimal currentValue = Convert.toBigDecimal(fieldData.getFieldValue(), null);
                if (currentValue == null) {
                    log.debug("[SideLessRedAlarm] PLC字段值为空: machineId={}, fieldKey={}, fieldValue={}",
                            machineId, rk.fieldKey(), fieldData.getFieldValue());
                    continue;
                }

                // 每个模具独立判断、独立建/取消报警（field_code 不变，mold_id 区分）
                for (ShootMoldRuleEntity rule : fieldRules) {
                    boolean isOutOfRange = currentValue.compareTo(rule.getMinValue()) < 0
                            || currentValue.compareTo(rule.getMaxValue()) > 0;
                    if (isOutOfRange) {
                        String dedupKey = stationId + "_" + rule.getMoldId() + "_" + rule.getId() + "_" + fieldKey + "_" + currentValue;
                        if (insertedKeys.add(dedupKey)) {
                            log.debug("[SideLessRedAlarm] 模具规则超限，建/续报警: machineId={}, stationNo={}, fieldKey={}, currentValue={}, moldId={}, ruleId={}, min={}, max={}",
                                    machineId, stationNo, fieldKey, currentValue, rule.getMoldId(), rule.getId(), rule.getMinValue(), rule.getMaxValue());
                            createSideLessAlarmPerMold(machineId, stationId, rule, currentValue, fieldKey, rk.displayName());
                        }
                    } else {
                        // 该模具规则认为正常 → 只取消该模具的报警，不影响其他模具
                        int n = baseMapper.autoCancelRedAlarmsByFieldCodeAndMold(machineId, stationId, fieldKey, rule.getMoldId());
                        if (n > 0) {
                            log.debug("[SideLessRedAlarm] 该模规则正常，取消本模报警: machineId={}, stationNo={}, fieldKey={}, moldId={}, currentValue={}",
                                    machineId, stationNo, fieldKey, rule.getMoldId(), currentValue);
                        }
                    }
                }
            }
        }
    }

    /**
     * 为 side-less GLOBAL 字段按 (station, field_code, mold_id) 维度建/续报警。
     * 与 createAlarmFromDetection 的区别：去重维度多了 mold_id，避免左右模共用同一 field_code
     * 时互相覆盖（前端期望每个模具一条报警，按 moldModel 分行展示）。
     */
    private void createSideLessAlarmPerMold(Long machineId, Long stationId, ShootMoldRuleEntity rule,
                                            BigDecimal currentValue, String plcFieldKey, String displayName) {
        try {
            Long moldId = rule.getMoldId();
            long existingCount = baseMapper.existsUnhandledRedAlarmByFieldCodeAndMold(machineId, stationId, plcFieldKey, moldId);
            if (existingCount > 0) {
                baseMapper.updateRedAlarmByFieldCodeAndMold(machineId, stationId, plcFieldKey, moldId,
                        currentValue, rule.getMinValue(), rule.getMaxValue(), rule.getId());
                return;
            }

            Long latestId = baseMapper.findLatestRedAlarmIdByFieldCodeAndMold(machineId, stationId, plcFieldKey, moldId);
            if (latestId != null) {
                baseMapper.reactivateRedAlarm(latestId, currentValue, rule.getMinValue(), rule.getMaxValue(), moldId, rule.getId());
                return;
            }

            String alarmFieldName = StrUtil.firstNonBlank(displayName, rule.getFieldName(), plcFieldKey);
            baseMapper.insertRedAlarm(machineId, stationId, moldId, rule.getId(),
                    plcFieldKey, alarmFieldName, rule.getMinValue(), rule.getMaxValue(), currentValue);
            log.info("[SideLessRedAlarm] 创建红色报警: machineId={}, stationId={}, moldId={}, fieldCode={}, currentValue={}",
                    machineId, stationId, moldId, plcFieldKey, currentValue);
        } catch (Exception e) {
            log.error("[SideLessRedAlarm] Create alarm failed: machineId={}, stationId={}, moldId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getMoldId(), rule.getId(), rule.getFieldCode(), e);
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

    private void createAlarmFromDetection(Long machineId, Long stationId, ShootMoldRuleEntity rule,
                                          BigDecimal currentValue, String plcFieldKey, String displayName) {
        try {
            String alarmFieldCode = StrUtil.isNotBlank(plcFieldKey) ? plcFieldKey : rule.getFieldCode();

            // 按物理点位(field_code)去重，不绑定 rule_id：
            // 规则被删除重建后 rule_id 会变化，若按 rule_id 去重，修改阈值会匹配不到旧报警而每次都新增全新报警。
            long existingCount = baseMapper.existsUnhandledRedAlarmByFieldCode(machineId, stationId, alarmFieldCode);
            if (existingCount > 0) {
                baseMapper.updateRedAlarmCurrentValueByFieldCode(machineId, stationId, alarmFieldCode, currentValue,
                        rule.getMinValue(), rule.getMaxValue(), rule.getMoldId(), rule.getId());
                log.debug("已存在红色报警，仅更新最新值: machineId={}, stationId={}, ruleId={}, fieldCode={}, currentValue={}, min={}, max={}",
                        machineId, stationId, rule.getId(), alarmFieldCode, currentValue, rule.getMinValue(), rule.getMaxValue());
                return;
            }

            Long latestId = baseMapper.findLatestRedAlarmIdByFieldCode(machineId, stationId, alarmFieldCode);
            if (latestId != null) {
                baseMapper.reactivateRedAlarm(latestId, currentValue, rule.getMinValue(), rule.getMaxValue(),
                        rule.getMoldId(), rule.getId());
                log.debug("复用已存在的红色报警（翻回未处理并更新值）: machineId={}, stationId={}, ruleId={}, fieldCode={}, alarmId={}, currentValue={}, min={}, max={}",
                        machineId, stationId, rule.getId(), alarmFieldCode, latestId, currentValue, rule.getMinValue(), rule.getMaxValue());
                return;
            }

            // field_name 优先用中文 displayName（如"右模第1阶段 射出压力"），其次用规则自带 fieldName，
            // 最后才退回 plcFieldKey（英文）。确保告警表 field_name 列可读性强。
            String alarmFieldName = StrUtil.firstNonBlank(displayName, rule.getFieldName(), plcFieldKey);
            baseMapper.insertRedAlarm(machineId, stationId, rule.getMoldId(), rule.getId(),
                    alarmFieldCode, alarmFieldName, rule.getMinValue(), rule.getMaxValue(), currentValue);
            log.info("创建红色报警: machineId={}, stationId={}, ruleId={}, fieldCode={}, currentValue={}",
                    machineId, stationId, rule.getId(), alarmFieldCode, currentValue);
        } catch (Exception e) {
            log.error("Create alarm failed: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getId(), rule.getFieldCode(), e);
        }
    }

    private void autoCancelRedAlarmWhenNormal(Long machineId, Long stationId, ShootMoldRuleEntity rule, String plcFieldKey) {
        try {
            // 按物理点位(field_code)取消，不绑定 rule_id：
            // 规则被删除重建后 rule_id 会变化，旧报警仍引用旧 rule_id，按 rule_id 取消永远匹配不到，
            // 导致「阈值修改后报警无法自动消除」。field_code 是实例化后的点位 key，当前规则判断值恢复正常，
            // 即可清理该点位所有未处理红色报警（含旧 rule_id 的孤儿报警）。
            int cancelledCount = baseMapper.autoCancelRedAlarmsByFieldCode(machineId, stationId, plcFieldKey);
            if (cancelledCount > 0) {
                log.info("参数恢复正常，自动取消{}条红色报警: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                        cancelledCount, machineId, stationId, rule.getId(), plcFieldKey);
            }
        } catch (Exception e) {
            log.error("自动取消红色报警失败: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getId(), rule.getFieldCode(), e);
        }
    }

    private record ResolvedKey(String fieldKey, String categoryName, String displayName, String categoryTemplate, boolean isSideLess) {
    }
}