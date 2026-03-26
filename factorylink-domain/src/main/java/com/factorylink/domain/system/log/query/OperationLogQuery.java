package com.factorylink.domain.system.log.query;

import cn.hutool.core.util.StrUtil;
import com.factorylink.common.core.page.AbstractPageQuery;
import com.factorylink.domain.system.log.db.SysOperationLogEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author valarchie
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OperationLogQuery extends AbstractPageQuery<SysOperationLogEntity> {

    @Parameter(description = "业务类型（0=其他, 1=添加, 2=修改, 3=删除, 4=授权, 5=导出, 6=导入, 7=强退, 8=清空, 9=查询）")
    private String businessType;
    @Parameter(description = "操作状态（1=正常, 0=异常）")
    private String status;
    private String username;
    private String requestModule;

    @Override
    public QueryWrapper<SysOperationLogEntity> addQueryCondition() {
        QueryWrapper<SysOperationLogEntity> queryWrapper = new QueryWrapper<SysOperationLogEntity>()
            .like(businessType!=null, "business_type", businessType)
            .eq(status != null, "status", status)
            .like(StrUtil.isNotEmpty(username), "username", username)
            .like(StrUtil.isNotEmpty(requestModule), "request_module", requestModule);

        this.timeRangeColumn = "operation_time";

        return queryWrapper;
    }
}
