package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        LocalDateTime ts = LocalDateTime.now();
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

    //根据设备名称查询
    @Override
    public List<PlcDataEntity> listLatestSameTimestampByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return List.of();
        }
        return enrichDisplayNames(baseMapper.selectListLatestSameTimestampByDeviceName(deviceName.trim()));
    }

    //根据id查询
    @Override
    public List<PlcDataEntity> listLatestSameTimestampByMachineId(Long machineId) {
        if (machineId == null) {
            return List.of();
        }
        return enrichDisplayNames(baseMapper.selectListLatestSameTimestampByMachineId(machineId));
    }

    //解析显示名称
    private List<PlcDataEntity> enrichDisplayNames(List<PlcDataEntity> rows) {
        rows.forEach(row -> row.setName(PlcFieldKeyDisplayNames.resolve(row.getFieldKey())));
        return rows;
    }

    //根据所有ids查询
    @Override
    public Map<Long, LocalDateTime> mapLatestDataTimestampByMachineIds(Collection<Long> machineIds) {
        if (machineIds == null || machineIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids =
                machineIds.stream().filter(id -> id != null && id > 0).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return baseMapper
                .selectList(
                        new QueryWrapper<PlcDataEntity>()
                                .select("machine_id AS machineId", "MAX(`timestamp`) AS dataTimestamp")
                                .eq("deleted", 0)
                                .in("machine_id", ids)
                                .groupBy("machine_id"))
                .stream()
                .filter(row -> row.getMachineId() != null && row.getDataTimestamp() != null)
                .collect(Collectors.toMap(PlcDataEntity::getMachineId, PlcDataEntity::getDataTimestamp, (a, b) -> a));
    }
}
