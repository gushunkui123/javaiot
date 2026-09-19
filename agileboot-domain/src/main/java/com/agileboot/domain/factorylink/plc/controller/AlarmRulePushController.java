package com.agileboot.domain.factorylink.plc.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.plc.dto.MoldRulePushDTO;
import com.agileboot.domain.factorylink.plc.dto.RulePushResultDTO;
import com.agileboot.domain.factorylink.plc.service.AlarmRulePushService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 告警规则下发接口。
 * <p>
 * GET 仅预览报文（不实际下发）；POST 触发全量快照推送到安冬硬件平台。
 * 定时下发见 {@link com.agileboot.domain.factorylink.plc.config.AlarmRulePushScheduler}。
 */
@Tag(name = "FactoryLink 告警规则下发")
@RestController
@RequestMapping("/factorylink/alarm-rule")
@RequiredArgsConstructor
public class AlarmRulePushController {

    private final ShootRuleAlarmService shootRuleAlarmService;
    private final AlarmRulePushService alarmRulePushService;

    @Operation(summary = "预览下发给第三方的告警规则报文（不实际推送）")
    @GetMapping("/teak")
    public ResponseDTO<List<MoldRulePushDTO>> buildRulePushPayload(
            @Parameter(description = "机台ID（可选，不传=全部启用机台，五号机为11，三号机为13）")
            @RequestParam(value = "machineId", required = false)
            Long machineId) {
        return ResponseDTO.ok(shootRuleAlarmService.buildRulePushPayload(machineId));
    }

    @Operation(summary = "触发推送告警规则到安冬硬件平台（全量启用机台，无需参数）")
    @PostMapping("/push")
    public ResponseDTO<RulePushResultDTO> pushRulePayload() {
        return ResponseDTO.ok(alarmRulePushService.pushAllRules());
    }
}
