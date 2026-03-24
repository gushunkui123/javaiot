package com.factorylink.domain.business.material.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.material.command.AddMaterialCommand;
import com.factorylink.domain.business.material.command.UpdateMaterialCommand;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@NoArgsConstructor
public class MaterialModel extends BizMaterialEntity {

    private BizMaterialService materialService;

    public MaterialModel(BizMaterialService materialService) {
        this.materialService = materialService;
    }

    public MaterialModel(BizMaterialEntity entity, BizMaterialService materialService) {
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
