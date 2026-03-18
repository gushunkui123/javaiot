package com.agileboot.domain.system.formula.db;

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
 * 配方信息表
 *
 * @author Codex
 */
@Getter
@Setter
@TableName("sys_formula")
@Schema(name = "SysFormulaEntity对象", description = "配方信息表")
public class SysFormulaEntity extends BaseEntity<SysFormulaEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配方ID")
    @TableId(value = "formula_id", type = IdType.AUTO)
    private Long formulaId;

    @Schema(description = "配方编号")
    @TableField("formula_code")
    private String formulaCode;

    @Schema(description = "配方名称")
    @TableField("formula_name")
    private String formulaName;

    @Override
    public Serializable pkVal() {
        return this.formulaId;
    }

}
