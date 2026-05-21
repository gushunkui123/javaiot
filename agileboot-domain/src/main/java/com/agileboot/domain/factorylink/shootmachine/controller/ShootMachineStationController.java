package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "射出机站位")
@RestController
@RequestMapping("/api/shoot")
@RequiredArgsConstructor
public class ShootMachineStationController {

    private final ShootMachineStationService shootMachineStationService;

    @Operation(summary = "查询机台下的站位列表")
    @GetMapping("/stations/{machineId}")
    public ResponseDTO<List<ShootMachineStationEntity>> list(
            @Parameter(description = "机台ID", required = true) @PathVariable Long machineId) {
        return ResponseDTO.ok(shootMachineStationService.listByMachineId(machineId));
    }

    @Operation(summary = "从 PLC 最新数据同步站位")
    @PostMapping("/machines/{machineId}")
    public ResponseDTO<Map<String, Object>> syncFromPlc(
            @Parameter(description = "机台ID", required = true) @PathVariable Long machineId) {
       shootMachineStationService.syncFromPlc(machineId);
        return ResponseDTO.ok();
    }

}
