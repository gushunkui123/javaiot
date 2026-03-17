package com.agileboot.domain.system.material.db;

import com.agileboot.common.core.base.BaseEntity;
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
@TableName("sys_material")
@Schema(name = "SysMaterialEntity对象", description = "原料信息表")
public class SysMaterialEntity extends BaseEntity<SysMaterialEntity> {

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

    @Override
    public Serializable pkVal() {
        return this.materialId;
    }

}
