package com.factorylink.domain.business.machine.converter;

import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.infrastructure.machine.dto.request.ScalePartsRequest;
import org.springframework.stereotype.Component;

/**
 * 原料实体 → 设备 API 请求转换器
 */
@Component
public class MaterialScaleConverter {

    public ScalePartsRequest toRequest(BizMaterialEntity material) {
        ScalePartsRequest request = new ScalePartsRequest();
        request.setPlant("");
        request.setPartNo(material.getMaterialCode());
        request.setPartName(material.getMaterialName());
        request.setPartClass(convertPartClass(material.getMaterialType()));
        return request;
    }

    public ScalePartsRequest toDeleteRequest(BizMaterialEntity material) {
        ScalePartsRequest request = new ScalePartsRequest();
        request.setPlant("");
        request.setPartNo(material.getMaterialCode());
        return request;
    }

    private String convertPartClass(String materialType) {
        if (materialType == null) {
            return "";
        }
        if ("主料".equals(materialType) || "颗粒".equals(materialType)) {
            return "5";
        }
        return "";
    }
}
