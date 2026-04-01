package com.factorylink.domain.business.material.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 原料信息表 服务实现类
 * </p>
 *
 * @author Codex
 */
@Service
public class BizMaterialServiceImpl extends ServiceImpl<BizMaterialMapper, BizMaterialEntity> implements BizMaterialService {

    @Override
    public boolean isMaterialCodeDuplicated(Long materialId, String materialCode) {
        QueryWrapper<BizMaterialEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(materialId != null, "material_id", materialId)
            .eq("material_code", materialCode);
        return baseMapper.exists(queryWrapper);
    }

    @Override
    public boolean isMaterialDuplicated(Long materialId, String materialType, String materialName) {
        QueryWrapper<BizMaterialEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(materialId != null, "material_id", materialId)
            .eq("material_type", materialType)
            .eq("material_name", materialName);
        return baseMapper.exists(queryWrapper);
    }

}
