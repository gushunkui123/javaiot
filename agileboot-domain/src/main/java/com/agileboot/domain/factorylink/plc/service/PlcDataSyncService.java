package com.agileboot.domain.factorylink.plc.service;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlcDataSyncService {

    private final ExternalPlcAuthService externalPlcAuthService;
    private final PlcDataLatestMapper plcDataLatestMapper;
    private final ShootRuleAlarmService shootRuleAlarmService;

    @Value("${factory-link.external-plc.base-url:http://10.0.100.225:8088}")
    private String externalPlcBaseUrl;

    /**
     * 同步外部 PLC 数据点到本地（saveOrUpdate）
     * @return 同步的记录数
     */
    public int syncPlcDataPoints() {
        // 1. 登录
        String token = externalPlcAuthService.login();
        if (token == null) {
            log.warn("外部PLC登录失败，跳过同步");
            return 0;
        }

        // 2. 获取数据点
        String url = externalPlcBaseUrl + "/workshopconfig/plcdatapoint/listByFactoryAndDevice";
        Map<String, Object> result = externalPlcAuthService.getJsonMapWithBearerToken(token, url);
        if (result == null) {
            log.warn("调用外部PLC接口失败，跳过同步");
            return 0;
        }

        // 3. 提取 rows
        Object rowsObj = result.get("rows");
        if (!(rowsObj instanceof List<?> rows)) {
            log.warn("数据格式异常，跳过同步");
            return 0;
        }

        log.info("外部PLC返回 {} 条数据", rows.size());

        // 4. 转换并保存
        List<PlcDataLatestEntity> entities = convertToEntities(rows);
        log.info("转换后 {} 条有效数据", entities.size());
        
        if (!entities.isEmpty()) {
            plcDataLatestMapper.batchUpsert(entities);
            log.info("同步完成，共写入 {} 条记录", entities.size());

            // 同步完成后触发报警检测（使用固定的 machineId=5）
            try {
                shootRuleAlarmService.detectAlarmsByPlcData(5L);
                log.info("PLC数据同步后报警检测完成");
            } catch (Exception e) {
                log.error("PLC数据同步后报警检测失败", e);
            }
        }

        return entities.size();
    }

    private List<PlcDataLatestEntity> convertToEntities(List<?> rows) {
        List<PlcDataLatestEntity> entities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 使用固定的设备信息
        String deviceName = "射出机五号机";
        Long machineId = 5L;

        int skippedRemark = 0;
        int skippedNotMap = 0;

        for (Object item : rows) {
            if (!(item instanceof Map<?, ?> map)) {
                skippedNotMap++;
                continue;
            }

            String remark = getStr(map, "remark");

            // remark 为空则跳过
            if (StrUtil.isBlank(remark)) {
                skippedRemark++;
                continue;
            }

            String currentValue = getStr(map, "currentValue");
            String categoryName = getStr(map, "categoryName");

            // category_name 为空时使用默认值，避免唯一索引失效
            if (StrUtil.isBlank(categoryName)) {
                categoryName = "默认";
            }

            PlcDataLatestEntity entity = new PlcDataLatestEntity();
            entity.setDeviceName(deviceName);
            entity.setMachineId(machineId);
            entity.setDataTimestamp(now);
            entity.setFieldKey(StrUtil.subPre(remark, 100));
            entity.setFieldValue(StrUtil.subPre(currentValue, 500));
            entity.setCategoryName(StrUtil.subPre(categoryName, 100));
            entity.setCreateTime(now);
            entities.add(entity);
        }

        log.info("总条数={}, 有效={}, 如果 remark 为空={}, 非Map跳过={}",
                rows.size(), entities.size(), skippedRemark, skippedNotMap);

        return entities;
    }

    private String getStr(Map<?, ?> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private Long parseLong(Object obj) {
        if (obj instanceof Number n) return n.longValue();
        if (obj instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}
