package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.infrastructure.cache.RedisUtil;
import com.agileboot.infrastructure.cache.redis.CacheKeyEnum;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlcDataServiceImpl extends ServiceImpl<PlcDataMapper, PlcDataEntity> implements PlcDataService {

    /** PLC 字段名（field_key）最大长度 */
    private static final int KEY_MAX = 100;
    /** PLC 字段值（field_value）最大长度 */
    private static final int VAL_MAX = 500;

    private final RedisUtil redisUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ingestFlatJsonTelemetry(String deviceName, String jsonPayload) {
        if (StrUtil.isBlank(deviceName) || StrUtil.isBlank(jsonPayload)) {
            return;
        }
        JSONObject root;
        try {
            root = JSONUtil.parseObj(jsonPayload);
        } catch (Exception e) {
            log.warn("PLC JSON parse failed: {}", e.getMessage());
            return;
        }
        String device = deviceName.trim();
        Date ts = new Date();
        List<PlcDataEntity> rows = new ArrayList<>(root.size());
        for (String key : root.keySet()) {
            PlcDataEntity row = new PlcDataEntity();
            row.setDeviceName(device);
            row.setDataTimestamp(ts);
            row.setFieldKey(StrUtil.subPre(key, KEY_MAX));
            row.setFieldValue(StrUtil.subPre(Convert.toStr(root.get(key), ""), VAL_MAX));
            row.setDeleted(false);
            rows.add(row);
        }
        if (!rows.isEmpty()) {
            saveBatch(rows, 200);
            cacheLatestTimestampIfNewer(device, ts);
        }
    }

    @Override
    public List<PlcDataEntity> listLatestSameTimestampByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return List.of();
        }
        List<PlcDataEntity> rows =
                baseMapper.selectListLatestSameTimestampByDeviceName(deviceName.trim());
        for (PlcDataEntity row : rows) {
            row.setName(PlcFieldKeyDisplayNames.resolve(row.getFieldKey()));
        }
        return rows;
    }

    /**
     * 批量查询各设备最新采集时间。
     * 
     * 查库后回写/清理 Redis。
     */
    @Override
    public Map<String, Date> mapLatestDataTimestampByDeviceNames(Collection<String> deviceNames) {
        if (deviceNames == null || deviceNames.isEmpty()) {
            return Map.of();
        }
        List<String> names =
                deviceNames.stream()
                        .filter(StrUtil::isNotBlank)
                        .map(String::trim)
                        .distinct()
                        .toList();
        if (names.isEmpty()) {
            return Map.of();
        }

        Map<String, Date> fromDb = loadLatestTimestampFromDb(names);
        for (String name : names) {
            Date ts = fromDb.get(name);
            if (ts != null) {
                cacheLatestTimestamp(name, ts);
            } else {
                redisUtil.deleteObject(latestTimestampCacheKey(name));
            }
        }
        return fromDb;
    }
        // 从数据库查询
    private Map<String, Date> loadLatestTimestampFromDb(List<String> deviceNames) {
        List<Map<String, Object>> rows = baseMapper.selectLatestTimestampByDeviceNames(deviceNames);
        Map<String, Date> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String deviceName = (String) row.get("device_name");
            Date timestamp = Convert.toDate(row.get("data_timestamp"), null);
            if (deviceName != null && timestamp != null) {
                result.put(deviceName, timestamp);
            }
        }
        return result;
    }

   
    private void cacheLatestTimestampIfNewer(String deviceName, Date timestamp) {
        if (StrUtil.isBlank(deviceName) || timestamp == null) {
            return;
        }
        String key = latestTimestampCacheKey(deviceName);
        Long cached = redisUtil.getCacheObject(key);
        if (cached != null && timestamp.getTime() < cached) {
            return;
        }
        cacheLatestTimestamp(deviceName, timestamp);
    }
 // 写缓存
    private void cacheLatestTimestamp(String deviceName, Date timestamp) {
        if (StrUtil.isBlank(deviceName) || timestamp == null) {
            return;
        }
        CacheKeyEnum cacheKey = CacheKeyEnum.PLC_DEVICE_LATEST_TS_KEY;
        redisUtil.setCacheObject(
                latestTimestampCacheKey(deviceName),
                timestamp.getTime(),
                cacheKey.expiration(),
                cacheKey.timeUnit());
    }
        // 缓存键
    private static String latestTimestampCacheKey(String deviceName) {
        return CacheKeyEnum.PLC_DEVICE_LATEST_TS_KEY.key() + deviceName;
    }
}
