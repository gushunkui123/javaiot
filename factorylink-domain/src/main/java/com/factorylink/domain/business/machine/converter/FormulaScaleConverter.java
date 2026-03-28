package com.factorylink.domain.business.machine.converter;

import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemEntity;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaRequest;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaRequest.FormulaEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 配方实体 → 设备 API 请求转换器
 * <p>
 * 配方按物料类型分类下发：主料部分下发到主磅，非主料部分下发到微量磅。
 */
@Component
@RequiredArgsConstructor
public class FormulaScaleConverter {

    private static final String MATERIAL_TYPE_MAIN = "主料";

    private final BizMaterialService materialService;

    /**
     * 转换为主磅配方请求（含 StepNo，仅包含主料）
     */
    public ScaleFormulaRequest toMainScaleRequest(BizFormulaEntity formula, List<BizFormulaItemEntity> items) {
        ScaleFormulaRequest request = new ScaleFormulaRequest();
        request.setPlant("");
        request.setFormulaCode(formula.getFormulaCode());
        request.setFormulaName(formula.getFormulaName());

        Map<Long, BizMaterialEntity> materialMap = getMaterialMap(items);

        List<FormulaEntry> entries = items.stream()
            .filter(item -> {
                BizMaterialEntity material = materialMap.get(item.getMaterialId());
                return material != null && MATERIAL_TYPE_MAIN.equals(material.getMaterialType());
            })
            .map(item -> {
                FormulaEntry entry = new FormulaEntry();
                entry.setMaterialNo(materialMap.get(item.getMaterialId()).getMaterialName());
                entry.setMaterialWeight(item.getMaterialWeight());
                entry.setStepNo(item.getStepNo() != null ? item.getStepNo() : 1);
                return entry;
            }).toList();

        request.setFormulaEntryList(entries);
        return request;
    }

    /**
     * 转换为微量配方请求（无 StepNo，仅包含非主料）
     */
    public ScaleFormulaRequest toMicroScaleRequest(BizFormulaEntity formula, List<BizFormulaItemEntity> items) {
        ScaleFormulaRequest request = new ScaleFormulaRequest();
        request.setPlant("");
        request.setFormulaCode(formula.getFormulaCode());
        request.setFormulaName(formula.getFormulaName());

        Map<Long, BizMaterialEntity> materialMap = getMaterialMap(items);

        List<FormulaEntry> entries = items.stream()
            .filter(item -> {
                BizMaterialEntity material = materialMap.get(item.getMaterialId());
                return material == null || !MATERIAL_TYPE_MAIN.equals(material.getMaterialType());
            })
            .map(item -> {
                FormulaEntry entry = new FormulaEntry();
                entry.setMaterialNo(materialMap.containsKey(item.getMaterialId())
                    ? materialMap.get(item.getMaterialId()).getMaterialName()
                    : String.valueOf(item.getMaterialId()));
                entry.setMaterialWeight(item.getMaterialWeight());
                // 微量配方不需要 StepNo，不设置
                return entry;
            }).toList();

        request.setFormulaEntryList(entries);
        return request;
    }

    /**
     * 转换为删除请求（仅需 formulaCode）
     */
    public ScaleFormulaRequest toDeleteRequest(BizFormulaEntity formula) {
        ScaleFormulaRequest request = new ScaleFormulaRequest();
        request.setPlant("");
        request.setFormulaCode(formula.getFormulaCode());
        return request;
    }

    private Map<Long, BizMaterialEntity> getMaterialMap(List<BizFormulaItemEntity> items) {
        List<Long> materialIds = items.stream()
            .map(BizFormulaItemEntity::getMaterialId)
            .distinct()
            .toList();
        List<BizMaterialEntity> materials = materialService.listByIds(materialIds);
        return materials.stream()
            .collect(Collectors.toMap(BizMaterialEntity::getMaterialId, m -> m));
    }
}
