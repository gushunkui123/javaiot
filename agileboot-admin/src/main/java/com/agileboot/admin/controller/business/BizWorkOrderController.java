package com.agileboot.admin.controller.business;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.enums.common.BusinessTypeEnum;
import com.agileboot.common.utils.poi.CustomExcelUtil;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.business.workorder.WorkOrderApplicationService;
import com.agileboot.domain.business.workorder.command.AddWorkOrderCommand;
import com.agileboot.domain.business.workorder.command.AssignFormulaCommand;
import com.agileboot.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.agileboot.domain.business.workorder.dto.WorkOrderDTO;
import com.agileboot.domain.business.workorder.query.WorkOrderQuery;
import com.agileboot.domain.business.machine.ScaleSyncService;
import com.agileboot.domain.business.machine.ScaleSyncService.OperationType;
import com.agileboot.domain.business.machine.dto.SyncResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工单信息操作处理
 *
 * @author Codex
 */
@Tag(name = "工单API", description = "工单相关的增删查改")
@RestController
@RequestMapping("/business/workOrder")
@Validated
@RequiredArgsConstructor
public class BizWorkOrderController extends BaseController {

    private final WorkOrderApplicationService workOrderApplicationService;

    private final ScaleSyncService scaleSyncService;

    @Operation(summary = "工单列表")
    @PreAuthorize("@permission.has('business:workOrder:list')")
    @GetMapping("/list")
    public ResponseDTO<PageDTO<WorkOrderDTO>> list(WorkOrderQuery query) {
        PageDTO<WorkOrderDTO> pageDTO = workOrderApplicationService.getWorkOrderList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "工单列表导出")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.EXPORT)
    @PreAuthorize("@permission.has('business:workOrder:export')")
    @GetMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public void export(HttpServletResponse response, WorkOrderQuery query) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=workorder_export.xlsx");
        List<WorkOrderDTO> all = workOrderApplicationService.getWorkOrderListAll(query);
        CustomExcelUtil.writeToResponse(all, WorkOrderDTO.class, response);
    }

    @Operation(summary = "工单详情")
    @PreAuthorize("@permission.has('business:workOrder:query')")
    @GetMapping("/{workOrderId}")
    public ResponseDTO<WorkOrderDTO> getInfo(@PathVariable Long workOrderId) {
        WorkOrderDTO workOrderDTO = workOrderApplicationService.getWorkOrderInfo(workOrderId);
        return ResponseDTO.ok(workOrderDTO);
    }

    @Operation(summary = "添加工单")
    @PreAuthorize("@permission.has('business:workOrder:add')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Validated @RequestBody AddWorkOrderCommand addCommand) {
        workOrderApplicationService.addWorkOrder(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改工单")
    @PreAuthorize("@permission.has('business:workOrder:edit')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> edit(@Validated @RequestBody UpdateWorkOrderCommand updateCommand) {
        workOrderApplicationService.updateWorkOrder(updateCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除工单")
    @PreAuthorize("@permission.has('business:workOrder:remove')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping
    public ResponseDTO<Void> remove(@RequestParam @NotNull @NotEmpty List<Long> ids) {
        workOrderApplicationService.deleteWorkOrder(new BulkOperationCommand<>(ids));
        return ResponseDTO.ok();
    }

    @Operation(summary = "下发工单到设备")
    @PreAuthorize("@permission.has('business:workOrder:sync')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/{workOrderId}/sync")
    public ResponseDTO<SyncResultDTO> syncWorkOrder(@PathVariable Long workOrderId,
            @RequestParam(defaultValue = "ADD") OperationType operationType) {
        SyncResultDTO result = scaleSyncService.syncWorkOrder(workOrderId, operationType);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "查询待派工工单", description = "查询未删除且流程状态为1(未派工)的工单列表")
    @PreAuthorize("@permission.has('business:workOrder:pending')")
    @GetMapping("/pending")
    public ResponseDTO<List<WorkOrderDTO>> pendingList() {
        List<WorkOrderDTO> list = workOrderApplicationService.getPendingWorkOrders();
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "分配配方", description = "为工单分配配方ID和配方编号，流程状态变为2(已派工)")
    @PreAuthorize("@permission.has('business:workOrder:assignFormula')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/assignFormula")
    public ResponseDTO<Void> assignFormula(@Validated @RequestBody AssignFormulaCommand command) {
        workOrderApplicationService.assignFormula(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询已派工工单", description = "查询未删除且流程状态为2(已派工)的工单列表")
    @PreAuthorize("@permission.has('business:workOrder:dispatched')")
    @GetMapping("/dispatched")
    public ResponseDTO<List<WorkOrderDTO>> dispatchedList() {
        List<WorkOrderDTO> list = workOrderApplicationService.getDispatchedWorkOrders();
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "开始生产", description = "工单状态改为生产中，流程状态改为3，同时下发工单信息到设备")
    @PreAuthorize("@permission.has('business:workOrder:startProduction')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/{workOrderId}/startProduction")
    public ResponseDTO<SyncResultDTO> startProduction(@PathVariable Long workOrderId) {
        SyncResultDTO result = workOrderApplicationService.startProduction(workOrderId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "取消未派工工单", description = "取消流程状态为1(未派工)的工单")
    @PreAuthorize("@permission.has('business:workOrder:cancelPending')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{workOrderId}/cancelPending")
    public ResponseDTO<Void> cancelPending(@PathVariable Long workOrderId) {
        workOrderApplicationService.cancelFromPending(workOrderId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "取消已派工工单", description = "取消流程状态为2(已派工)的工单")
    @PreAuthorize("@permission.has('business:workOrder:cancelDispatched')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{workOrderId}/cancelDispatched")
    public ResponseDTO<Void> cancelDispatched(@PathVariable Long workOrderId) {
        workOrderApplicationService.cancelFromDispatched(workOrderId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "取消生产中工单", description = "取消流程状态为3(生产中)的工单")
    @PreAuthorize("@permission.has('business:workOrder:cancelProducing')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{workOrderId}/cancelProducing")
    public ResponseDTO<Void> cancelProducing(@PathVariable Long workOrderId) {
        workOrderApplicationService.cancelFromProducing(workOrderId);
        return ResponseDTO.ok();
    }

}
