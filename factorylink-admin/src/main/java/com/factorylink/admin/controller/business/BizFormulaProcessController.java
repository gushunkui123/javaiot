package com.factorylink.admin.controller.business;

import com.factorylink.admin.customize.aop.accessLog.AccessLog;
import com.factorylink.common.core.base.BaseController;
import com.factorylink.common.core.dto.ResponseDTO;
import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.enums.common.BusinessTypeEnum;
import com.factorylink.domain.business.formula.process.FormulaProcessApplicationService;
import com.factorylink.domain.business.formula.process.command.AddFormulaProcessCommand;
import com.factorylink.domain.business.formula.process.command.UpdateFormulaProcessCommand;
import com.factorylink.domain.business.formula.process.dto.FormulaProcessDTO;
import com.factorylink.domain.business.formula.process.query.FormulaProcessQuery;
import com.factorylink.domain.common.command.BulkOperationCommand;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工艺信息操作处理
 *
 * @author Codex
 */
@Tag(name = "工艺API", description = "工艺模板相关的增删查改")
@RestController
@RequestMapping("/business/formula-process")
@Validated
@RequiredArgsConstructor
public class BizFormulaProcessController extends BaseController {

    private final FormulaProcessApplicationService formulaProcessApplicationService;

    @Operation(summary = "工艺列表")
    @PreAuthorize("@permission.has('business:formula-process:list')")
    @AccessLog(title = "工艺管理", businessType = BusinessTypeEnum.QUERY)
    @GetMapping("/list")
    public ResponseDTO<PageDTO<FormulaProcessDTO>> list(FormulaProcessQuery query) {
        PageDTO<FormulaProcessDTO> pageDTO = formulaProcessApplicationService.getFormulaProcessList(query);
        return ResponseDTO.ok(pageDTO);
    }

    @Operation(summary = "工艺详情")
    @PreAuthorize("@permission.has('business:formula-process:list')")
    @AccessLog(title = "工艺管理", businessType = BusinessTypeEnum.QUERY)
    @GetMapping("/{processId}")
    public ResponseDTO<FormulaProcessDTO> getInfo(@PathVariable Long processId) {
        FormulaProcessDTO dto = formulaProcessApplicationService.getFormulaProcessInfo(processId);
        return ResponseDTO.ok(dto);
    }

    @Operation(summary = "添加工艺")
    @PreAuthorize("@permission.has('business:formula-process:add')")
    @AccessLog(title = "工艺管理", businessType = BusinessTypeEnum.ADD)
    @PostMapping
    public ResponseDTO<Void> add(@Validated @RequestBody AddFormulaProcessCommand addCommand) {
        formulaProcessApplicationService.addFormulaProcess(addCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改工艺")
    @PreAuthorize("@permission.has('business:formula-process:edit')")
    @AccessLog(title = "工艺管理", businessType = BusinessTypeEnum.MODIFY)
    @PutMapping
    public ResponseDTO<Void> edit(@Validated @RequestBody UpdateFormulaProcessCommand updateCommand) {
        formulaProcessApplicationService.updateFormulaProcess(updateCommand);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除工艺")
    @PreAuthorize("@permission.has('business:formula-process:remove')")
    @AccessLog(title = "工艺管理", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping
    public ResponseDTO<Void> remove(@RequestParam @NotNull @NotEmpty List<Long> ids) {
        formulaProcessApplicationService.deleteFormulaProcess(new BulkOperationCommand<>(ids));
        return ResponseDTO.ok();
    }

}
