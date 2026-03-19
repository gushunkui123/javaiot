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
import com.agileboot.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.agileboot.domain.business.workorder.dto.WorkOrderDTO;
import com.agileboot.domain.business.workorder.query.WorkOrderQuery;
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
    public ResponseDTO<Void> add(@RequestBody AddWorkOrderCommand addCommand) {
        workOrderApplicationService.addWorkOrder(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改工单")
    @PreAuthorize("@permission.has('business:workOrder:edit')")
    @AccessLog(title = "工单管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> edit(@RequestBody UpdateWorkOrderCommand updateCommand) {
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

}
