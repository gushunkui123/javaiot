package com.factorylink.domain.business.formula;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.factorylink.common.config.FactoryLinkConfig;
import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.business.formula.command.AddFormulaCommand;
import com.factorylink.domain.business.formula.command.FormulaItemCommand;
import com.factorylink.domain.business.formula.command.UpdateFormulaCommand;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.formula.dto.FormulaDTO;
import com.factorylink.domain.business.formula.dto.FormulaExcelDTO;
import com.factorylink.domain.business.formula.dto.FormulaItemDTO;
import com.factorylink.domain.business.formula.model.FormulaModel;
import com.factorylink.domain.business.formula.model.FormulaModelFactory;
import com.factorylink.domain.business.formula.query.FormulaQuery;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class FormulaApplicationService {

    private final FormulaModelFactory formulaModelFactory;

    private final BizFormulaService formulaService;

    private final BizFormulaItemService formulaItemService;

    private final AuditUserEnricher auditUserEnricher;

    private final BizMaterialService materialService;

    public PageDTO<FormulaDTO> getFormulaList(FormulaQuery query) {
        Page<BizFormulaEntity> page = formulaService.page(query.toPage(), query.toQueryWrapper());
        List<FormulaDTO> records = page.getRecords().stream().map(FormulaDTO::new).toList();
        auditUserEnricher.enrich(records);
        return new PageDTO<>(records, page.getTotal());
    }

    public FormulaDTO getFormulaInfo(Long formulaId) {
        FormulaModel model = formulaModelFactory.loadById(formulaId);
        FormulaDTO dto = new FormulaDTO(model);
        auditUserEnricher.enrich(dto);

        LambdaQueryWrapper<BizFormulaItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizFormulaItemEntity::getFormulaId, formulaId)
            .orderByAsc(BizFormulaItemEntity::getSortOrder);
        List<BizFormulaItemEntity> itemEntities = formulaItemService.list(queryWrapper);

        List<FormulaItemDTO> itemDTOs = itemEntities.stream().map(entity -> {
            FormulaItemDTO itemDTO = new FormulaItemDTO();
            BeanUtil.copyProperties(entity, itemDTO);
            return itemDTO;
        }).toList();
        dto.setItems(itemDTOs);

        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addFormula(AddFormulaCommand addCommand) {
        FormulaModel formulaModel = formulaModelFactory.create();
        formulaModel.loadFromAddCommand(addCommand);
        formulaModel.checkFormulaCodeUnique();
        formulaModel.checkMaterialsExist();
        formulaModel.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFormula(UpdateFormulaCommand updateCommand) {
        FormulaModel formulaModel = formulaModelFactory.loadById(updateCommand.getFormulaId());
        formulaModel.loadFromUpdateCommand(updateCommand);
        formulaModel.checkFormulaCodeUnique();
        formulaModel.checkMaterialsExist();
        formulaModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFormula(BulkOperationCommand<Long> deleteCommand) {
        for (Long id : deleteCommand.getIds()) {
            FormulaModel formulaModel = formulaModelFactory.loadById(id);
            formulaModel.deleteById();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void importFormula(InputStream inputStream) {
        List<FormulaExcelDTO> excelDTOs = FormulaExcelParser.parseAll(inputStream);
        if (excelDTOs.isEmpty()) {
            throw new ApiException(Business.COMMON_UNSUPPORTED_OPERATION);
        }

        // 构建原料编号 -> ID 映射（所有 sheet 共用）
        List<BizMaterialEntity> allMaterials = materialService.list();
        Map<String, Long> materialCodeToId = allMaterials.stream()
            .filter(m -> StrUtil.isNotBlank(m.getMaterialCode()))
            .collect(Collectors.toMap(BizMaterialEntity::getMaterialCode, BizMaterialEntity::getMaterialId, (a, b) -> a));

        for (FormulaExcelDTO excelDTO : excelDTOs) {
            // 跳过配方编号为空的 sheet
            if (StrUtil.isBlank(excelDTO.getFormulaCode())) {
                continue;
            }
            // 跳过已存在的配方编号
            if (formulaService.isFormulaCodeDuplicated(null, excelDTO.getFormulaCode())) {
                continue;
            }

            AddFormulaCommand addCommand = buildAddCommand(excelDTO, materialCodeToId);
            addFormula(addCommand);
        }
    }

    private AddFormulaCommand buildAddCommand(FormulaExcelDTO excelDTO, Map<String, Long> materialCodeToId) {
        AddFormulaCommand addCommand = new AddFormulaCommand();
        addCommand.setFormulaCode(excelDTO.getFormulaCode());
        addCommand.setFormulaName(excelDTO.getSpecification());
        addCommand.setFormulaDate(excelDTO.getDate());
        addCommand.setMoldCode(excelDTO.getModel());
        addCommand.setBatch(excelDTO.getBatch());
        addCommand.setBatchCount(excelDTO.getBatchCount());
        addCommand.setOrderNo(excelDTO.getOrderNo());

        List<FormulaItemCommand> items = new ArrayList<>();
        for (FormulaExcelDTO.Item excelItem : excelDTO.getItems()) {
            Long materialId = materialCodeToId.get(excelItem.getCode());
            if (materialId == null) {
                if (FactoryLinkConfig.isFormulaImportAutoCreateMaterial()) {
                    BizMaterialEntity newMaterial = new BizMaterialEntity();
                    newMaterial.setMaterialCode(excelItem.getCode());
                    newMaterial.setMaterialName(excelItem.getCode());
                    newMaterial.setMaterialType(convertMaterialType(excelItem.getCategory()));
                    materialService.save(newMaterial);
                    materialId = newMaterial.getMaterialId();
                    materialCodeToId.put(excelItem.getCode(), materialId);
                } else {
                    throw new ApiException(Business.FORMULA_IMPORT_MATERIAL_NOT_FOUND,
                        excelItem.getCode());
                }
            }
            FormulaItemCommand item = new FormulaItemCommand();
            item.setMaterialId(materialId);
            item.setMaterialWeight(excelItem.getWeight());
            item.setRatio(excelItem.getRatio());
            item.setWeightUnit(StrUtil.isNotBlank(excelItem.getUnit()) ? excelItem.getUnit() : "g");
            item.setSortOrder(excelItem.getSortOrder());
            items.add(item);
        }
        addCommand.setItems(items);
        return addCommand;
    }

    private String convertMaterialType(String category) {
        if (category == null) {
            return null;
        }
        if (category.contains("主料")) {
            return "主料";
        }
        if (category.contains("顆粒") || category.contains("颗粒")) {
            return "颗粒";
        }
        if (category.contains("粉未") || category.contains("粉末")) {
            return "粉末";
        }
        if (category.contains("色粒")) {
            return "色粒";
        }
        return category;
    }

}
