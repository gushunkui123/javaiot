package com.agileboot.domain.factorylink.shootmachine.service.impl;

import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataLatestMapper;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootBoardFieldConfigEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootBoardFieldEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootBoardFieldConfigMapper;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootBoardFieldConfigService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** 看板固定字段配置服务实现 */
@Service
@RequiredArgsConstructor
public class ShootBoardFieldConfigServiceImpl implements ShootBoardFieldConfigService {

    private final ShootBoardFieldConfigMapper boardFieldConfigMapper;
    private final PlcDataLatestMapper plcDataLatestMapper;
    private final ShootRuleAlarmService shootRuleAlarmService;

    @Override
    public List<ShootBoardFieldEntity> listBoardFixedFields(Long machineId, Integer stationNo) {
        // 单站台/全局：复用批量逻辑，仅处理一个站台
        Map<Integer, List<ShootBoardFieldEntity>> map = listBoardFixedFieldsForStations(
                machineId, stationNo == null ? List.of(-1) : List.of(stationNo));
        if (stationNo == null) {
            return map.getOrDefault(-1, List.of());
        }
        return map.getOrDefault(stationNo, List.of());
    }

    @Override
    public Map<Integer, List<ShootBoardFieldEntity>> listBoardFixedFieldsForStations(Long machineId, List<Integer> stationNos) {
        Map<Integer, List<ShootBoardFieldEntity>> result = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(stationNos)) {
            return result;
        }

        // 1. 优先取机器专属配置；若无专属，则回退到通用模板（全站台共享，只查一次）
        List<ShootBoardFieldConfigEntity> configs =
                boardFieldConfigMapper.selectEnabledByMachineId(machineId, false);
        if (configs.isEmpty()) {
            configs = boardFieldConfigMapper.selectEnabledByMachineId(machineId, true);
        }

        // 2. 构建 fieldCode -> 最高报警等级 映射（全站台共享，只查一次）
        Map<String, String> alarmLevelByField = new HashMap<>();
        for (ShootRuleAlarmEntity alarm : shootRuleAlarmService.listUnhandledWithRelation(machineId)) {
            String code = alarm.getFieldCode();
            if (code == null) continue;
            alarmLevelByField.merge(code, alarm.getAlarmLevel(), (oldL, newL) ->
                    "red".equalsIgnoreCase(oldL) ? oldL : newL);
        }

        // 3. 批量查询 PLC 实时值：(field_key, category_name) 组合一次 IN 查询
        //    plc_data_latest.category_name 由 FieldMatchingEngine.buildCategoryName 生成，规律为 "站台"+stationNo。
        //    categoryName=null 表示全局值（stationNo=-1 占位，用于无站台机器的机器级展示）。
        Map<String, PlcDataLatestEntity> latestByKey = new HashMap<>();
        List<Map<String, String>> fieldCategories = new ArrayList<>();
        for (ShootBoardFieldConfigEntity cfg : configs) {
            for (Integer stationNo : stationNos) {
                String categoryName = stationNo == -1 ? null : "站台" + stationNo;
                // 占位：null 的 categoryName 需要单独走全局查询，这里只收集带站台的组合
                if (categoryName != null) {
                    Map<String, String> fc = new HashMap<>();
                    fc.put("fieldKey", cfg.getFieldKey());
                    fc.put("categoryName", categoryName);
                    fieldCategories.add(fc);
                }
            }
        }
        if (!fieldCategories.isEmpty()) {
            for (PlcDataLatestEntity data : plcDataLatestMapper.selectLatestByFieldAndCategoryList(machineId, fieldCategories)) {
                // 同一组合可能返回多条（create_time 不同），保留最新一条
                latestByKey.putIfAbsent(data.getFieldKey() + "\u0000" + data.getCategoryName(), data);
            }
        }

        // 4. 逐站台组装响应
        for (Integer stationNo : stationNos) {
            List<ShootBoardFieldEntity> list = new ArrayList<>();
            String categoryName = stationNo == -1 ? null : "站台" + stationNo;
            for (ShootBoardFieldConfigEntity cfg : configs) {
                ShootBoardFieldEntity resp = new ShootBoardFieldEntity();
                resp.setFieldKey(cfg.getFieldKey());
                resp.setDisplayName(cfg.getDisplayName());
                resp.setGroupName(cfg.getGroupName());

                PlcDataLatestEntity latest;
                if (categoryName != null) {
                    latest = latestByKey.get(cfg.getFieldKey() + "\u0000" + categoryName);
                } else {
                    latest = plcDataLatestMapper.selectLatestByMachineIdAndFieldKeyForGlobal(
                            machineId, cfg.getFieldKey());
                }
                resp.setFieldValue(latest != null ? latest.getFieldValue() : null);

                String level = alarmLevelByField.get(cfg.getFieldKey());
                resp.setAlarming(level != null);
                resp.setAlarmLevel(level);
                list.add(resp);
            }
            result.put(stationNo, list);
        }
        return result;
    }
}
