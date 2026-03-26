package com.factorylink.domain.business.material.db;

import com.factorylink.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 原料信息表
 * </p>
 *
 * @author Codex
 */
@Getter
@Setter
@TableName("biz_material")
@Schema(name = "BizMaterialEntity对象", description = "原料信息表")
public class BizMaterialEntity extends BaseEntity<BizMaterialEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "原料ID")
    @TableId(value = "material_id", type = IdType.AUTO)
    private Long materialId;

    @Schema(description = "原料类型")
    @TableField("material_type")
    private String materialType;

    @Schema(description = "原料名称")
    @TableField("material_name")
    private String materialName;

    @Schema(description = "称重方式（1-手动 2-自动）")
    @TableField("weighing_method")
    private Integer weighingMethod;

    @Override
    public Serializable pkVal() {
        return this.materialId;
    }

}
