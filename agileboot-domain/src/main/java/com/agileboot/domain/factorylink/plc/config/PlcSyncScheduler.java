package com.agileboot.domain.factorylink.plc.config;

import com.agileboot.domain.factorylink.plc.service.PlcDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
@ConditionalOnProperty(name = "factory-link.sync.enabled", havingValue = "true", matchIfMissing = false)
public class PlcSyncScheduler {

    private final PlcDataSyncService plcDataSyncService;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * 每10秒同步一次外部PLC数据
     */
    @Scheduled(fixedRate = 10000)
    public void syncPlcDataPoints() {
        // 防止任务重叠执行
        if (!running.compareAndSet(false, true)) {
            log.debug("上一次同步任务仍在执行，跳过本次");
            return;
        }

        try {
            plcDataSyncService.syncPlcDataPoints();
        } catch (Exception e) {
            log.error("PLC数据同步异常: {}", e.getMessage());
        } finally {
            running.set(false);
        }
    }
}
