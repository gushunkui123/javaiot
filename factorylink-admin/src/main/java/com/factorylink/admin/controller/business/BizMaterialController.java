package com.factorylink.admin.controller.business;

import cn.hutool.core.collection.ListUtil;
import com.factorylink.admin.customize.aop.accessLog.AccessLog;
import com.factorylink.common.core.base.BaseController;
import com.factorylink.common.core.dto.ResponseDTO;
import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.enums.common.BusinessTypeEnum;
import com.factorylink.common.utils.poi.CustomExcelUtil;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.business.material.MaterialApplicationService;
import com.factorylink.domain.business.material.command.AddMaterialCommand;
import com.factorylink.domain.business.material.command.UpdateMaterialCommand;
import com.factorylink.domain.business.material.dto.MaterialDTO;
import com.factorylink.domain.business.material.query.MaterialQuery;
import com.factorylink.domain.business.machine.ScaleSyncService;
import com.factorylink.domain.business.machine.ScaleSyncService.OperationType;
import com.factorylink.domain.business.machine.dto.SyncResultDTO;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 原料信息操作处理
 *
 * @author Codex
 */
@Tag(name = "原料API", description = "原料相关的增删查改")
@RestController
@RequestMapping("/business/material")
@Validated
@RequiredArgsConstructor
public class BizMaterialController extends BaseController {

    private final MaterialApplicationService materialApplicationService;

    private final ScaleSyncService scaleSyncService;

    @Operation(summary = "原料列表")
    @PreAuthorize("@permission.has('business:material:list')")
    @GetMapping("/list")
    public ResponseDTO<PageDTO<MaterialDTO>> list(MaterialQuery query) {
        PageDTO<MaterialDTO> pageDTO = materialApplicationService.getMaterialList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "原料列表导出")
    @PreAuthorize("@permission.has('business:material:export')")
    @AccessLog(title = "原料管理", businessType = BusinessTypeEnum.EXPORT)
    @GetMapping(value = "/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public void exportByExcel(HttpServletResponse response, MaterialQuery query) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=material_export.xlsx");
        PageDTO<MaterialDTO> materialList = materialApplicationService.getMaterialList(query);
        CustomExcelUtil.writeToResponse(materialList.getRows(), MaterialDTO.class, response);
    }

    @Operation(summary = "原料列表导入")
    @PreAuthorize("@permission.has('business:material:import')")
    @AccessLog(title = "原料管理", businessType = BusinessTypeEnum.IMPORT)
    @PostMapping(value = "/excel", consumes = "multipart/form-data")
    public ResponseDTO<Void> importByExcel(@RequestPart("file") MultipartFile file) {
        List<AddMaterialCommand> commands = CustomExcelUtil.readFromRequest(AddMaterialCommand.class, file);
        materialApplicationService.importMaterial(commands);
        return ResponseDTO.ok();
    }

    @Operation(summary = "原料导入模板下载")
    @GetMapping(value = "/excelTemplate", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public void downloadExcelTemplate(HttpServletResponse response) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=material_template.xlsx");
        CustomExcelUtil.writeToResponse(ListUtil.toList(new AddMaterialCommand()), AddMaterialCommand.class, response);
    }

    @Operation(summary = "添加原料")
    @PreAuthorize("@permission.has('business:material:add')")
    @AccessLog(title = "原料管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@RequestBody AddMaterialCommand addCommand) {
        materialApplicationService.addMaterial(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改原料")
    @PreAuthorize("@permission.has('business:material:edit')")
    @AccessLog(title = "原料管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> edit(@RequestBody UpdateMaterialCommand updateCommand) {
        materialApplicationService.updateMaterial(updateCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除原料")
    @PreAuthorize("@permission.has('business:material:remove')")
    @AccessLog(title = "原料管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping
    public ResponseDTO<Void> remove(@RequestParam @NotNull @NotEmpty List<Long> ids) {
        materialApplicationService.deleteMaterial(new BulkOperationCommand<>(ids));
        return ResponseDTO.ok();
    }

    @Operation(summary = "下发原料到设备")
    @PreAuthorize("@permission.has('business:material:sync')")
    @AccessLog(title = "原料管理", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/{materialId}/sync")
    public ResponseDTO<SyncResultDTO> syncMaterial(@PathVariable Long materialId,
            @RequestParam(defaultValue = "ADD") OperationType operationType) {
        SyncResultDTO result = scaleSyncService.syncMaterial(materialId, operationType);
        return ResponseDTO.ok(result);
    }

}
