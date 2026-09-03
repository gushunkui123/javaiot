package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.annotation.AccessLog;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.common.enums.common.BusinessTypeEnum;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "射出机模具")
@RestController
@RequestMapping("/api/shoot/mold")
@RequiredArgsConstructor
public class ShootMoldController {

    private final ShootMoldService shootMoldService;

    @Operation(summary = "分页查询模具列表")
    @GetMapping
    public ResponseDTO<PageDTO<ShootMoldEntity>> list(
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "pageNum", defaultValue = "1")
            int pageNum,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(value = "pageSize", defaultValue = "20")
            int pageSize,
            @Parameter(description = "是否仅返回启用模具（排期下拉用）")
            @RequestParam(value = "enabled", required = false)
            Boolean enabled,
            @Parameter(description = "模具型号（模糊匹配）")
            @RequestParam(value = "moldModel", required = false)
            String moldModel,
            @Parameter(description = "颜色（模糊匹配）")
            @RequestParam(value = "color", required = false)
            String color) {
        return ResponseDTO.ok(shootMoldService.list(pageNum, pageSize, enabled, moldModel, color));
    }

    @Operation(summary = "查询模具详情")
    @GetMapping("/{id}")
    public ResponseDTO<ShootMoldEntity> getById(
            @Parameter(description = "模具ID", required = true) @PathVariable Long id) {
        return ResponseDTO.ok(shootMoldService.getByIdOrThrow(id));
    }

    @AccessLog(title = "射出机模具", businessType = BusinessTypeEnum.ADD)
    @Operation(summary = "新增模具")
    @PostMapping("/create")
    public ResponseDTO<ShootMoldEntity> create(@RequestBody ShootMoldEntity entity) {
        return ResponseDTO.ok(shootMoldService.create(entity));
    }

    @AccessLog(title = "射出机模具", businessType = BusinessTypeEnum.MODIFY)
    @Operation(summary = "编辑模具")
    @PutMapping("/{id}")
    public ResponseDTO<ShootMoldEntity> update(
            @Parameter(description = "模具ID", required = true) @PathVariable Long id,
            @RequestBody ShootMoldEntity entity) {
        return ResponseDTO.ok(shootMoldService.update(id, entity));
    }

    @AccessLog(title = "射出机模具", businessType = BusinessTypeEnum.DELETE)
    @Operation(summary = "删除模具")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(
            @Parameter(description = "模具ID", required = true) @PathVariable Long id) {
        shootMoldService.delete(id);
        return ResponseDTO.ok();
    }
}
