package com.factorylink.domain.business.material.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 原料信息表 服务类
 * </p>
 *
 * @author Codex
 */
public interface BizMaterialService extends IService<BizMaterialEntity> {

    /**
     * 校验原料类型和原料名称是否重复
     *
     * @param materialId 原料ID
     * @param materialType 原料类型
     * @param materialName 原料名称
     * @return 是否重复
     */
    boolean isMaterialDuplicated(Long materialId, String materialType, String materialName);

}
