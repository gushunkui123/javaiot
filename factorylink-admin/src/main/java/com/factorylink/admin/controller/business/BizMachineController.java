package com.factorylink.admin.controller.business;

import com.factorylink.admin.customize.aop.accessLog.AccessLog;
import com.factorylink.common.core.base.BaseController;
import com.factorylink.common.core.dto.ResponseDTO;
import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.enums.common.BusinessTypeEnum;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.business.machine.MachineApplicationService;
import com.factorylink.domain.business.machine.command.AddMachineCommand;
import com.factorylink.domain.business.machine.command.UpdateMachineCommand;
import com.factorylink.domain.business.machine.dto.MachineDTO;
import com.factorylink.domain.business.machine.query.MachineQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备信息操作处理
 */
@Tag(name = "设备管理API", description = "设备（磅秤/贴标机）的增删查改")
@RestController
@RequestMapping("/business/machine")
@Validated
@RequiredArgsConstructor
public class BizMachineController extends BaseController {

    private final MachineApplicationService machineApplicationService;

    @Operation(summary = "设备列表")
    @PreAuthorize("@permission.has('business:machine:list')")
    @GetMapping("/list")
    public ResponseDTO<PageDTO<MachineDTO>> list(MachineQuery query) {
        PageDTO<MachineDTO> pageDTO = machineApplicationService.getMachineList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "添加设备")
    @PreAuthorize("@permission.has('business:machine:add')")
    @AccessLog(title = "设备管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Validated @RequestBody AddMachineCommand addCommand) {
        machineApplicationService.addMachine(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改设备")
    @PreAuthorize("@permission.has('business:machine:edit')")
    @AccessLog(title = "设备管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> edit(@Validated @RequestBody UpdateMachineCommand updateCommand) {
        machineApplicationService.updateMachine(updateCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除设备")
    @PreAuthorize("@permission.has('business:machine:remove')")
    @AccessLog(title = "设备管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping
    public ResponseDTO<Void> remove(@RequestParam @NotNull @NotEmpty List<Long> ids) {
        machineApplicationService.deleteMachine(new BulkOperationCommand<>(ids));
        return ResponseDTO.ok();
    }

}
