package com.factorylink.domain.business.formula.db;

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
 * 配方信息表
 *
 * @author Codex
 */
@Getter
@Setter
@TableName("biz_formula")
@Schema(name = "BizFormulaEntity对象", description = "配方信息表")
public class BizFormulaEntity extends BaseEntity<BizFormulaEntity> {

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

    @Schema(description = "日期")
    @TableField("formula_date")
    private String formulaDate;

    @Schema(description = "模具代号")
    @TableField("mold_code")
    private String moldCode;

    @Schema(description = "批次")
    @TableField("batch")
    private String batch;

    @Schema(description = "手数")
    @TableField("batch_count")
    private String batchCount;

    @Schema(description = "生产订单号")
    @TableField("order_no")
    private String orderNo;

    @Override
    public Serializable pkVal() {
        return this.formulaId;
    }

}
