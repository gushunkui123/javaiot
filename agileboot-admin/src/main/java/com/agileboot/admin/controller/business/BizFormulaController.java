package com.agileboot.admin.controller.business;

import com.agileboot.admin.customize.aop.accessLog.AccessLog;
import com.agileboot.common.core.base.BaseController;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.enums.common.BusinessTypeEnum;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.business.formula.FormulaApplicationService;
import com.agileboot.domain.business.formula.command.AddFormulaCommand;
import com.agileboot.domain.business.formula.command.UpdateFormulaCommand;
import com.agileboot.domain.business.formula.dto.FormulaDTO;
import com.agileboot.domain.business.formula.query.FormulaQuery;
import com.agileboot.domain.business.machine.ScaleSyncService;
import com.agileboot.domain.business.machine.ScaleSyncService.OperationType;
import com.agileboot.domain.business.machine.dto.SyncResultDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 配方信息操作处理
 *
 * @author Codex
 */
@Tag(name = "配方API", description = "配方相关的增删查改")
@RestController
@RequestMapping("/business/formula")
@Validated
@RequiredArgsConstructor
public class BizFormulaController extends BaseController {

    private final FormulaApplicationService formulaApplicationService;

    private final ScaleSyncService scaleSyncService;

    @Operation(summary = "配方列表")
    @PreAuthorize("@permission.has('business:formula:list')")
    @GetMapping("/list")
    public ResponseDTO<PageDTO<FormulaDTO>> list(FormulaQuery query) {
        PageDTO<FormulaDTO> pageDTO = formulaApplicationService.getFormulaList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "配方详情")
    @PreAuthorize("@permission.has('business:formula:list')")
    @GetMapping("/{formulaId}")
    public ResponseDTO<FormulaDTO> getInfo(@PathVariable Long formulaId) {
        FormulaDTO formulaDTO = formulaApplicationService.getFormulaInfo(formulaId);
        return ResponseDTO.ok(formulaDTO);
    }

    @Operation(summary = "配方Excel导入")
    @PreAuthorize("@permission.has('business:formula:import')")
    @AccessLog(title = "配方管理", businessType = BusinessTypeEnum.IMPORT)
    @PostMapping(value = "/excel", consumes = "multipart/form-data")
    public ResponseDTO<Void> importByExcel(@RequestPart("file") MultipartFile file) throws IOException {
        formulaApplicationService.importFormula(file.getInputStream());
        return ResponseDTO.ok();
    }

    @Operation(summary = "添加配方")
    @PreAuthorize("@permission.has('business:formula:add')")
    @AccessLog(title = "配方管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Validated @RequestBody AddFormulaCommand addCommand) {
        formulaApplicationService.addFormula(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改配方")
    @PreAuthorize("@permission.has('business:formula:edit')")
    @AccessLog(title = "配方管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> edit(@Validated @RequestBody UpdateFormulaCommand updateCommand) {
        formulaApplicationService.updateFormula(updateCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除配方")
    @PreAuthorize("@permission.has('business:formula:remove')")
    @AccessLog(title = "配方管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping
    public ResponseDTO<Void> remove(@RequestParam @NotNull @NotEmpty List<Long> ids) {
        formulaApplicationService.deleteFormula(new BulkOperationCommand<>(ids));
        return ResponseDTO.ok();
    }

    @Operation(summary = "下发配方到设备")
    @PreAuthorize("@permission.has('business:formula:sync')")
    @AccessLog(title = "配方管理", businessType = BusinessTypeEnum.MODIFY)
    @PostMapping("/{formulaId}/sync")
    public ResponseDTO<SyncResultDTO> syncFormula(@PathVariable Long formulaId,
            @RequestParam(defaultValue = "ADD") OperationType operationType) {
        SyncResultDTO result = scaleSyncService.syncFormula(formulaId, operationType);
        return ResponseDTO.ok(result);
    }

}
