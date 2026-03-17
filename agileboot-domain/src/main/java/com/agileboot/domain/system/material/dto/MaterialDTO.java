package com.agileboot.domain.system.material.dto;

import cn.hutool.core.bean.BeanUtil;
import com.agileboot.domain.system.material.db.SysMaterialEntity;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@Data
@NoArgsConstructor
public class MaterialDTO {

    public MaterialDTO(SysMaterialEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long materialId;

    private String materialType;

    private String materialName;

    private Long creatorId;

    private Date createTime;

    private Long updaterId;

    private Date updateTime;

}
