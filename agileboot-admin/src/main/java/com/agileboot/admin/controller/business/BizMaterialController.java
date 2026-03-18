package com.agileboot.admin.controller.business;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.enums.common.BusinessTypeEnum;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.business.material.MaterialApplicationService;
import com.agileboot.domain.business.material.command.AddMaterialCommand;
import com.agileboot.domain.business.material.command.UpdateMaterialCommand;
import com.agileboot.domain.business.material.dto.MaterialDTO;
import com.agileboot.domain.business.material.query.MaterialQuery;
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

    @Operation(summary = "原料列表")
    @PreAuthorize("@permission.has('business:material:list')")
    @GetMapping("/list")
    public ResponseDTO<PageDTO<MaterialDTO>> list(MaterialQuery query) {
        PageDTO<MaterialDTO> pageDTO = materialApplicationService.getMaterialList(query);
        return ResponseDTO.ok(pageDTO);
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

}
