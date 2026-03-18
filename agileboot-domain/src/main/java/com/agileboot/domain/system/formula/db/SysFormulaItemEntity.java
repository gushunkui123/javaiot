package com.agileboot.domain.system.formula.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 配方明细表
 *
 * @author Codex
 */
@Getter
@Setter
@TableName("sys_formula_item")
@Schema(name = "SysFormulaItemEntity对象", description = "配方明细表")
public class SysFormulaItemEntity extends Model<SysFormulaItemEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "明细ID")
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    @Schema(description = "配方ID")
    @TableField("formula_id")
    private Long formulaId;

    @Schema(description = "原料ID")
    @TableField("material_id")
    private Long materialId;

    @Schema(description = "原料重量(kg)")
    @TableField("material_weight")
    private BigDecimal materialWeight;

    @Schema(description = "下料段序")
    @TableField("step_no")
    private Integer stepNo;

    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;

    @Override
    public Serializable pkVal() {
        return this.itemId;
    }

}
