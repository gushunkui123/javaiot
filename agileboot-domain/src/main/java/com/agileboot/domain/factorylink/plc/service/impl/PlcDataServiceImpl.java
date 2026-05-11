package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PlcDataServiceImpl extends ServiceImpl<PlcDataMapper, PlcDataEntity> implements PlcDataService {

    // PLC 字段 最大长度
    private static final int KEY_MAX = 100;
    // PLC 字段值 最大长度
    private static final int VAL_MAX = 500;

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
        Date ts = new Date();
        List<PlcDataEntity> rows = new ArrayList<>(root.size());
        for (String key : root.keySet()) {
            PlcDataEntity row = new PlcDataEntity();
            row.setDeviceName(deviceName);
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
        PlcDataEntity newest =
                lambdaQuery()
                        .eq(PlcDataEntity::getDeviceName, deviceName)
                        .orderByDesc(PlcDataEntity::getDataTimestamp)
                        .orderByDesc(PlcDataEntity::getId)
                        .last("LIMIT 1")
                        .one();
        if (newest == null || newest.getDataTimestamp() == null) {
            return List.of();
        }
        Date latestTs = newest.getDataTimestamp();
        List<PlcDataEntity> rows =
                lambdaQuery()
                        .eq(PlcDataEntity::getDeviceName, deviceName)
                        .eq(PlcDataEntity::getDataTimestamp, latestTs)
                        .orderByAsc(PlcDataEntity::getId)
                        .list();
        for (PlcDataEntity row : rows) {
            row.setName(PlcFieldKeyDisplayNames.resolve(row.getFieldKey()));
        }
        return rows;
    }
}
