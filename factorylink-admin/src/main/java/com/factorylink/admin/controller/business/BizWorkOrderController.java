package com.factorylink.admin.controller.business;

import com.factorylink.admin.customize.aop.accessLog.AccessLog;
import com.factorylink.common.core.base.BaseController;
import com.factorylink.common.core.dto.ResponseDTO;
import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.enums.common.BusinessTypeEnum;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.common.utils.poi.CustomExcelUtil;
import com.factorylink.domain.business.workorder.WorkOrderApplicationService;
import com.factorylink.domain.business.workorder.command.AddWorkOrderCommand;
import com.factorylink.domain.business.workorder.command.AssignFormulaCommand;
import com.factorylink.domain.business.workorder.command.ModifyWorkOrderFormulaCommand;
import com.factorylink.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.factorylink.domain.business.workorder.dto.WorkOrderDTO;
import com.factorylink.domain.business.workorder.query.WorkOrderQuery;
import com.factorylink.domain.business.machine.ScaleSyncService.OperationType;
import com.factorylink.domain.business.machine.dto.SyncResultDTO;
import cn.hutool.core.date.DateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
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
@Tag(name = "工单API", description = "工单全生命周期管理，包括创建、分配配方、开始生产、取消等流程操作。"
        + " 流程状态流转：已创建(1) → 已添加配方(2) → 生产中(3) → 已完成(4)，任意阶段均可取消(5)")
@RestController
@RequestMapping("/business/workOrder")
@Validated
@RequiredArgsConstructor
public class BizWorkOrderController extends BaseController {

    private final WorkOrderApplicationService workOrderApplicationService;

