package com.factorylink.domain.business.material.query;

import cn.hutool.core.util.StrUtil;
import com.factorylink.common.core.page.AbstractPageQuery;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
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
@Schema(name = "原料查询参数")
public class MaterialQuery extends AbstractPageQuery<BizMaterialEntity> {

    @Schema(description = "原料类型")
    private String materialType;

    @Schema(description = "原料名称")
    private String materialName;

    @Schema(description = "称重方式（1-手动 2-自动）")
    private Integer weighingMethod;

    @Override
    public QueryWrapper<BizMaterialEntity> addQueryCondition() {
        QueryWrapper<BizMaterialEntity> queryWrapper = new QueryWrapper<BizMaterialEntity>()
            .eq(StrUtil.isNotEmpty(materialType), "material_type", materialType)
            .like(StrUtil.isNotEmpty(materialName), "material_name", materialName)
            .eq(weighingMethod != null, "weighing_method", weighingMethod);

        if (StrUtil.isEmpty(this.getOrderColumn())) {
            this.setOrderColumn("createTime");
            this.setOrderDirection("descending");
        }
        this.setTimeRangeColumn("create_time");
        return queryWrapper;
    }

}
