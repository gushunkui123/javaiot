package com.agileboot.domain.business.workorder.query;

import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.page.AbstractPageQuery;
import com.agileboot.domain.business.workorder.db.BizWorkOrderEntity;
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
@Schema(name = "工单查询参数")
public class WorkOrderQuery extends AbstractPageQuery<BizWorkOrderEntity> {

    @Schema(description = "工单编号")
    private String workOrderNo;

    @Schema(description = "配方编号")
    private String formulaCode;

    @Schema(description = "设备编号")
    private Integer machineId;

    @Schema(description = "工单状态: 1-未生产, 2-生产中, 3-已完工, 4-已取消")
    private Integer orderState;

    @Override
    public QueryWrapper<BizWorkOrderEntity> addQueryCondition() {
        QueryWrapper<BizWorkOrderEntity> queryWrapper = new QueryWrapper<BizWorkOrderEntity>()
            .like(StrUtil.isNotEmpty(workOrderNo), "work_order_no", workOrderNo)
            .eq(StrUtil.isNotEmpty(formulaCode), "formula_code", formulaCode)
            .eq(machineId != null, "machine_id", machineId)
            .eq(orderState != null, "order_state", orderState);

        if (StrUtil.isEmpty(this.getOrderColumn())) {
            this.setOrderColumn("createTime");
            this.setOrderDirection("descending");
        }
        this.setTimeRangeColumn("create_time");
        return queryWrapper;
    }

}
