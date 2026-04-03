package com.factorylink.domain.business.formula.dto;

import lombok.Data;

/**
 * 配方导入时缺失的原料信息
 */
@Data
public class MissingMaterialDTO {

    /** 原料编号（来自Excel） */
    private String materialCode;

    /** 转换后的原料类型（主料/颗粒/粉末/色粒） */
    private String materialType;

    /** Excel中的原始分类名称 */
    private String category;
}
