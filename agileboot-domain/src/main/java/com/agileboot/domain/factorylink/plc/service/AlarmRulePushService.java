package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.dto.MoldRulePushDTO;
import com.agileboot.domain.factorylink.plc.dto.RulePushResultDTO;
import com.agileboot.domain.factorylink.plc.util.AntonRulePushClient;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 安冬告警规则下发编排：构建"当前在产排期"的全量规则快照并推送。
 * <p>
 * 手动触发走 {@link #pushAllRules()}（忽略快照对比，总是推送）；
 * 定时触发走 {@link #pushAllRulesIfChanged()}（快照与上次成功推送一致时跳过，避免无变化时空推）。
 * <p>
 * 快照始终为全量：安冬侧按"收到的数组"与自身状态对账，缺失的组会被自动清理，
 * 因此空快照（当前无在产排期）同样需要推送，用于清空安冬侧不再需要的规则。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmRulePushService {

    private final ShootRuleAlarmService shootRuleAlarmService;
    private final AntonRulePushClient antonRulePushClient;

    /** 上次成功推送的快照，用于跳过无变化的重复推送 */
    private final AtomicReference<List<MoldRulePushDTO>> lastPushedSnapshot = new AtomicReference<>();

    /** 手动触发：总是推送 */
    public RulePushResultDTO pushAllRules() {
        return doPush(false);
    }

    /** 定时触发：快照无变化则跳过 */
    public RulePushResultDTO pushAllRulesIfChanged() {
        return doPush(true);
    }

    private RulePushResultDTO doPush(boolean skipIfUnchanged) {
        // null = 全量启用机台
        List<MoldRulePushDTO> payload = shootRuleAlarmService.buildRulePushPayload(null);

        RulePushResultDTO result = new RulePushResultDTO();
        result.setTargetUrl(antonRulePushClient.getTargetUrl());
        result.setPayload(payload);
        result.setGroupCount(payload.size());
        result.setRuleCount(payload.stream()
                .mapToInt(group -> group.getRules() == null ? 0 : group.getRules().size())
                .sum());

        if (!antonRulePushClient.isEnabled()) {
            result.setError("安冬规则下发未启用（factory-link.anton.enabled=false）");
            return result;
        }
        if (skipIfUnchanged && payload.equals(lastPushedSnapshot.get())) {
            result.setSkipped(true);
            log.debug("[AntonPush] 快照未变化，跳过推送: 分组={}, 规则={}", result.getGroupCount(), result.getRuleCount());
            return result;
        }

        // 空快照是正常状态（当前无在产排期）：照常推送空数组，让安冬按全量对账清掉不再需要的规则。
        // 若因误操作或数据异常导致清空，下一轮（默认 60s）快照恢复后会立即重新推回，可自愈。
        if (payload.isEmpty()) {
            log.warn("[AntonPush] 快照为空（当前无在产排期），将推送空数组以清空安冬侧规则");
        }

        long start = System.currentTimeMillis();
        try {
            result.setResponse(antonRulePushClient.pushRules(payload));
            result.setSuccess(true);
            // 仅记录成功推送的快照，失败时保持上一次基线以便下轮重试
            lastPushedSnapshot.set(payload);
        } catch (Exception e) {
            result.setError(e.getMessage());
        } finally {
            result.setCostMs(System.currentTimeMillis() - start);
        }
        return result;
    }
}
