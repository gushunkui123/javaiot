package com.factorylink.domain.business.notification.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 工单创建事件
 *
 * @author Codex
 */
@Getter
public class WorkOrderCreatedEvent extends ApplicationEvent {

    private final String workOrderNo;
    private final String lineNo;
    private final String moldCode;
    private final String modelColor;
    private final Integer orderBatchNum;
    private final String email;

    public WorkOrderCreatedEvent(Object source, String workOrderNo, String lineNo,
            String moldCode, String modelColor, Integer orderBatchNum, String email) {
        super(source);
        this.workOrderNo = workOrderNo;
        this.lineNo = lineNo;
        this.moldCode = moldCode;
        this.modelColor = modelColor;
        this.orderBatchNum = orderBatchNum;
        this.email = email;
    }
}
