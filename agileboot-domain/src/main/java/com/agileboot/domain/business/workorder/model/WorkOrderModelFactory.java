package com.agileboot.domain.business.workorder.model;

import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.business.workorder.db.BizWorkOrderEntity;
import com.agileboot.domain.business.workorder.db.BizWorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class WorkOrderModelFactory {

    private final BizWorkOrderService workOrderService;

    public WorkOrderModel loadById(Long workOrderId) {
        BizWorkOrderEntity byId = workOrderService.getById(workOrderId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, workOrderId, "工单");
        }
        return new WorkOrderModel(byId, workOrderService);
    }

    public WorkOrderModel create() {
        return new WorkOrderModel(workOrderService);
    }

}
