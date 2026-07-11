package com.agileboot.domain.factorylink.plc.util;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PlcDataSyncService {

    private static final Long MACHINE_ID = 5L;
    private static final String DEVICE_NAME = "射出机五号机";
    private static final List<String> DEFAULT_DATA_CODES = List.of("kkb756", "7JTAVe");

    private final PlcDataLatestMapper plcDataLatestMapper;
    private final ShootRuleAlarmService shootRuleAlarmService;
    private final RestTemplate restTemplate;

    @Value("${factory-link.workshop.base-url:http://10.0.100.225:8088}")
    private String externalPlcBaseUrl;

    @Value("${factory-link.workshop.api-key:}")
    private String workshopApiKey;

    @Value("${factory-link.workshop.api-secret:}")
    private String workshopApiSecret;

    /**
     * 同步外部 PLC 数据点到本地（固定 dataCodes）
     * @return 同步的记录数
     */
    public int syncPlcDataPoints() {
        return doSync(DEFAULT_DATA_CODES);
    }

    private int doSync(List<String> dataCodes) {
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

        // 合并所有请求结果，统一落库、统一触发一次报警检测
        List<PlcDataLatestEntity> entities = convertToEntities(allRows);
        if (entities.isEmpty()) {
            return 0;
        }

        plcDataLatestMapper.batchUpsert(entities);
        log.info("同步完成，共写入 {} 条记录", entities.size());

        try {
            shootRuleAlarmService.detectAlarmsByPlcData(MACHINE_ID);
            log.info("PLC数据同步后报警检测完成");
        } catch (Exception e) {
            log.error("PLC数据同步后报警检测失败", e);
        }

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
                    externalPlcBaseUrl, "/api/device/listByFactoryAndDevice",
                    params, new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> result = response.getBody();
            if (result == null) {
                log.warn("调用外部PLC接口返回为空, dataCode={}", dataCode);
                return null;
            }
            Object dataObj = result.get("data");
            if (!(dataObj instanceof List<?> rows)) {
                log.warn("数据格式异常, dataCode={}", dataCode);
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
        LocalDateTime now = LocalDateTime.now();
        int skipped = 0;
        for (Object item : rows) {
            if (!(item instanceof Map<?, ?> map)) {
                skipped++;
                continue;
            }
            PlcDataLatestEntity entity = toEntity(map, now);
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
     * 单条外部数据转实体；remark 或 dataCode 为空返回 null（避免空数据/唯一键冲突）
     */
    private PlcDataLatestEntity toEntity(Map<?, ?> map, LocalDateTime now) {
        String remark = getStr(map, "remark");
        String dataCode = getStr(map, "dataCode");
        if (StrUtil.isBlank(remark) || StrUtil.isBlank(dataCode)) {
            return null;
        }

        String currentValue = getStr(map, "currentValue");
        String categoryName = getStr(map, "categoryName");
        if (StrUtil.isBlank(categoryName)) {
            categoryName = "默认";
        }

        PlcDataLatestEntity entity = new PlcDataLatestEntity();
        entity.setDeviceName(DEVICE_NAME);
        entity.setMachineId(MACHINE_ID);
        entity.setDataTimestamp(now);
        entity.setFieldKey(StrUtil.subPre(remark, 100));
        entity.setDataCode(dataCode);
        entity.setFieldValue(StrUtil.subPre(currentValue, 500));
        entity.setCategoryName(StrUtil.subPre(categoryName, 100));
        entity.setCreateTime(now);
        return entity;
    }

    private String getStr(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
