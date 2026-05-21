package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootStationScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "射出机站位排期")
@RestController
@RequestMapping("/api/shoot")
@RequiredArgsConstructor
public class ShootStationScheduleController {

    private final ShootStationScheduleService shootStationScheduleService;

    @Operation(summary = "查询站位下的排期列表")
    @GetMapping
    public ResponseDTO<List<ShootStationScheduleEntity>> listByStation(
            @Parameter(description = "站位ID", required = true) @RequestParam Long stationId) {
        return ResponseDTO.ok(shootStationScheduleService.listByStationId(stationId));
    }

    @Operation(summary = "查询机台当前时刻各站位生效中的排期")
    @GetMapping("/current")
    public ResponseDTO<List<ShootStationScheduleEntity>> listCurrentByMachine(
            @Parameter(description = "机台ID", required = true) @RequestParam Long machineId) {
        return ResponseDTO.ok(shootStationScheduleService.listCurrentByMachineId(machineId));
    }

    @Operation(summary = "查询排期详情")
    @GetMapping("/{id}")
    public ResponseDTO<ShootStationScheduleEntity> getById(
            @Parameter(description = "排期ID", required = true) @PathVariable Long id) {
        return ResponseDTO.ok(shootStationScheduleService.getByIdOrThrow(id));
    }

    @Operation(summary = "新增排期")
    @PostMapping
    public ResponseDTO<ShootStationScheduleEntity> create(@RequestBody ShootStationScheduleEntity entity) {
        return ResponseDTO.ok(shootStationScheduleService.create(entity.getStationId(), entity));
    }

    @Operation(summary = "编辑排期")
    @PutMapping("/{id}")
    public ResponseDTO<ShootStationScheduleEntity> update(
            @Parameter(description = "排期ID", required = true) @PathVariable Long id,
            @RequestBody ShootStationScheduleEntity entity) {
        return ResponseDTO.ok(shootStationScheduleService.update(id, entity));
    }

    @Operation(summary = "删除排期")
    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(
            @Parameter(description = "排期ID", required = true) @PathVariable Long id) {
        shootStationScheduleService.delete(id);
        return ResponseDTO.ok();
    }

    @Operation(summary = "取消排期")
    @PatchMapping("/{id}/cancel")
    public ResponseDTO<Void> cancel(
            @Parameter(description = "排期ID", required = true) @PathVariable Long id) {
        shootStationScheduleService.cancel(id);
        return ResponseDTO.ok();
    }
}
