package com.factorylink.domain.business.notification.event;

import com.factorylink.infrastructure.sse.SseMessageLevel;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class FormulaModifiedEvent extends ApplicationEvent {

    private final Long workOrderId;
    private final Long formulaId;
    private final SseMessageLevel level;

    public FormulaModifiedEvent(Object source, Long workOrderId, Long formulaId, SseMessageLevel level) {
        super(source);
        this.workOrderId = workOrderId;
        this.formulaId = formulaId;
        this.level = level;
    }
}
