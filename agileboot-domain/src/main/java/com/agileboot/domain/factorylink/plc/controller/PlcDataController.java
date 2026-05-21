package com.agileboot.domain.factorylink.plc.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.agileboot.domain.factorylink.plc.service.PlcDataPointService;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FactoryLink PLC 数据")
@RestController
@RequestMapping("/factorylink/plc")
@Validated
@RequiredArgsConstructor
public class PlcDataController {

    private final PlcDataService plcDataService;
    private final PlcDataPointService plcDataPointService;

    @Operation(summary = "查询设备最新一批 PLC 数据（同一时间）")
    @GetMapping("/data/recent")
    public ResponseDTO<List<PlcDataEntity>> recent(
            @Parameter(description = "设备名称", required = true, example = "射出机五号机")
            @RequestParam("deviceName")
            @NotBlank
            String deviceName) {
        return ResponseDTO.ok(plcDataService.listLatestSameTimestampByDeviceName(deviceName));
    }

    @Operation(summary = "按设备名称查询 PLC 数据点（按 sort_order 排序）")
    @GetMapping("/data/points/recent")
    public ResponseDTO<List<PlcDataPointEntity>> recentPoints(
            @Parameter(description = "设备名称", required = true, example = "主磅")
            @RequestParam("deviceName")
            @NotBlank
            String deviceName) {
        return ResponseDTO.ok(plcDataPointService.listAllByDeviceName(deviceName));
    }

    @Operation(summary = "保存点位排序与勾选颜色")
    @PutMapping("/data/points/sort")
    public ResponseDTO<Void> updatePointSort(
            @Parameter(description = "设备名称", required = true)
            @RequestParam("deviceName")
            @NotBlank
            String deviceName,
            @RequestBody List<PlcDataPointEntity> items) {
        plcDataPointService.batchUpdateSortOrder(deviceName, items);
        return ResponseDTO.ok();
    }
}
