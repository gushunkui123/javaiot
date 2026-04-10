package com.factorylink.domain.business.machine.query;

import cn.hutool.core.util.StrUtil;
import com.factorylink.common.core.page.AbstractPageQuery;
import com.factorylink.domain.business.machine.db.BizMachineEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 设备查询参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Schema(name = "设备查询参数")
public class MachineQuery extends AbstractPageQuery<BizMachineEntity> {

    @Schema(description = "设备名称")
    private String machineName;

    @Schema(description = "设备类型")
    private String deviceType;

    @Schema(description = "所属产线")
    private String productionLine;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Override
    public QueryWrapper<BizMachineEntity> addQueryCondition() {
        QueryWrapper<BizMachineEntity> queryWrapper = new QueryWrapper<BizMachineEntity>()
            .like(StrUtil.isNotEmpty(machineName), "machine_name", machineName)
            .eq(StrUtil.isNotEmpty(deviceType), "device_type", deviceType)
            .eq(StrUtil.isNotEmpty(productionLine), "production_line", productionLine)
            .eq(enabled != null, "enabled", enabled);

        if (StrUtil.isEmpty(this.getOrderColumn())) {
            this.setOrderColumn("createTime");
            this.setOrderDirection("descending");
        }
        this.setTimeRangeColumn("create_time");
        return queryWrapper;
    }

}
