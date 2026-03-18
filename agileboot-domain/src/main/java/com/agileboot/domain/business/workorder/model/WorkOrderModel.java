package com.agileboot.domain.business.workorder.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.business.workorder.command.AddWorkOrderCommand;
import com.agileboot.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.agileboot.domain.business.workorder.db.BizWorkOrderEntity;
import com.agileboot.domain.business.workorder.db.BizWorkOrderService;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@NoArgsConstructor
public class WorkOrderModel extends BizWorkOrderEntity {

    private BizWorkOrderService workOrderService;

    public WorkOrderModel(BizWorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    public WorkOrderModel(BizWorkOrderEntity entity, BizWorkOrderService workOrderService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.workOrderService = workOrderService;
    }

    public void loadFromAddCommand(AddWorkOrderCommand addCommand) {
        if (addCommand != null) {
            BeanUtil.copyProperties(addCommand, this, "workOrderId");
            setWorkOrderNo(StrUtil.trim(getWorkOrderNo()));
            setFormulaCode(StrUtil.trim(getFormulaCode()));
        }
    }

    public void loadFromUpdateCommand(UpdateWorkOrderCommand updateCommand) {
        if (updateCommand != null) {
            loadFromAddCommand(updateCommand);
        }
    }

    public void checkWorkOrderNoUnique() {
        if (workOrderService.isWorkOrderNoDuplicated(getWorkOrderId(), getWorkOrderNo())) {
            throw new ApiException(Business.WORK_ORDER_NO_IS_NOT_UNIQUE, getWorkOrderNo());
        }
    }

}
