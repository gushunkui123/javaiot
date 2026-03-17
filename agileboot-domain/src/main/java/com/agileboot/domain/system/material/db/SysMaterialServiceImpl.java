package com.agileboot.domain.system.material.db;

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
public class SysMaterialServiceImpl extends ServiceImpl<SysMaterialMapper, SysMaterialEntity> implements SysMaterialService {

    @Override
    public boolean isMaterialDuplicated(Long materialId, String materialType, String materialName) {
        QueryWrapper<SysMaterialEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(materialId != null, "material_id", materialId)
            .eq("material_type", materialType)
            .eq("material_name", materialName);
        return baseMapper.exists(queryWrapper);
    }

}
