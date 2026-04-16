package com.factorylink.domain.business.machine.converter;

import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemEntity;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaProcessRequest;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaProcessRequest.ProcessEntry;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaRequest;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaRequest.FormulaEntry;
import java.math.BigDecimal;
import java.util.ArrayList;
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
                entry.setMaterialNo(materialMap.get(item.getMaterialId()).getMaterialCode());
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
                    ? materialMap.get(item.getMaterialId()).getMaterialCode()
                    : String.valueOf(item.getMaterialId()));
                entry.setMaterialWeight(item.getMaterialWeight());
                // 微量配方不需要 StepNo，不设置
                return entry;
            }).toList();

        request.setFormulaEntryList(entries);
        return request;
    }

    /**
     * 为主磅配方请求添加配方名条目（用于主磅显示配方名称）
     */
    public void addFormulaNameEntry(ScaleFormulaRequest request, BizFormulaEntity formula) {
        FormulaEntry entry = new FormulaEntry();
        entry.setMaterialNo(formula.getFormulaCode());
        entry.setMaterialWeight(BigDecimal.ONE);
        entry.setStepNo(1);
        List<FormulaEntry> entries = new ArrayList<>(request.getFormulaEntryList());
        entries.addFirst(entry);
        request.setFormulaEntryList(entries);
    }

    /**
     * 构建默认配方工艺请求（投料 → 密炼 → 排料）
     */
    public ScaleFormulaProcessRequest toDefaultProcessRequest(BizFormulaEntity formula) {
        ScaleFormulaProcessRequest request = new ScaleFormulaProcessRequest();
        request.setPlant("");
        request.setFormulaCode(formula.getFormulaCode());

        List<ProcessEntry> entries = new ArrayList<>();

        // 步骤1: 投料 (ActionID=2)
        ProcessEntry step1 = new ProcessEntry();
        step1.setStepNo(1);
        step1.setActionId(2);
        step1.setMixingTime(BigDecimal.ZERO);
        step1.setMixingCurrent(BigDecimal.ZERO);
        step1.setMixingTemp(BigDecimal.ZERO);
        step1.setRotateSpeed(BigDecimal.ZERO);
        step1.setPressure(BigDecimal.ZERO);
        step1.setClosingConditionId(0);
        step1.setTurningTimes(0);
        step1.setRisingTime(BigDecimal.ZERO);
        step1.setFallingTime(BigDecimal.ZERO);
        entries.add(step1);

        // 步骤2: 密炼 (ActionID=4)
        ProcessEntry step2 = new ProcessEntry();
        step2.setStepNo(0);
        step2.setActionId(4);
        step2.setMixingTime(BigDecimal.ZERO);
        step2.setMixingCurrent(BigDecimal.ZERO);
        step2.setMixingTemp(BigDecimal.ZERO);
        step2.setRotateSpeed(BigDecimal.ZERO);
        step2.setPressure(BigDecimal.ZERO);
        step2.setClosingConditionId(0);
        step2.setTurningTimes(0);
        step2.setRisingTime(BigDecimal.ZERO);
        step2.setFallingTime(BigDecimal.ZERO);
        entries.add(step2);

        // 步骤3: 排料 (ActionID=9)
        ProcessEntry step3 = new ProcessEntry();
        step3.setStepNo(0);
        step3.setActionId(9);
        step3.setMixingTime(BigDecimal.ZERO);
        step3.setMixingCurrent(BigDecimal.ZERO);
        step3.setMixingTemp(BigDecimal.ZERO);
        step3.setRotateSpeed(BigDecimal.ZERO);
        step3.setPressure(BigDecimal.ZERO);
        step3.setClosingConditionId(0);
        step3.setTurningTimes(0);
        step3.setRisingTime(BigDecimal.ZERO);
        step3.setFallingTime(BigDecimal.ZERO);
        entries.add(step3);

        request.setFormulaProcessEntryList(entries);
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
