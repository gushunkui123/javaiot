package com.factorylink.domain.business.notification;

import com.factorylink.infrastructure.sse.SseConnectionManager;
import com.factorylink.infrastructure.sse.SseMessage;
import com.factorylink.infrastructure.sse.SseMessageLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseNotificationService {

    private final SseConnectionManager sseConnectionManager;

    public void notifyFormulaModified(Long workOrderId, Long formulaId, SseMessageLevel level) {
        String title = level == SseMessageLevel.ALERT
                ? "告警：生产中工单配方已修改"
                : "通知：工单配方已修改";
        String content = String.format("工单ID: %d, 配方ID: %d", workOrderId, formulaId);

        SseMessage message = SseMessage.builder()
                .level(level)
                .title(title)
                .content(content)
                .build();

        sseConnectionManager.broadcast(message);
        log.info("配方修改通知已推送, workOrderId={}, formulaId={}, level={}", workOrderId, formulaId, level);
    }
}
