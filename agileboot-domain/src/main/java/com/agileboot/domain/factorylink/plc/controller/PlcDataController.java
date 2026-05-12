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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "FactoryLink PLC 数据")
@RestController
@RequestMapping("/factorylink/plc/data")
@Validated
@RequiredArgsConstructor
public class PlcDataController {

    private final PlcDataService plcDataService;
    private final PlcDataPointService plcDataPointService;

    /**
     * 按设备返回最新一次采集的全部字段（同一组数据）。
     */
    @Operation(summary = "查询设备最新一批 PLC 数据（同一时间）")
    @GetMapping("/recent")
    public ResponseDTO<List<PlcDataEntity>> recent(
            @Parameter(description = "设备名称", required = true, example = "射出机五号机")
            @RequestParam("deviceName")
            String deviceName) {
        return ResponseDTO.ok(plcDataService.listLatestSameTimestampByDeviceName(deviceName));
    }

    /**
     * 返回该设备该秒内的全部数据点。
     */
    @Operation(summary = "按展示名查询最新一批 PLC 数据点")
    @GetMapping("/points/recent")
    public ResponseDTO<List<PlcDataPointEntity>> recentPoints(
            @Parameter(description = "数据点展示名称 display_name", required = true,example = "实时重量")
            @RequestParam("displayName")
            @NotBlank
            String displayName) {
        return ResponseDTO.ok(plcDataPointService.listLatestSameSecondByDisplayName(displayName));
    }
}
