package com.agileboot.domain.factorylink.plc.config;

import com.agileboot.domain.factorylink.plc.dto.RulePushResultDTO;
import com.agileboot.domain.factorylink.plc.service.AlarmRulePushService;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 安冬告警规则定时下发：周期性推送"当前在产排期"的全量规则快照。
 * <p>
 * 排期是否生效由时间区间决定（start_time &lt;= now &lt; end_time），没有对应的写操作，
 * 因此靠定时全量重推兜住：排期生效/失效的时间边界、漏推、失败重试。
 * 快照与上次成功推送一致时自动跳过，避免无变化时空推。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "factory-link.anton.enabled", havingValue = "true")
public class AlarmRulePushScheduler {

    private final AlarmRulePushService alarmRulePushService;

    /** 防重入：上轮未结束则跳过本轮 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${factory-link.anton.push-interval-ms:60000}")
    public void pushRules() {
        if (!running.compareAndSet(false, true)) {
            log.debug("[AntonPush] 上轮推送仍在执行，跳过本次");
            return;
        }
        try {
            RulePushResultDTO result = alarmRulePushService.pushAllRulesIfChanged();
            if (result.isSkipped()) {
                log.debug("[AntonPush] 快照未变化，跳过本轮");
            } else if (result.isSuccess()) {
                log.info("[AntonPush] 定时下发成功: 分组={}, 规则={}, 耗时={}ms",
                        result.getGroupCount(), result.getRuleCount(), result.getCostMs());
            } else {
                log.warn("[AntonPush] 定时下发未成功: {}", result.getError());
            }
        } catch (Exception e) {
            log.error("[AntonPush] 定时下发异常", e);
        } finally {
            running.set(false);
        }
    }
}
