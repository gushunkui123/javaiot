package com.agileboot.admin.controller.business;

import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.business.machine.ScaleQueryService;
import com.agileboot.infrastructure.machine.dto.ScaleApiResponse;
import com.agileboot.infrastructure.machine.dto.response.AlarmLogData;
import com.agileboot.infrastructure.machine.dto.response.MaterialConsumptionData;
import com.agileboot.infrastructure.machine.dto.response.ProcessData;
import com.agileboot.infrastructure.machine.dto.response.WeighingRecordData;
import com.agileboot.infrastructure.machine.dto.response.WorkOrderData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 磅秤设备实时数据查询
 */
@Tag(name = "磅秤设备查询API", description = "实时查询主磅/微量设备数据")
@RestController
@RequestMapping("/business/scale")
@Validated
@RequiredArgsConstructor
public class BizScaleController extends BaseController {

    private final ScaleQueryService scaleQueryService;

    // ======================== 主磅查询 ========================

    @Operation(summary = "主磅-工单数据")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/main/workOrder")
    public ResponseDTO<ScaleApiResponse<WorkOrderData>> mainWorkOrder(
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String formulaCode,
            @RequestParam(required = false) String orderDateFrom,
            @RequestParam(required = false) String orderDateTo,
            @RequestParam(required = false) String orderState) {
        return ResponseDTO.ok(scaleQueryService.queryMainScaleWorkOrder(
            workOrderNo, formulaCode, orderDateFrom, orderDateTo, orderState));
    }

    @Operation(summary = "主磅-称量记录")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/main/weighing")
    public ResponseDTO<ScaleApiResponse<WeighingRecordData>> mainWeighing(
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String formulaCode,
            @RequestParam String weighTimeFrom,
            @RequestParam String weighTimeTo) {
        return ResponseDTO.ok(scaleQueryService.queryMainScaleWeighingRecord(
            workOrderNo, formulaCode, weighTimeFrom, weighTimeTo));
    }

    @Operation(summary = "主磅-报警记录")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/main/alarm")
    public ResponseDTO<ScaleApiResponse<AlarmLogData>> mainAlarm(
            @RequestParam(required = false) String alarmGroup,
            @RequestParam String alarmTimeFrom,
            @RequestParam String alarmTimeTo) {
        return ResponseDTO.ok(scaleQueryService.queryMainScaleAlarmLog(
            alarmGroup, alarmTimeFrom, alarmTimeTo));
    }

    @Operation(summary = "主磅-原料耗用")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/main/materialConsumption")
    public ResponseDTO<ScaleApiResponse<MaterialConsumptionData>> mainMaterialConsumption(
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String materialNo,
            @RequestParam String consTimeFrom,
            @RequestParam String consTimeTo) {
        return ResponseDTO.ok(scaleQueryService.queryMainScaleMaterialConsumption(
            workOrderNo, materialNo, consTimeFrom, consTimeTo));
    }

    @Operation(summary = "主磅-密炼机工艺数据")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/main/processData")
    public ResponseDTO<ScaleApiResponse<ProcessData>> mainProcessData(
            @RequestParam String workOrderNo,
            @RequestParam Integer batch) {
        return ResponseDTO.ok(scaleQueryService.queryMainScaleProcessData(workOrderNo, batch));
    }

    // ======================== 微量查询 ========================

    @Operation(summary = "微量-工单数据")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/micro/workOrder")
    public ResponseDTO<ScaleApiResponse<WorkOrderData>> microWorkOrder(
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String formulaCode,
            @RequestParam(required = false) String orderDateFrom,
            @RequestParam(required = false) String orderDateTo,
            @RequestParam(required = false) String orderState) {
        return ResponseDTO.ok(scaleQueryService.queryMicroScaleWorkOrder(
            workOrderNo, formulaCode, orderDateFrom, orderDateTo, orderState));
    }

    @Operation(summary = "微量-称量记录")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/micro/weighing")
    public ResponseDTO<ScaleApiResponse<WeighingRecordData>> microWeighing(
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String formulaCode,
            @RequestParam String weighTimeFrom,
            @RequestParam String weighTimeTo) {
        return ResponseDTO.ok(scaleQueryService.queryMicroScaleWeighingRecord(
            workOrderNo, formulaCode, weighTimeFrom, weighTimeTo));
    }

    @Operation(summary = "微量-报警记录")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/micro/alarm")
    public ResponseDTO<ScaleApiResponse<AlarmLogData>> microAlarm(
            @RequestParam(required = false) String alarmGroup,
            @RequestParam String alarmTimeFrom,
            @RequestParam String alarmTimeTo) {
        return ResponseDTO.ok(scaleQueryService.queryMicroScaleAlarmLog(
            alarmGroup, alarmTimeFrom, alarmTimeTo));
    }

    @Operation(summary = "微量-原料耗用")
    @PreAuthorize("@permission.has('business:scale:query')")
    @GetMapping("/micro/materialConsumption")
    public ResponseDTO<ScaleApiResponse<MaterialConsumptionData>> microMaterialConsumption(
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String materialNo,
            @RequestParam String consTimeFrom,
            @RequestParam String consTimeTo) {
        return ResponseDTO.ok(scaleQueryService.queryMicroScaleMaterialConsumption(
            workOrderNo, materialNo, consTimeFrom, consTimeTo));
    }
}
