package com.agileboot.domain.factorylink.plc.controller;

import com.agileboot.domain.factorylink.plc.entity.FieldMappingEntity;
import com.agileboot.domain.factorylink.plc.mapper.FieldMappingMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "FactoryLink 字段映射")
@RestController
@RequestMapping("/api/field-mapping")
@Validated
@RequiredArgsConstructor
@Slf4j
public class FieldMappingController {

    private final FieldMappingMapper fieldMappingMapper;

    /**
     * 查询字段维度上限。
     * 所有维度上限（idx/stage）均来自 field_mapping.stage_count：
     *   - MOLD_SET_TEMP 的 stage_count=2 → idx 上限
     *   - GUN_TEMP 的 stage_count=4 → 射枪温度阶段数
     *   - INJECT_SPEED 的 stage_count=5 → 射出速度阶段数
     * 返回结构：{ stageFields: { MOLD_SET_TEMP: 2, GUN_TEMP: 4, INJECT_SPEED: 5, ... } }
     */
    @Operation(summary = "查询字段维度上限（idx/stage 等，来自 field_mapping.stage_count）")
    @GetMapping("/dimensions")
    public Map<String, Object> dimensions() {
        Map<String, Object> result = new HashMap<>();

        Map<String, Integer> stageFields = new HashMap<>();
        List<FieldMappingEntity> mappings = fieldMappingMapper.listEnabled();
        for (FieldMappingEntity m : mappings) {
            if (m.getStageCount() != null && m.getStageCount() > 0) {
                stageFields.put(m.getInternalKey(), m.getStageCount());
            }
        }
        result.put("stageFields", stageFields);

        return result;
    }
}
