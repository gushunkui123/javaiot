package com.agileboot.domain.factorylink.plc.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcThresholdEntity;
import com.agileboot.domain.factorylink.plc.service.PlcDataPointService;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.plc.service.PlcThresholdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final PlcThresholdService plcThresholdService;

    /**
     * 按设备返回最新一次采集的全部字段（同一组数据）。
     */
    @Operation(summary = "查询设备最新一批 PLC 数据（同一时间）")
    @GetMapping("/data/recent")
    public ResponseDTO<List<PlcDataEntity>> recent(
            @Parameter(description = "设备名称", required = true, example = "射出机五号机")
            @RequestParam("deviceName")
            String deviceName) {
        return ResponseDTO.ok(plcDataService.listLatestSameTimestampByDeviceName(deviceName));
    }

    /**
     * 根据 设备名 解析设备 id返回该设备全部数据点。
     */
    @Operation(summary = "按设备名称查询该设备下全部 PLC 数据点")
    @GetMapping("/data/points/recent")
    public ResponseDTO<List<PlcDataPointEntity>> recentPoints(
            @Parameter(description = "设备名称", required = true, example = "主磅")
            @RequestParam("deviceName")
            @NotBlank
            String deviceName) {
        return ResponseDTO.ok(plcDataPointService.listAllByDeviceName(deviceName));
    }

//    /**
//     * 根据设备名称查询阈值配置
//     */
//    @Operation(summary = "查询设备阈值配置")
//    @GetMapping("/threshold")
//    public ResponseDTO<PlcThresholdEntity> getThreshold(
//            @Parameter(description = "设备名称", required = true, example = "射出机五号机")
//            @RequestParam("deviceName")
//            String deviceName) {
//        return ResponseDTO.ok(plcThresholdService.getByDeviceName(deviceName));
//    }
//
//    /**
//     * 新增或更新阈值配置
//     */
//    @Operation(summary = "新增或更新设备阈值配置")
//    @PostMapping("/threshold/saveOrUpdate")
//    public ResponseDTO<PlcThresholdEntity> saveOrUpdateThreshold(
//            @Parameter(description = "阈值配置", required = true)
//            @RequestBody
//            PlcThresholdEntity entity) {
//        plcThresholdService.saveOrUpdate(entity);
//        return ResponseDTO.ok(entity);
//    }
}