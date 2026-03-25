package com.factorylink.domain.business.notification.listener;

import com.factorylink.domain.business.notification.SseNotificationService;
import com.factorylink.domain.business.notification.event.FormulaModifiedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class FormulaModifiedEventListener {

    private final SseNotificationService sseNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFormulaModified(FormulaModifiedEvent event) {
        try {
            sseNotificationService.notifyFormulaModified(
                    event.getWorkOrderId(),
                    event.getFormulaId(),
                    event.getLevel());
        } catch (Exception e) {
            log.error("配方修改事件处理失败, workOrderId={}", event.getWorkOrderId(), e);
        }
    }
}
