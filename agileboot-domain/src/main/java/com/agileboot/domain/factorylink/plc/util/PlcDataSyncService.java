package com.agileboot.domain.factorylink.plc.util;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.FieldMappingEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcMachineDataConfigEntity;
import com.agileboot.domain.factorylink.plc.mapper.FieldMappingMapper;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
import com.agileboot.domain.factorylink.plc.mapper.PlcMachineDataConfigMapper;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlcDataSyncService {

    private final PlcDataLatestMapper plcDataLatestMapper;
    private final FieldMappingMapper fieldMappingMapper;
    private final ShootRuleAlarmService shootRuleAlarmService;
    private final ShootMachineMapper shootMachineMapper;
    private final PlcMachineDataConfigMapper plcMachineDataConfigMapper;
    private final RestTemplate restTemplate;

    @Value("${factory-link.workshop.base-url:http://10.0.100.225:33010}")
    private String externalPlcBaseUrl;

    @Value("${factory-link.workshop.api-key:}")
    private String workshopApiKey;

    @Value("${factory-link.workshop.api-secret:}")
    private String workshopApiSecret;

    /** 高频 internal_keys：开模止/合模止，3秒同步一次 */
    private static final Set<String> HIGH_FREQ_INTERNAL_KEYS = Set.of("MOLD_CLOSE", "MOLD_OPEN");

    /** field_mapping 预编译结果，启动时加载 */
    private volatile List<CompiledMapping> compiledMappings = List.of();

    /** 机台 dataCode/configCode 映射缓存，启动时加载（替代原 ShootMachineCode 枚举硬编码） */
    private volatile List<PlcMachineDataConfigEntity> machineDataConfigCache = List.of();

    @PostConstruct
    public void initMappingCache() {
        try {
            List<FieldMappingEntity> list = fieldMappingMapper.listEnabled();
            compiledMappings = CompiledMapping.compile(list);
            log.info("[PlcDataSync] field_mapping 预编译完成，共 {} 条", compiledMappings.size());
        } catch (Exception e) {
            log.error("[PlcDataSync] field_mapping 预编译失败，fallback 空列表（落库时 category_name 可能不准）", e);
            compiledMappings = List.of();
        }
        try {
            machineDataConfigCache = plcMachineDataConfigMapper.listEnabled();
            log.info("[PlcDataSync] plc_machine_data_config 加载完成，共 {} 条", machineDataConfigCache.size());
        } catch (Exception e) {
            log.error("[PlcDataSync] plc_machine_data_config 加载失败，fallback 空列表（PLC 数据将停止同步）", e);
            machineDataConfigCache = List.of();
        }
    }

    /** convertToEntities 的返回包装：entity + 匹配结果（便于高频/低频过滤） */
    record ConvertResult(PlcDataLatestEntity entity, MatchResult match) {}

    /**
     * 同步外部 PLC 数据点到本地（固定 dataCodes），无过滤
     * @return 同步的记录数
     */
    public int syncPlcDataPoints() {
        return doSync(null);
    }

    /**
     * 仅同步指定 internal_key 的数据（高频字段：开模止/合模止）
     * @return 同步的记录数
     */
    public int syncHighFrequencyFields() {
        return doSync(result -> result.match() != null
                && HIGH_FREQ_INTERNAL_KEYS.contains(result.match().getMapping().getInternalKey()));
    }

    /**
     * 同步除指定 internal_key 外的所有数据（低频字段）
     * @return 同步的记录数
     */
    public int syncLowFrequencyFields() {
        return doSync(result -> result.match() == null
                || !HIGH_FREQ_INTERNAL_KEYS.contains(result.match().getMapping().getInternalKey()));
    }

    /**
     * 遍历 plc_machine_data_config 中启用的 (machineName, dataCode, configCode) 映射，
     * 每个机台请求其下所有 dataCode 的数据，请求结果均归属该机台（无需按 dataCode 反查）。
     * @param resultFilter 基于 ConvertResult 的过滤（null 表示不过滤）
     */
    private int doSync(Predicate<ConvertResult> resultFilter) {
        SignedRestTemplateUtil signedUtil = new SignedRestTemplateUtil(restTemplate, workshopApiKey, workshopApiSecret);

        // 按机台分组配置行（保留插入顺序）
        Map<String, List<PlcMachineDataConfigEntity>> configsByMachine = machineDataConfigCache.stream()
                .collect(Collectors.groupingBy(PlcMachineDataConfigEntity::getMachineName,
                        LinkedHashMap::new, Collectors.toList()));

        // 预查所有机台实体，避免 toEntity 每条数据查一次 DB
        Map<String, ShootMachineEntity> machineByName = new HashMap<>();
        for (String machineName : configsByMachine.keySet()) {
            ShootMachineEntity m = shootMachineMapper.selectOne(
                    new LambdaQueryWrapper<ShootMachineEntity>().eq(ShootMachineEntity::getMachineName, machineName));
            if (m != null) {
                machineByName.put(machineName, m);
            }
        }

        List<ConvertResult> allResults = new ArrayList<>();
        int totalRows = 0;

        for (Map.Entry<String, List<PlcMachineDataConfigEntity>> machineEntry : configsByMachine.entrySet()) {
            String machineName = machineEntry.getKey();
            ShootMachineEntity cachedMachine = machineByName.get(machineName);
            for (PlcMachineDataConfigEntity cfg : machineEntry.getValue()) {
                List<?> rows = callOnce(signedUtil, cfg.getDataCode(), cfg.getConfigCode());
                if (rows == null || rows.isEmpty()) {
                    continue;
                }
                totalRows += rows.size();
                List<ConvertResult> batchResults = convertAndFilter(rows, machineName, cachedMachine, resultFilter);
                allResults.addAll(batchResults);
            }
        }

        if (allResults.isEmpty()) {
            return 0;
        }

        log.info("外部PLC返回 {} 条数据，有效 {} 条", totalRows, allResults.size());

        List<PlcDataLatestEntity> entities = allResults.stream()
                .map(ConvertResult::entity)
                .collect(Collectors.toList());

        plcDataLatestMapper.batchUpsert(entities);
        log.info("同步完成，共写入 {} 条记录", entities.size());

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

    private List<ConvertResult> convertAndFilter(List<?> rows, String machineName, ShootMachineEntity cachedMachine, Predicate<ConvertResult> resultFilter) {
        List<ConvertResult> out = new ArrayList<>();
        int skipped = 0;
        int unMatched = 0;
        for (Object item : rows) {
            if (!(item instanceof Map<?, ?> map)) {
                skipped++;
                continue;
            }
            ConvertResult r = toEntity(map, machineName, cachedMachine);
            if (r == null) {
                skipped++;
                continue;
            }
            if (r.match() == null) {
                unMatched++;
                continue;
            }
            if (resultFilter != null && !resultFilter.test(r)) {
                continue;
            }
            out.add(r);
        }
        if (unMatched > 0 || skipped > 0) {
            log.debug("PLC数据转换: machine={}, 总={}, 有效={}, 未匹配={}, 跳过={}",
                    machineName, rows.size(), out.size(), unMatched, skipped);
        }
        return out;
    }

    /**
     * 单次调用外部 PLC 接口，按 dataCode + configCode 请求
     */
    private List<?> callOnce(SignedRestTemplateUtil signedUtil, String dataCode, String configCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (dataCode != null) {
            params.add("dataCode", dataCode);
            if (configCode != null) {
                params.add("configCode", configCode);
            }
        }
        try {
            log.debug("调用外部PLC接口 baseUrl={}, dataCode={}, configCode={}", externalPlcBaseUrl, dataCode, configCode);

            ResponseEntity<Map<String, Object>> response = signedUtil.get(
                    externalPlcBaseUrl, "/api/device/listByFactoryAndDevice",
                    params, new ParameterizedTypeReference<Map<String, Object>>() {});
            log.debug("外部PLC响应状态码={}, dataCode={}", response.getStatusCode(), dataCode);
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

    /**
     * 单条外部数据转实体；dataCode 为空返回 null。
     * 核心改动：用 FieldMatchingEngine 把第三方中文 fieldKey → field_mapping.internal_key，
     *          category_name 优先保留第三方原样，空时按 category_template + stationNo/gunCount 兜底重算。
     * @param cachedMachine 由 doSync 预查的机台实体（避免 N+1），可为 null（本地未配置该机台）
     */
    private ConvertResult toEntity(Map<?, ?> map, String machineName, ShootMachineEntity cachedMachine) {
        String remark = getStr(map, "remark");
        String dataCode = getStr(map, "dataCode");
        if (StrUtil.isBlank(dataCode)) {
            return null;
        }

        // field_key 优先使用第三方返回的 displayName；displayName 缺失时回退 remark
        String displayName = getStr(map, "displayName");
        String fieldKeySource = StrUtil.isNotBlank(displayName) ? displayName : remark;
        if (StrUtil.isBlank(fieldKeySource)) {
            return null;
        }
        fieldKeySource = StrUtil.subPre(fieldKeySource, 100).trim();

        String currentValue = getStr(map, "currentValue");
        String processedValue = getStr(map, "processedValue");
        String fieldValue = StrUtil.isNotBlank(processedValue) ? processedValue : currentValue;
        String areaName = getStr(map, "areaName");
        String thirdCategoryName = getStr(map, "categoryName");
        if (StrUtil.isBlank(thirdCategoryName)) {
            thirdCategoryName = "默认";
        }

        if (cachedMachine == null) {
            log.warn("本地未找到机台记录，跳过该条数据: machineName={}, dataCode={}", machineName, dataCode);
            return null;
        }
        Long machineId = cachedMachine.getId();
        int gunCount = cachedMachine.getGunCount() != null ? cachedMachine.getGunCount() : 4;

        if (StrUtil.isNotBlank(areaName) && !areaName.equals(machineName)) {
            log.warn("第三方 areaName={} 与 dataCode 归属机台={} 不一致，请核对映射", areaName, machineName);
        }

        // 匹配 field_mapping：拿到 internal_key + 维度（stationNo/gun/stage/idx/side）
        MatchResult match = FieldMatchingEngine.matchField(compiledMappings, fieldKeySource);

        // category_name：优先第三方原样，空或"默认"时按 category_template 兜底
        String categoryName;
        if (match != null && match.getMapping() != null
                && (StrUtil.isBlank(thirdCategoryName) || "默认".equals(thirdCategoryName))) {
            int stationNo = match.getStationNo() > 0 ? match.getStationNo()
                    : guessStationNoFromCategory(thirdCategoryName);
            categoryName = FieldMatchingEngine.buildCategoryName(
                    match.getMapping().getCategoryTemplate(), stationNo, gunCount);
        } else {
            categoryName = thirdCategoryName;
        }

        LocalDateTime dataTime = parseThirdPartyTime(map);

        PlcDataLatestEntity entity = new PlcDataLatestEntity();
        // 射枪温度：仅对应机台填充 deviceName（保留旧逻辑，不影响 category_template 工作）
        boolean isShootFive = ShootMachineCode.isShootFive(machineName);
        boolean isShootNine = ShootMachineCode.isShootNine(machineName);
        String deviceName = StrUtil.isNotBlank(areaName) ? StrUtil.subPre(areaName, 100) : "";
        if ("4射枪温度".equals(categoryName)) {
            entity.setDeviceName(isShootFive ? deviceName : "");
        } else if ("2射枪温度".equals(categoryName)) {
            entity.setDeviceName(isShootNine ? deviceName : "");
        } else {
            entity.setDeviceName(deviceName);
        }
        entity.setMachineId(machineId);
        entity.setDataTimestamp(dataTime);
        // field_key 改为存储英文 internal_key（如 MOLD_SET_TEMP_L_1），用于报警检测匹配
        String englishFieldKey = FieldMatchingEngine.buildFieldKey(match);
        entity.setFieldKey(StrUtil.isNotBlank(englishFieldKey) ? englishFieldKey : fieldKeySource);
        entity.setDataCode(dataCode);
        entity.setFieldValue(StrUtil.subPre(fieldValue, 500));
        entity.setCategoryName(StrUtil.subPre(categoryName, 100));
        // 第三方接口返回的原始点位名称（displayName/remark），用于追溯与展示
        entity.setThirdPointName(StrUtil.subPre(fieldKeySource, 200));
        entity.setCreateTime(dataTime);
        // 首次插入 & 值变化都走 Java 设置的 now；SQL ON DUPLICATE 会用 VALUES(value_changed_at) 更新（已对齐）
        entity.setValueChangedAt(LocalDateTime.now());

        // 落库时顺便把展示名写进 entity.name（非表字段），前端读接口时可直接显示
        if (match != null && match.getMapping() != null && StrUtil.isNotBlank(match.getMapping().getMatchPattern())) {
            entity.setName(match.getMapping().getMatchPattern());
        } else {
            entity.setName(fieldKeySource);
        }
        return new ConvertResult(entity, match);
    }

    /** 从第三方 category_name="站台3" 里提取 stationNo；提取不到默认 1 */
    private int guessStationNoFromCategory(String categoryName) {
        if (StrUtil.isBlank(categoryName)) return 1;
        if (categoryName.startsWith("站台")) {
            try {
                int n = Integer.parseInt(categoryName.substring(2));
                return n >= 1 ? n : 1;
            } catch (Exception ignore) { /* fallthrough */ }
        }
        return 1;
    }

    /**
     * 从第三方PLC返回的 map 中提取时间字段，支持 updateTime/timestamp/time 等常见字段名
     * 解析失败回退 LocalDateTime.now()
     */
    private LocalDateTime parseThirdPartyTime(Map<?, ?> map) {
        String dataUpdatedAt = getStr(map, "dataUpdatedAt");
        if (StrUtil.isNotBlank(dataUpdatedAt)) {
            LocalDateTime time = tryParseTime(dataUpdatedAt);
            if (time != null) {
                log.debug("使用第三方 dataUpdatedAt: {} -> {}", dataUpdatedAt, time);
                return time;
            }
        }
        String updateTime = getStr(map, "updateTime");
        if (StrUtil.isNotBlank(updateTime)) {
            LocalDateTime time = tryParseTime(updateTime);
            if (time != null) {
                log.debug("使用第三方 updateTime: {} -> {}", updateTime, time);
                return time;
            }
        }
        String createTime = getStr(map, "createTime");
        if (StrUtil.isNotBlank(createTime)) {
            LocalDateTime time = tryParseTime(createTime);
            if (time != null) {
                log.debug("使用第三方 createTime: {} -> {}", createTime, time);
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
            return LocalDateTime.parse(val.replace(" ", "T").substring(0, 19));
        } catch (Exception e) {
            // ignore
        }
        try {
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
