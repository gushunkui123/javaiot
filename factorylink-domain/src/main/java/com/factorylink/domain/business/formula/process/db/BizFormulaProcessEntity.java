package com.factorylink.domain.business.formula.process.db;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.factorylink.common.core.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 工艺信息表
 *
 * @author Codex
 */
@Getter
@Setter
@TableName("biz_formula_process")
@Schema(name = "BizFormulaProcessEntity对象", description = "工艺信息表")
public class BizFormulaProcessEntity extends BaseEntity<BizFormulaProcessEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "工艺ID")
    @TableId(value = "process_id", type = IdType.AUTO)
    private Long processId;

    @Schema(description = "工艺编号")
    @TableField("process_code")
    private String processCode;

    @Schema(description = "工艺名称")
    @TableField("process_name")
    private String processName;

    @Override
    public Serializable pkVal() {
        return this.processId;
    }

}
