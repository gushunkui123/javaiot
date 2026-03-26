package com.factorylink.domain.business.material.dto;

import cn.hutool.core.bean.BeanUtil;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.common.audit.AuditableDTO;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@Data
@NoArgsConstructor
public class MaterialDTO implements AuditableDTO {

    public MaterialDTO(BizMaterialEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long materialId;

    private String materialType;

    private String materialName;

    private Integer weighingMethod;

    private Long creatorId;

    private String creatorName;

    private Date createTime;

    private Long updaterId;

    private String updaterName;

    private Date updateTime;

}
