package com.agileboot.domain.system.material;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.system.material.command.AddMaterialCommand;
import com.agileboot.domain.system.material.command.UpdateMaterialCommand;
import com.agileboot.domain.system.material.db.SysMaterialEntity;
import com.agileboot.domain.system.material.db.SysMaterialService;
import com.agileboot.domain.system.material.dto.MaterialDTO;
import com.agileboot.domain.system.material.model.MaterialModel;
import com.agileboot.domain.system.material.model.MaterialModelFactory;
import com.agileboot.domain.system.material.query.MaterialQuery;
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

    private final SysMaterialService materialService;

    public PageDTO<MaterialDTO> getMaterialList(MaterialQuery query) {
        Page<SysMaterialEntity> page = materialService.page(query.toPage(), query.toQueryWrapper());
        List<MaterialDTO> records = page.getRecords().stream().map(MaterialDTO::new).toList();
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
