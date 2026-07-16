package com.agileboot.domain.factorylink.plc.config;

import com.agileboot.domain.factorylink.plc.util.PlcDataSyncService;
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
    private final AtomicBoolean highFreqRunning = new AtomicBoolean(false);
    private final AtomicBoolean lowFreqRunning = new AtomicBoolean(false);

    /**
     * 高频同步：每3秒同步开模止/合模止
     */
    @Scheduled(fixedRate = 3000)
    public void syncHighFrequencyFields() {
        if (!highFreqRunning.compareAndSet(false, true)) {
            log.debug("高频同步任务仍在执行，跳过本次");
            return;
        }
        try {
            plcDataSyncService.syncHighFrequencyFields();
        } catch (Exception e) {
            log.error("高频PLC数据同步异常: {}", e.getMessage());
        } finally {
            highFreqRunning.set(false);
        }
    }

    /**
     * 低频同步：每10秒同步除开模止/合模止外的所有字段
     */
    @Scheduled(fixedRate = 10000)
    public void syncLowFrequencyFields() {
        if (!lowFreqRunning.compareAndSet(false, true)) {
            log.debug("低频同步任务仍在执行，跳过本次");
            return;
        }
        try {
            plcDataSyncService.syncLowFrequencyFields();
        } catch (Exception e) {
            log.error("低频PLC数据同步异常: {}", e.getMessage());
        } finally {
            lowFreqRunning.set(false);
        }
    }
}
