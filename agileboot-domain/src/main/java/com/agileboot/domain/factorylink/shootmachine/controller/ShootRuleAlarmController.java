package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Internal;
import com.agileboot.domain.factorylink.shootmachine.dto.AlarmPageResponse;
import com.agileboot.domain.factorylink.shootmachine.dto.BatchHandleAlarmDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import com.agileboot.domain.factorylink.shootmachine.util.AlarmExcelExportUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

    @Operation(summary = "查询未处理报警的机器/站位聚合汇总（首屏看板用）")
    @GetMapping("/unhandled/summary")
    public ResponseDTO<List<Map<String, Object>>> listUnhandledSummary(
            @RequestParam(required = false) Long machineId) {
        return ResponseDTO.ok(shootRuleAlarmService.listUnhandledSummary(machineId));
    }

    @Operation(summary = "分页查询未处理报警明细（按机器+站位过滤）")
    @GetMapping("/unhandled/page")
    public ResponseDTO<AlarmPageResponse> listUnhandledPage(
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) Long stationId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ResponseDTO.ok(shootRuleAlarmService.listUnhandledPaged(machineId, stationId, page, pageSize));
    }

    @Operation(summary = "根据站位号查询报警详情")
    @GetMapping("/detail")
    public ResponseDTO<Map<String, Object>> getDetailByStationNo(@RequestParam Integer stationNo) {
        return ResponseDTO.ok(shootRuleAlarmService.getDetailByStationNo(stationNo));
    }

    @Operation(summary = "根据报警ID列表批量处理报警")
    @PostMapping("/handleByStation")
    public ResponseDTO<Void> handleByStation(@RequestBody BatchHandleAlarmDTO dto) {
        shootRuleAlarmService.handleByStationIdAndField(dto.getIds(), dto.getHandleRemark());
        return ResponseDTO.ok();
    }

    @Operation(summary = "统计概览")
    @GetMapping("/statistics/count")
    public ResponseDTO<Map<String, Long>> getStatisticsOverview(@RequestParam(required = false) Long machineId) {
        return ResponseDTO.ok(shootRuleAlarmService.getStatisticsOverview(machineId));
    }

    @Operation(summary = "导出报警数据到Excel")
    @GetMapping("/export")
    public void exportAlarms(@RequestParam(required = false) Long machineId,
                             @RequestParam(defaultValue = "15") Integer days,
                             HttpServletResponse response) {
        try {
            AlarmExcelExportUtil.export(response, shootRuleAlarmService.listAllForExport(machineId, days));
        } catch (Exception e) {
            throw new ApiException(e, Internal.EXCEL_PROCESS_ERROR, e.getMessage());
        }
    }
}
