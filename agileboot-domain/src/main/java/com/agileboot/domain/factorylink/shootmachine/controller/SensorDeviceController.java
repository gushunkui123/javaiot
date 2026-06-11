package com.agileboot.domain.factorylink.shootmachine.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.shootmachine.entity.SensorDeviceEntity;
import com.agileboot.domain.factorylink.shootmachine.service.SensorDeviceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 传感器设备 Controller
 */
@Tag(name = "传感器设备")
@RestController
@RequestMapping("/api/sensor/device")
@RequiredArgsConstructor
public class SensorDeviceController {

    private final SensorDeviceService sensorDeviceService;

    @Operation(summary = "分页查询设备列表")
    @GetMapping("/page")
    public ResponseDTO<IPage<SensorDeviceEntity>> page(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestParam(required = false) String deviceName) {
        Page<SensorDeviceEntity> page = 
                new Page<>(pageNum, pageSize);
        return ResponseDTO.ok(sensorDeviceService.page(page, deviceName));
    }

    @Operation(summary = "查询所有设备列表")
    @GetMapping("/list")
    public ResponseDTO<List<SensorDeviceEntity>> list() {
        return ResponseDTO.ok(sensorDeviceService.list());
    }
}
