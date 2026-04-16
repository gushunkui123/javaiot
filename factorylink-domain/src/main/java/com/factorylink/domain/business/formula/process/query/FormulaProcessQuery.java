package com.factorylink.domain.business.formula.process.query;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.factorylink.common.core.page.AbstractPageQuery;
import com.factorylink.domain.business.formula.process.db.BizFormulaProcessEntity;
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
@Schema(name = "工艺查询参数")
public class FormulaProcessQuery extends AbstractPageQuery<BizFormulaProcessEntity> {

    @Schema(description = "工艺编号")
    private String processCode;

    @Schema(description = "工艺名称")
    private String processName;

    @Override
    public QueryWrapper<BizFormulaProcessEntity> addQueryCondition() {
        QueryWrapper<BizFormulaProcessEntity> queryWrapper = new QueryWrapper<BizFormulaProcessEntity>()
            .like(StrUtil.isNotEmpty(processCode), "process_code", processCode)
            .like(StrUtil.isNotEmpty(processName), "process_name", processName);

        if (StrUtil.isEmpty(this.getOrderColumn())) {
            this.setOrderColumn("createTime");
            this.setOrderDirection("descending");
        }
        this.setTimeRangeColumn("create_time");
        return queryWrapper;
    }

}