    @Operation(summary = "工单列表", description = "分页查询工单列表，支持按工单编号、配方编号、设备编号、工单状态筛选，默认按创建时间倒序排列")
    @PreAuthorize("@permission.has('business:workOrder:list')")
    @GetMapping("/list")
    public ResponseDTO<PageDTO<WorkOrderDTO>> list(WorkOrderQuery query) {
        PageDTO<WorkOrderDTO> pageDTO = workOrderApplicationService.getWorkOrderList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "工单列表导出", description = "导出符合查询条件的全部工单数据为Excel文件（.xlsx格式）")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.EXPORT)
    @PreAuthorize("@permission.has('business:workOrder:export')")
    @GetMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public void export(HttpServletResponse response, WorkOrderQuery query) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=workorder_export.xlsx");
        List<WorkOrderDTO> all = workOrderApplicationService.getWorkOrderListAll(query);
        CustomExcelUtil.writeToResponse(all, WorkOrderDTO.class, response);
    }

    @Operation(summary = "工单详情", description = "根据工单ID查询单条工单的完整信息")
    @PreAuthorize("@permission.has('business:workOrder:query')")
    @GetMapping("/{workOrderId}")
    public ResponseDTO<WorkOrderDTO> getInfo(
            @Parameter(description = "工单ID", required = true) @PathVariable Long workOrderId) {
        WorkOrderDTO workOrderDTO = workOrderApplicationService.getWorkOrderInfo(workOrderId);
        return ResponseDTO.ok(workOrderDTO);
    }

    @Operation(summary = "生成工单编号", description = "返回自动生成的工单编号前缀（LZ+当天日期），用户在此基础上补充序号")
    @PreAuthorize("@permission.has('business:workOrder:add')")
    @GetMapping("/generateWorkOrderNo")
    public ResponseDTO<String> generateWorkOrderNo() {
        String workOrderNo = "LZ" + DateUtil.format(new Date(), "yyyyMMdd");
        return ResponseDTO.ok(workOrderNo);
    }

    @Operation(summary = "添加工单", description = "创建新工单，初始流程状态为1(已创建)。"
            + " 必填字段：产线编号(lineNo)、模具代号(moldCode)、型体颜色(modelColor)、计划批次数(orderBatchNum)。"
            + " 选填字段：工单编号(workOrderNo，不传则由系统自动生成)、工单日期(orderDate，不传则自动取当天日期)、"
            + "设备编号(machineId)、工厂别(plant)、配方编号(formulaCode)、配方ID(formulaId)、"
            + "单批次重量(batchWeight，默认75kg)、备注(remark)。"
            + " 计划重量 = 单批次重量 × 计划批次数")
    @PreAuthorize("@permission.has('business:workOrder:add')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Validated @RequestBody AddWorkOrderCommand addCommand) {
        workOrderApplicationService.addWorkOrder(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改工单", description = "修改已有工单的基本信息，工单编号不允许与其他工单重复")
    @PreAuthorize("@permission.has('business:workOrder:edit')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> edit(@Validated @RequestBody UpdateWorkOrderCommand updateCommand) {
        workOrderApplicationService.updateWorkOrder(updateCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除工单", description = "按ID批量删除工单，已完成状态的工单不允许删除")
    @PreAuthorize("@permission.has('business:workOrder:remove')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping
    public ResponseDTO<Void> remove(@RequestParam @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.NotEmpty List<Long> ids) {
        workOrderApplicationService.deleteWorkOrder(new BulkOperationCommand<>(ids));
        return ResponseDTO.ok();
    }

    @Operation(summary = "下发工单到设备", description = "将工单信息下发至主磅/微量磅秤设备。"
            + " 下发工单前会自动先下发配方（DELETE操作除外）。"
            + " 操作类型：ADD-新增下发，UPDATE-更新下发，DELETE-删除下发")
    @PreAuthorize("@permission.has('business:workOrder:sync')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/{workOrderId}/sync")
    public ResponseDTO<SyncResultDTO> syncWorkOrder(
            @Parameter(description = "工单ID", required = true) @PathVariable Long workOrderId,
            @Parameter(description = "操作类型：ADD-新增, UPDATE-更新, DELETE-删除") @RequestParam(defaultValue = "ADD") OperationType operationType) {
        SyncResultDTO result = workOrderApplicationService.syncWorkOrderWithFormula(workOrderId, operationType);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "分配配方", description = "为工单分配配方ID和配方编号，流程状态从1(已创建)变为2(已添加配方)。"
            + " 仅流程状态为1的工单可执行此操作")
    @PreAuthorize("@permission.has('business:workOrder:assignFormula')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/assignFormula")
    public ResponseDTO<Void> assignFormula(@Validated @RequestBody AssignFormulaCommand command) {
        workOrderApplicationService.assignFormula(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "查询已添加配方工单", description = "查询流程状态为2(已添加配方)的工单列表，"
            + "用于生产调度员选择工单并开始生产。按创建时间倒序排列")
    @PreAuthorize("@permission.has('business:workOrder:dispatched')")
    @GetMapping("/dispatched")
    public ResponseDTO<List<WorkOrderDTO>> dispatchedList() {
        List<WorkOrderDTO> list = workOrderApplicationService.getDispatchedWorkOrders();
        return ResponseDTO.ok(list);
    }

    @Operation(summary = "开始生产", description = "将工单状态改为生产中，流程状态从2(已添加配方)变为3(生产中)，"
            + "记录生产开始时间，并自动将工单信息下发到磅秤设备。若下发失败不影响状态变更，后续可手动重试下发")
    @PreAuthorize("@permission.has('business:workOrder:startProduction')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/{workOrderId}/startProduction")
    public ResponseDTO<SyncResultDTO> startProduction(
            @Parameter(description = "工单ID", required = true) @PathVariable Long workOrderId) {
        SyncResultDTO result = workOrderApplicationService.startProduction(workOrderId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "修改工单配方", description = "修改工单关联配方的明细内容。"
            + " 流程状态为2(已添加配方)时可直接修改；流程状态为3(生产中)时需传confirmed=true确认后才能修改，"
            + "修改后会通过SSE向现场人员推送告警消息")
    @PreAuthorize("@permission.has('business:workOrder:modifyFormula')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{workOrderId}/formula")
    public ResponseDTO<Void> modifyFormula(
            @Parameter(description = "工单ID", required = true) @PathVariable Long workOrderId,
            @Validated @RequestBody ModifyWorkOrderFormulaCommand command) {
        command.setWorkOrderId(workOrderId);
        workOrderApplicationService.modifyWorkOrderFormula(command);
        return ResponseDTO.ok();
    }

    @Operation(summary = "取消工单", description = "取消工单，流程状态变为5(已取消)，工单状态变为4(已取消)。"
            + " 仅流程状态为1(已创建)、2(已添加配方)、3(生产中)的工单可取消，"
            + "已完成或已取消的工单不可重复操作")
    @PreAuthorize("@permission.has('business:workOrder:cancel')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping("/{workOrderId}/cancel")
    public ResponseDTO<Void> cancel(
            @Parameter(description = "工单ID", required = true) @PathVariable Long workOrderId) {
        workOrderApplicationService.cancelWorkOrder(workOrderId);
        return ResponseDTO.ok();
    }

}
