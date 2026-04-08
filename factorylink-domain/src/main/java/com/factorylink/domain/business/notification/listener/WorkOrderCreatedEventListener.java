package com.factorylink.domain.business.notification.listener;

import com.factorylink.domain.business.notification.event.WorkOrderCreatedEvent;
import com.factorylink.infrastructure.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 工单创建事件监听器 - 发送邮件通知
 *
 * @author Codex
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WorkOrderCreatedEventListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkOrderCreated(WorkOrderCreatedEvent event) {
        try {
            String subject = "工单创建通知 - " + event.getWorkOrderNo();
            String content = String.format(
                "您好，以下工单已创建成功：\n\n"
                + "工单编号：%s\n"
                + "产线编号：%s\n"
                + "模具代号：%s\n"
                + "型体颜色：%s\n"
                + "计划批次数：%d\n\n"
                + "请及时关注工单进度。",
                event.getWorkOrderNo(),
                event.getLineNo(),
                event.getMoldCode(),
                event.getModelColor(),
                event.getOrderBatchNum()
            );
            emailService.sendSimpleMail(event.getEmail(), subject, content);
        } catch (Exception e) {
            log.error("工单创建邮件通知发送失败, workOrderNo={}, email={}", event.getWorkOrderNo(), event.getEmail(), e);
        }
    }
}
