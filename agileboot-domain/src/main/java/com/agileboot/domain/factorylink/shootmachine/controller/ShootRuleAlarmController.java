package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "模具阈值报警")
@RestController
@RequestMapping("/api/shoot/alarm")
@RequiredArgsConstructor
public class ShootRuleAlarmController {

    private final ShootRuleAlarmService shootRuleAlarmService;

    @Operation(summary = "查询所有未处理的报警")
    @GetMapping("/unhandled")
    public ResponseDTO<List<ShootRuleAlarmEntity>> listUnhandled(@RequestParam(required = false) Long machineId) {
        return ResponseDTO.ok(shootRuleAlarmService.listUnhandledWithRelation(machineId));
    }

    @Operation(summary = "根据站位号查询报警详情")
    @GetMapping("/detail")
    public ResponseDTO<Map<String, Object>> getDetailByStationNo(@RequestParam Integer stationNo) {
        return ResponseDTO.ok(shootRuleAlarmService.getDetailByStationNo(stationNo));
    }

    @Operation(summary = "根据站位号和字段名称批量处理报警")
    @PostMapping("/handleByStation")
    public ResponseDTO<Void> handleByStation(
            @RequestParam Integer stationNo,
            @RequestParam String fieldName,
            @RequestParam(required = false) String handleRemark) {
        shootRuleAlarmService.handleByStationNoAndField(stationNo, fieldName, handleRemark);
        return ResponseDTO.ok();
    }

    @Operation(summary = "统计概览")
    @GetMapping("/statistics/count")
    public ResponseDTO<Map<String, Long>> getStatisticsOverview(@RequestParam(required = false) Long machineId) {
        return ResponseDTO.ok(shootRuleAlarmService.getStatisticsOverview(machineId));
    }
}
