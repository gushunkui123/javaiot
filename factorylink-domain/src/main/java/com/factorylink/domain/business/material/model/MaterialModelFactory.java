package com.factorylink.domain.business.material.model;

import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class MaterialModelFactory {

    private final BizMaterialService materialService;

    public MaterialModel loadById(Long materialId) {
        BizMaterialEntity byId = materialService.getById(materialId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, materialId, "原料");
        }
        return new MaterialModel(byId, materialService);
    }

    public MaterialModel create() {
        return new MaterialModel(materialService);
    }

}
