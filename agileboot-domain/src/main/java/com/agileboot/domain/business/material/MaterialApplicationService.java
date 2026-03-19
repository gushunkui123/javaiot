package com.agileboot.domain.business.material;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.common.audit.AuditUserEnricher;
import com.agileboot.domain.business.material.command.AddMaterialCommand;
import com.agileboot.domain.business.material.command.UpdateMaterialCommand;
import com.agileboot.domain.business.material.db.BizMaterialEntity;
import com.agileboot.domain.business.material.db.BizMaterialService;
import com.agileboot.domain.business.material.dto.MaterialDTO;
import com.agileboot.domain.business.material.model.MaterialModel;
import com.agileboot.domain.business.material.model.MaterialModelFactory;
import com.agileboot.domain.business.material.query.MaterialQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class MaterialApplicationService {

    private final MaterialModelFactory materialModelFactory;

    private final BizMaterialService materialService;

    private final AuditUserEnricher auditUserEnricher;

    public PageDTO<MaterialDTO> getMaterialList(MaterialQuery query) {
        Page<BizMaterialEntity> page = materialService.page(query.toPage(), query.toQueryWrapper());
        List<MaterialDTO> records = page.getRecords().stream().map(MaterialDTO::new).toList();
        auditUserEnricher.enrich(records);
        return new PageDTO<>(records, page.getTotal());
    }

    public void addMaterial(AddMaterialCommand addCommand) {
        MaterialModel materialModel = materialModelFactory.create();
        materialModel.loadFromAddCommand(addCommand);
        materialModel.checkMaterialUnique();
        materialModel.insert();
    }

    public void updateMaterial(UpdateMaterialCommand updateCommand) {
        MaterialModel materialModel = materialModelFactory.loadById(updateCommand.getMaterialId());
        materialModel.loadFromUpdateCommand(updateCommand);
        materialModel.checkMaterialUnique();
        materialModel.updateById();
    }

    public void deleteMaterial(BulkOperationCommand<Long> deleteCommand) {
        materialService.removeBatchByIds(deleteCommand.getIds());
    }

}
