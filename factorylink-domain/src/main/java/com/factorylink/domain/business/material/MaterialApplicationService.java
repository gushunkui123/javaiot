package com.factorylink.domain.business.material;

import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.business.formula.db.BizFormulaItemEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.material.command.AddMaterialCommand;
import com.factorylink.domain.business.material.command.UpdateMaterialCommand;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.factorylink.domain.business.material.dto.MaterialDTO;
import com.factorylink.domain.business.material.model.MaterialModel;
import com.factorylink.domain.business.material.model.MaterialModelFactory;
import com.factorylink.domain.business.material.query.MaterialQuery;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class MaterialApplicationService {

    private final MaterialModelFactory materialModelFactory;

    private final BizMaterialService materialService;

    private final BizFormulaItemService formulaItemService;

    private final AuditUserEnricher auditUserEnricher;

    public PageDTO<MaterialDTO> getMaterialList(MaterialQuery query) {
        Page<BizMaterialEntity> page = materialService.page(query.toPage(), query.toQueryWrapper());
        List<MaterialDTO> records = page.getRecords().stream().map(MaterialDTO::new).toList();
        auditUserEnricher.enrich(records);
        return new PageDTO<>(records, page.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addMaterial(AddMaterialCommand addCommand) {
        MaterialModel materialModel = materialModelFactory.create();
        materialModel.loadFromAddCommand(addCommand);
        materialModel.checkMaterialUnique();
        materialModel.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMaterial(UpdateMaterialCommand updateCommand) {
        MaterialModel materialModel = materialModelFactory.loadById(updateCommand.getMaterialId());
        materialModel.loadFromUpdateCommand(updateCommand);
        materialModel.checkMaterialUnique();
        materialModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMaterial(BulkOperationCommand<Long> deleteCommand) {
        boolean referenced = formulaItemService.exists(
            new LambdaQueryWrapper<BizFormulaItemEntity>()
                .in(BizFormulaItemEntity::getMaterialId, deleteCommand.getIds()));
        if (referenced) {
            throw new ApiException(Business.MATERIAL_ALREADY_ASSIGNED_TO_FORMULA_CAN_NOT_BE_DELETED);
        }
        materialService.removeBatchByIds(deleteCommand.getIds());
    }

    @Transactional(rollbackFor = Exception.class)
    public void importMaterial(List<AddMaterialCommand> commands) {
        for (AddMaterialCommand command : commands) {
            addMaterial(command);
        }
    }

}
