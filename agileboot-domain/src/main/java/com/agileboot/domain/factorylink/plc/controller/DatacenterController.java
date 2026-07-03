package com.agileboot.domain.factorylink.plc.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.plc.mapper.DataCenterMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "FactoryLink 数据中心")
@RestController
@RequestMapping("/factorylink/datacenter")
@Validated
@RequiredArgsConstructor
public class DatacenterController {

    private final DataCenterMapper dataCenterMapper;

    @Operation(summary = "按设备 ID 查询点位信息（数据中心 iot_data_center）")
    @GetMapping("/points/byDevice")
    public ResponseDTO<List<Map<String, Object>>> getDataPointsByDevice(
            @Parameter(description = "设备 ID", required = true)
            @RequestParam("deviceId")
            @NotNull
            Long deviceId) {
        return ResponseDTO.ok(dataCenterMapper.selectDataPointsByDeviceId(deviceId));
    }
}
