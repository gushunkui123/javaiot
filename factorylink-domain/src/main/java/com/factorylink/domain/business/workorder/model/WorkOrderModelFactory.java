package com.factorylink.domain.business.workorder.model;

import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.factorylink.domain.business.workorder.db.BizWorkOrderService;
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
