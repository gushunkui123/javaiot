package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.dynamic.datasource.annotation.DS;
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
@DS("master")
@RequiredArgsConstructor
public class PlcDataServiceImpl extends ServiceImpl<PlcDataMapper, PlcDataEntity> implements PlcDataService {

    private final PlcDataLatestMapper plcDataLatestMapper;

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
        List<PlcDataLatestEntity> latestRows = new ArrayList<>(root.size());
        for (String key : root.keySet()) {
            String fk = StrUtil.subPre(key, KEY_MAX);
            String fv = StrUtil.subPre(Convert.toStr(root.get(key), ""), VAL_MAX);

            // 历史表行
            PlcDataEntity row = new PlcDataEntity();
            row.setMachineId(machineId);
            row.setDeviceName(device);
            row.setDataTimestamp(ts);
            row.setFieldKey(fk);
            row.setFieldValue(fv);
            row.setDeleted(false);
            rows.add(row);

            // 最新表行
            PlcDataLatestEntity latestRow = new PlcDataLatestEntity();
            latestRow.setMachineId(machineId);
            latestRow.setDeviceName(device);
            latestRow.setDataTimestamp(ts);
            latestRow.setFieldKey(fk);
            latestRow.setFieldValue(fv);
            latestRows.add(latestRow);
        }
        if (!rows.isEmpty()) {
            // 1. 写入历史表（追加）
            saveBatch(rows, 200);
            // 2. 写入最新表（upsert：存在则更新，不存在则插入）
            plcDataLatestMapper.batchUpsert(latestRows);
        }
    }

    //根据设备名称查询
    @Override
    public List<PlcDataEntity> listLatestSameTimestampByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return List.of();
        }
        List<PlcDataLatestEntity> latestRows = plcDataLatestMapper.selectListByDeviceName(deviceName.trim());
        return toPlcDataEntities(latestRows);
    }

    //根据id查询
    @Override
    public List<PlcDataEntity> listLatestSameTimestampByMachineId(Long machineId) {
        if (machineId == null) {
            return List.of();
        }
        List<PlcDataLatestEntity> latestRows = plcDataLatestMapper.selectListByMachineId(machineId);
        return toPlcDataEntities(latestRows);
    }

    //根据所有ids查询最新数据戳
    @Override
    public Map<Long, LocalDateTime> mapLatestDataTimestampByMachineIds(Collection<Long> machineIds) {
        if (machineIds == null || machineIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = machineIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return plcDataLatestMapper
                .selectList(
                        new QueryWrapper<PlcDataLatestEntity>()
                                .select("machine_id AS machineId", "MAX(`timestamp`) AS dataTimestamp")
                                .in("machine_id", ids)
                                .groupBy("machine_id"))
                .stream()
                .filter(row -> row.getMachineId() != null && row.getDataTimestamp() != null)
                .collect(Collectors.toMap(
                        PlcDataLatestEntity::getMachineId,
                        PlcDataLatestEntity::getDataTimestamp,
                        (a, b) -> a));
    }

    // ===== 转换工具方法 =====

    /** PlcDataLatestEntity → PlcDataEntity */
    private PlcDataEntity toPlcDataEntity(PlcDataLatestEntity latest) {
        PlcDataEntity entity = new PlcDataEntity();
        entity.setId(latest.getId());
        entity.setDeviceName(latest.getDeviceName());
        entity.setMachineId(latest.getMachineId());
        entity.setDataTimestamp(latest.getDataTimestamp());
        entity.setFieldKey(latest.getFieldKey());
        entity.setFieldValue(latest.getFieldValue());
        entity.setCategoryName(latest.getCategoryName());
        entity.setCreateTime(latest.getCreateTime());
        entity.setDeleted(false);
        return entity;
    }

    private List<PlcDataEntity> toPlcDataEntities(List<PlcDataLatestEntity> latestRows) {
        return latestRows.stream().map(this::toPlcDataEntity).toList();
    }
}
