package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootBoardFieldEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootBoardFieldConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "看板固定字段配置")
@RestController
@RequestMapping("/api/shoot/board")
@RequiredArgsConstructor
public class ShootBoardFieldConfigController {

    private final ShootBoardFieldConfigService boardFieldConfigService;

    @Operation(summary = "查询看板顶部固定展示字段（含实时值与报警状态）")
    @GetMapping("/fixed-fields")
    public ResponseDTO<List<ShootBoardFieldEntity>> listFixedFields(
            @RequestParam Long machineId,
            @RequestParam(required = false) Integer stationNo) {
        return ResponseDTO.ok(boardFieldConfigService.listBoardFixedFields(machineId, stationNo));
    }

    @Operation(summary = "批量查询看板固定展示字段：一次返回多个站台的固定字段（按站台隔离取实时值）")
    @GetMapping("/fixed-fields-batch")
    public ResponseDTO<Map<Integer, List<ShootBoardFieldEntity>>> listFixedFieldsBatch(
            @RequestParam Long machineId,
            @RequestParam List<Integer> stationNos) {
        return ResponseDTO.ok(boardFieldConfigService.listBoardFixedFieldsForStations(machineId, stationNos));
    }
}
