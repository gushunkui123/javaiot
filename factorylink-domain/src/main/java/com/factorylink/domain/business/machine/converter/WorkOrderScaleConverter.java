package com.factorylink.domain.business.machine.converter;

import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.factorylink.infrastructure.machine.dto.request.ScaleWorkOrderRequest;
import java.text.SimpleDateFormat;
import org.springframework.stereotype.Component;

/**
 * 工单实体 → 设备 API 请求转换器
 */
@Component
public class WorkOrderScaleConverter {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public ScaleWorkOrderRequest toRequest(BizWorkOrderEntity workOrder) {
        ScaleWorkOrderRequest request = new ScaleWorkOrderRequest();
        request.setPlant(workOrder.getPlant() != null ? workOrder.getPlant() : "");
        request.setWorkOrderNo(workOrder.getWorkOrderNo());
        if (workOrder.getOrderDate() != null) {
            request.setWorkOrderDate(new SimpleDateFormat(DATE_FORMAT).format(workOrder.getOrderDate()));
        }
        request.setFormulaCode(workOrder.getFormulaCode());
        request.setBatch(workOrder.getOrderBatchNum());
        request.setLineNo(workOrder.getLineNo() != null ? workOrder.getLineNo() : "");
        return request;
    }

    public ScaleWorkOrderRequest toDeleteRequest(BizWorkOrderEntity workOrder) {
        ScaleWorkOrderRequest request = new ScaleWorkOrderRequest();
        request.setPlant(workOrder.getPlant() != null ? workOrder.getPlant() : "");
        request.setWorkOrderNo(workOrder.getWorkOrderNo());
        return request;
    }
}
