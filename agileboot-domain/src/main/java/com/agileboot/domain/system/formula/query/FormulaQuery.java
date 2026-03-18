package com.agileboot.domain.system.formula.query;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.AbstractPageQuery;
import com.agileboot.domain.system.formula.db.SysFormulaEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Schema(name = "配方查询参数")
public class FormulaQuery extends AbstractPageQuery<SysFormulaEntity> {

    @Schema(description = "配方编号")
    private String formulaCode;

    @Schema(description = "配方名称")
    private String formulaName;

    @Override
    public QueryWrapper<SysFormulaEntity> addQueryCondition() {
        QueryWrapper<SysFormulaEntity> queryWrapper = new QueryWrapper<SysFormulaEntity>()
            .like(StrUtil.isNotEmpty(formulaCode), "formula_code", formulaCode)
            .like(StrUtil.isNotEmpty(formulaName), "formula_name", formulaName);

        if (StrUtil.isEmpty(this.getOrderColumn())) {
            this.setOrderColumn("createTime");
            this.setOrderDirection("descending");
        }
        this.setTimeRangeColumn("create_time");
        return queryWrapper;
    }

}
