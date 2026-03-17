package com.agileboot.domain.system.material.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.system.material.command.AddMaterialCommand;
import com.agileboot.domain.system.material.command.UpdateMaterialCommand;
import com.agileboot.domain.system.material.db.SysMaterialEntity;
import com.agileboot.domain.system.material.db.SysMaterialService;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@NoArgsConstructor
public class MaterialModel extends SysMaterialEntity {

    private SysMaterialService materialService;

    public MaterialModel(SysMaterialService materialService) {
        this.materialService = materialService;
    }

    public MaterialModel(SysMaterialEntity entity, SysMaterialService materialService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.materialService = materialService;
    }

    public void loadFromAddCommand(AddMaterialCommand addCommand) {
        if (addCommand != null) {
            BeanUtil.copyProperties(addCommand, this, "materialId");
            setMaterialType(StrUtil.trim(getMaterialType()));
            setMaterialName(StrUtil.trim(getMaterialName()));
        }
    }

    public void loadFromUpdateCommand(UpdateMaterialCommand updateCommand) {
        if (updateCommand != null) {
            loadFromAddCommand(updateCommand);
        }
    }

    public void checkMaterialUnique() {
        if (materialService.isMaterialDuplicated(getMaterialId(), getMaterialType(), getMaterialName())) {
            throw new ApiException(Business.MATERIAL_TYPE_AND_NAME_IS_NOT_UNIQUE, getMaterialType(), getMaterialName());
        }
    }

}
