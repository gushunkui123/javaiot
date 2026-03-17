package com.agileboot.domain.system.material.model;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.system.material.db.SysMaterialEntity;
import com.agileboot.domain.system.material.db.SysMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class MaterialModelFactory {

    private final SysMaterialService materialService;

    public MaterialModel loadById(Long materialId) {
        SysMaterialEntity byId = materialService.getById(materialId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, materialId, "原料");
        }
        return new MaterialModel(byId, materialService);
    }

    public MaterialModel create() {
        return new MaterialModel(materialService);
    }

}
