package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ingestFlatJsonTelemetry(Long machineId, String deviceName, String jsonPayload) {
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
            row.setMachineId(machineId);
            row.setDeviceName(device);
            row.setDataTimestamp(ts);
            row.setFieldKey(StrUtil.subPre(key, KEY_MAX));
            row.setFieldValue(StrUtil.subPre(Convert.toStr(root.get(key), ""), VAL_MAX));
            row.setDeleted(false);
            rows.add(row);
        }
        if (!rows.isEmpty()) {
            saveBatch(rows, 200);
        }
    }

    @Override
    public List<PlcDataEntity> listLatestSameTimestampByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return List.of();
        }
        return enrichDisplayNames(
                baseMapper.selectListLatestSameTimestampByDeviceName(deviceName.trim()));
    }

    @Override
    public List<PlcDataEntity> listLatestSameTimestampByMachineId(Long machineId) {
        if (machineId == null) {
            return List.of();
        }
        return enrichDisplayNames(baseMapper.selectListLatestSameTimestampByMachineId(machineId));
    }

    private List<PlcDataEntity> enrichDisplayNames(List<PlcDataEntity> rows) {
        rows.forEach(row -> row.setName(PlcFieldKeyDisplayNames.resolve(row.getFieldKey())));
        return rows;
    }

    @Override
    public Map<Long, Date> mapLatestDataTimestampByMachineIds(Collection<Long> machineIds) {
        if (machineIds == null || machineIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids =
                machineIds.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Map<String, Object>> rows = baseMapper.selectLatestTimestampByMachineIds(ids);
        Map<Long, Date> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long machineId = Convert.toLong(row.get("machine_id"), null);
            Date timestamp = Convert.toDate(row.get("data_timestamp"), null);
            if (machineId != null && timestamp != null) {
                result.put(machineId, timestamp);
            }
        }
        return result;
    }
}
