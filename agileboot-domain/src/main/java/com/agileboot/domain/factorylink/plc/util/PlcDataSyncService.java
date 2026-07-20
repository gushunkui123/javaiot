package com.agileboot.domain.factorylink.plc.util;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlcDataSyncService {

    private static final List<String> DEFAULT_DATA_CODES = List.of("kkb756", "7JTAVe","2WSK6z","QLjnd7");

    private final PlcDataLatestMapper plcDataLatestMapper;
    private final ShootRuleAlarmService shootRuleAlarmService;
    private final ShootMachineMapper shootMachineMapper;
    private final RestTemplate restTemplate;

    @Value("${factory-link.workshop.base-url:http://10.0.100.225:8088}")
    private String externalPlcBaseUrl;

    @Value("${factory-link.workshop.api-key:}")
    private String workshopApiKey;

    @Value("${factory-link.workshop.api-secret:}")
    private String workshopApiSecret;

    /** 高频字段：开模止/合模止，3秒同步一次 */
    private static final Set<String> HIGH_FREQ_FIELDS = Set.of("开模止", "合模止");

    /**
     * 同步外部 PLC 数据点到本地（固定 dataCodes），无过滤
     * @return 同步的记录数
     */
    public int syncPlcDataPoints() {
        return doSync(DEFAULT_DATA_CODES, null);
    }

    /**
     * 仅同步指定 fieldKey 的数据（高频字段：开模止/合模止）
     * @return 同步的记录数
     */
    public int syncHighFrequencyFields() {
        return doSync(DEFAULT_DATA_CODES, fieldKey -> HIGH_FREQ_FIELDS.contains(fieldKey));
    }

    /**
     * 同步除指定 fieldKey 外的所有数据（低频字段）
     * @return 同步的记录数
     */
    public int syncLowFrequencyFields() {
        return doSync(DEFAULT_DATA_CODES, fieldKey -> !HIGH_FREQ_FIELDS.contains(fieldKey));
    }

    private int doSync(List<String> dataCodes, Predicate<String> fieldKeyFilter) {
        SignedRestTemplateUtil signedUtil = new SignedRestTemplateUtil(restTemplate, workshopApiKey, workshopApiSecret);

        // 第三方单值验签：每个 dataCode 单独请求；无 dataCode 时发一次全量请求(null)
        List<String> targets = (dataCodes == null || dataCodes.isEmpty())
                ? Collections.singletonList(null) : dataCodes;

        List<Object> allRows = new ArrayList<>();
        for (String code : targets) {
            List<?> rows = callOnce(signedUtil, code);
            if (rows != null) {
                allRows.addAll(rows);
            }
        }

        if (allRows.isEmpty()) {
            return 0;
        }

        log.info("外部PLC返回 {} 条数据", allRows.size());

        // 临时调试：打印返回数据的完整内容，确认第三方返回结构（最多打印前 3 条）
        Object first = allRows.get(0);
        if (first instanceof Map<?, ?> firstMap) {
            log.info("第三方返回字段名: {}", firstMap.keySet());
        }
        int previewCount = Math.min(allRows.size(), 3);
        for (int i = 0; i < previewCount; i++) {
            Object row = allRows.get(i);
            if (row instanceof Map<?, ?> rowMap) {
                log.info("第三方返回数据[{}]: {}", i, rowMap);
            }
        }

        // 合并所有请求结果，统一落库、统一触发一次报警检测
        List<PlcDataLatestEntity> entities = convertToEntities(allRows);
        if (entities.isEmpty()) {
            return 0;
        }

        // 按 fieldKey 过滤：高频任务只写开模止/合模止，低频任务写其余
        if (fieldKeyFilter != null) {
            entities = entities.stream()
                    .filter(e -> e.getFieldKey() != null && fieldKeyFilter.test(e.getFieldKey()))
                    .collect(Collectors.toList());
            if (entities.isEmpty()) {
                return 0;
            }
        }

        plcDataLatestMapper.batchUpsert(entities);
        log.info("同步完成，共写入 {} 条记录", entities.size());

        // 按 distinct machineId 触发报警检测（machineId 由第三方 areaName 反查得到）
        Set<Long> machineIds = entities.stream()
                .map(PlcDataLatestEntity::getMachineId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (Long machineId : machineIds) {
            try {
                shootRuleAlarmService.detectAlarmsByPlcData(machineId);
            } catch (Exception e) {
                log.error("PLC数据同步后报警检测失败, machineId={}", machineId, e);
            }
        }
        log.info("PLC数据同步后报警检测完成, machineIds={}", machineIds);

        return entities.size();
    }

    /**
     * 单次调用外部 PLC 接口，只带一个 dataCodes（或 null 表示全量）
     */
    private List<?> callOnce(SignedRestTemplateUtil signedUtil, String dataCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (dataCode != null) {
            // 第三方参数名为 dataCode（camelCase 单数）
            params.add("dataCode", dataCode);
        }
        try {
            ResponseEntity<Map<String, Object>> response = signedUtil.get(
//                    externalPlcBaseUrl, "/api/device/listByFactoryAndDevice",
                    externalPlcBaseUrl, "/prod-api/api/device/listByFactoryAndDevice",
                    params, new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> result = response.getBody();
            if (result == null) {
                log.warn("调用外部PLC接口返回为空, dataCode={}", dataCode);
                return null;
            }
            Object dataObj = result.get("data");
            if (!(dataObj instanceof List<?> rows)) {
                log.warn("数据格式异常, dataCode={}, 返回内容={}, data字段类型={}", dataCode, result, dataObj != null ? dataObj.getClass().getName() : "null");
                return null;
            }
            return rows;
        } catch (Exception e) {
            log.error("调用外部PLC接口失败, dataCode={}", dataCode, e);
            return null;
        }
    }

    private List<PlcDataLatestEntity> convertToEntities(List<?> rows) {
        List<PlcDataLatestEntity> entities = new ArrayList<>();
        int skipped = 0;
        for (Object item : rows) {
            if (!(item instanceof Map<?, ?> map)) {
                skipped++;
                continue;
            }
            PlcDataLatestEntity entity = toEntity(map);
            if (entity == null) {
                skipped++;
                continue;
            }
            entities.add(entity);
        }
        log.info("PLC数据转换: 总={}, 有效={}, 跳过={}", rows.size(), entities.size(), skipped);
        return entities;
    }

    /**
     * 根据第三方 areaName 反查 shoot_machine 表主键
     * @return 匹配的机台 id；未匹配到返回 null
     */
    private Long resolveMachineId(String areaName) {
        if (StrUtil.isBlank(areaName)) {
            return null;
        }
        ShootMachineEntity machine = shootMachineMapper.selectOne(
                new LambdaQueryWrapper<ShootMachineEntity>().eq(ShootMachineEntity::getMachineName, areaName));
        return machine != null ? machine.getId() : null;
    }

    /**
     * 单条外部数据转实体；dataCode 为空或 field_key 来源（displayName，缺失时回退 remark）为空返回 null（避免空数据/唯一键冲突）
     */
    private PlcDataLatestEntity toEntity(Map<?, ?> map) {
        String remark = getStr(map, "remark");
        String dataCode = getStr(map, "dataCode");
        if (StrUtil.isBlank(dataCode)) {
            return null;
        }

        // field_key 优先使用第三方返回的 displayName；displayName 缺失时回退 remark，避免数据被静默丢弃
        String displayName = getStr(map, "displayName");
        String fieldKeySource = StrUtil.isNotBlank(displayName) ? displayName : remark;
        if (StrUtil.isBlank(fieldKeySource)) {
            return null;
        }

        String currentValue = getStr(map, "currentValue");
        String processedValue = getStr(map, "processedValue");
        String fieldValue = StrUtil.isNotBlank(processedValue) ? processedValue : currentValue;
        String areaName = getStr(map, "areaName");
        String categoryName = getStr(map, "categoryName");
        if (StrUtil.isBlank(categoryName)) {
            categoryName = "默认";
        }

        // 根据第三方 areaName 反查 shoot_machine.machine_name 得到机台主键
        Long machineId = resolveMachineId(areaName);
        if (machineId == null) {
            log.warn("未匹配到机台，跳过该条数据: areaName={}, dataCode={}", areaName, dataCode);
            return null;
        }

        // 从第三方数据中提取时间字段（尝试常见字段名），解析失败回退当前时间
        LocalDateTime dataTime = parseThirdPartyTime(map);

        PlcDataLatestEntity entity = new PlcDataLatestEntity();
        // 当categoryName为"4射枪温度"时，device_name不填写；其他情况优先使用第三方返回的areaName
        if ("4射枪温度".equals(categoryName)) {
            entity.setDeviceName("");
        } else {
            entity.setDeviceName(StrUtil.isNotBlank(areaName) ? StrUtil.subPre(areaName, 100) : "");
        }
        entity.setMachineId(machineId);
        entity.setDataTimestamp(dataTime);
        entity.setFieldKey(StrUtil.subPre(fieldKeySource, 100).trim());
        entity.setDataCode(dataCode);
        entity.setFieldValue(StrUtil.subPre(fieldValue, 500));
        entity.setCategoryName(StrUtil.subPre(categoryName, 100));
        entity.setCreateTime(dataTime);
        // 首次插入时 value_changed_at = 当前同步时间；upsert 时 SQL 仅在 field_value 变化时更新
        entity.setValueChangedAt(LocalDateTime.now());
        return entity;
    }

    /**
     * 从第三方PLC返回的 map 中提取时间字段，支持 updateTime/timestamp/time 等常见字段名
     * 解析失败回退 LocalDateTime.now()
     */
    private LocalDateTime parseThirdPartyTime(Map<?, ?> map) {
        // 1. 优先使用第三方的 dataUpdatedAt（数据更新时间）
        String dataUpdatedAt = getStr(map, "dataUpdatedAt");
        if (StrUtil.isNotBlank(dataUpdatedAt)) {
            LocalDateTime time = tryParseTime(dataUpdatedAt);
            if (time != null) {
                log.info("使用第三方 dataUpdatedAt: {} -> {}", dataUpdatedAt, time);
                return time;
            }
        }

        // 2. 其次尝试 updateTime
        String updateTime = getStr(map, "updateTime");
        if (StrUtil.isNotBlank(updateTime)) {
            LocalDateTime time = tryParseTime(updateTime);
            if (time != null) {
                log.info("使用第三方 updateTime: {} -> {}", updateTime, time);
                return time;
            }
        }

        // 3. 最后回退到 createTime
        String createTime = getStr(map, "createTime");
        if (StrUtil.isNotBlank(createTime)) {
            LocalDateTime time = tryParseTime(createTime);
            if (time != null) {
                log.info("使用第三方 createTime: {} -> {}", createTime, time);
                return time;
            }
        }

        log.warn("未找到有效时间字段，使用当前时间。数据字段: {}", map.keySet());
        return LocalDateTime.now();
    }

    private LocalDateTime tryParseTime(String val) {
        if (StrUtil.isBlank(val)) {
            return null;
        }
        try {
            // yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd'T'HH:mm:ss
            return LocalDateTime.parse(val.replace(" ", "T").substring(0, 19));
        } catch (Exception e) {
            // ignore
        }
        try {
            // 毫秒时间戳
            long ts = Long.parseLong(val);
            if (ts > 1_000_000_000_000L) {
                return java.time.Instant.ofEpochMilli(ts).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            } else {
                return java.time.Instant.ofEpochSecond(ts).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private String getStr(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
