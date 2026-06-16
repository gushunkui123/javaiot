package com.agileboot.domain.factorylink.plc.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.plc.entity.DataCenterPointEntity;
import com.agileboot.domain.factorylink.plc.service.DataCenterPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "数据中台 PLC 数据点")
@RestController
@RequestMapping("/factorylink/datacenter")
@RequiredArgsConstructor
public class DataCenterPointController {

    private final DataCenterPointService dataCenterPointService;

    @Operation(summary = "按设备ID查询数据点")
    @GetMapping("/points/byDevice")
    public ResponseDTO<List<DataCenterPointEntity>> listByDeviceId(
            @Parameter(description = "设备ID", required = true)
            @RequestParam("deviceId") Long deviceId) {
        return ResponseDTO.ok(dataCenterPointService.listByDeviceId(deviceId));
    }

    @Operation(summary = "按分类ID查询数据点")
    @GetMapping("/points/byCategory")
    public ResponseDTO<List<DataCenterPointEntity>> listByCategoryId(
            @Parameter(description = "分类ID", required = true)
            @RequestParam("categoryId") Long categoryId) {
        return ResponseDTO.ok(dataCenterPointService.listByCategoryId(categoryId));
    }
}
