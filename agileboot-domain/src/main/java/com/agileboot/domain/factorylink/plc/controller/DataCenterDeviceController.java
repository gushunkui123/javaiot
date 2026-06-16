package com.agileboot.domain.factorylink.plc.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.plc.entity.DataCenterDeviceEntity;
import com.agileboot.domain.factorylink.plc.service.DataCenterDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据中台 PLC 设备配置")
@RestController
@RequestMapping("/factorylink/datacenter")
@RequiredArgsConstructor
@Validated
public class DataCenterDeviceController {

    private final DataCenterDeviceService dataCenterDeviceService;

    @Operation(summary = "查询全部设备列表")
    @GetMapping("/devices/all")
    public ResponseDTO<List<DataCenterDeviceEntity>> listAll() {
        return ResponseDTO.ok(dataCenterDeviceService.listAll());
    }

    @Operation(summary = "按车间ID查询设备")
    @GetMapping("/devices/byWorkshop")
    public ResponseDTO<List<DataCenterDeviceEntity>> listByWorkshopId(
            @Parameter(description = "车间ID", required = true)
            @RequestParam("workshopId") Long workshopId) {
        return ResponseDTO.ok(dataCenterDeviceService.listByWorkshopId(workshopId));
    }

    @Operation(summary = "按区域ID查询设备")
    @GetMapping("/devices/byArea")
    public ResponseDTO<List<DataCenterDeviceEntity>> listByAreaId(
            @Parameter(description = "区域ID", required = true)
            @RequestParam("areaId") Long areaId) {
        return ResponseDTO.ok(dataCenterDeviceService.listByAreaId(areaId));
    }

    @Operation(summary = "按关键字搜索设备")
    @GetMapping("/devices/search")
    public ResponseDTO<List<DataCenterDeviceEntity>> search(
            @Parameter(description = "搜索关键字", required = true)
            @RequestParam("keyword") String keyword) {
        return ResponseDTO.ok(dataCenterDeviceService.searchByKeyword(keyword));
    }
}
