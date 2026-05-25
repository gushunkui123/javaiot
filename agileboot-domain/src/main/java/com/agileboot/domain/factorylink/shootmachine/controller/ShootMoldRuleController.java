package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMoldRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "射出机模具规则")
@RestController
@RequestMapping("/api/shoot/mold-rules")
@RequiredArgsConstructor
public class ShootMoldRuleController {

    private final ShootMoldRuleService shootMoldRuleService;

    @Operation(summary = "查询所有模具的规则列表")
    @GetMapping
    public ResponseDTO<List<ShootMoldRuleEntity>> listAll() {
        return ResponseDTO.ok(shootMoldRuleService.listAll());
    }

    @Operation(summary = "查询指定模具下的规则列表")
    @GetMapping("{moldId}")
    public ResponseDTO<List<ShootMoldRuleEntity>> listByMold(
            @Parameter(description = "模具ID", required = true) @PathVariable Long moldId) {
        return ResponseDTO.ok(shootMoldRuleService.listByMoldId(moldId));
    }

    @Operation(summary = "整批保存模具规则")
    @PutMapping("/mold/{moldId}")
    public ResponseDTO<Void> saveByMold(
            @Parameter(description = "模具ID", required = true) @PathVariable Long moldId,
            @RequestBody List<ShootMoldRuleEntity> rules) {
        shootMoldRuleService.saveByMoldId(moldId, rules);
        return ResponseDTO.ok();
    }

    @Operation(summary = "新增单条规则")
    @PostMapping("/create")
    public ResponseDTO<ShootMoldRuleEntity> create(@RequestBody ShootMoldRuleEntity entity) {
        return ResponseDTO.ok(shootMoldRuleService.create(entity));
    }

    @Operation(summary = "编辑单条规则")
    @PutMapping("/{id}")
    public ResponseDTO<ShootMoldRuleEntity> update(
            @Parameter(description = "规则ID", required = true) @PathVariable Long id,
            @RequestBody ShootMoldRuleEntity entity) {
        return ResponseDTO.ok(shootMoldRuleService.update(id, entity));
    }

    @Operation(summary = "删除单条规则")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(
            @Parameter(description = "规则ID", required = true) @PathVariable Long id) {
        shootMoldRuleService.delete(id);
        return ResponseDTO.ok();
    }
}
