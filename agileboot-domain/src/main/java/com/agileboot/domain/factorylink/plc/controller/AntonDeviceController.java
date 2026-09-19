package com.agileboot.domain.factorylink.plc.controller;

import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.domain.factorylink.plc.dto.AntonDeviceBindingResultDTO;
import com.agileboot.domain.factorylink.plc.dto.AntonDeviceListDTO;
import com.agileboot.domain.factorylink.plc.util.AntonDeviceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 安冬硬件平台设备查询接口，对应其：
 * <ul>
 *     <li>{@code GET /api/anton/device/list} —— 设备列表（{@code /list}）；</li>
 *     <li>{@code GET /api/anton/device/{deviceId}/bindings} —— 设备绑定的生产设备（{@code /bindings}，
 *         内部先取列表拿到 deviceId 再逐个查询，一步得到映射关系）。</li>
 * </ul>
 * 用于核对"我们的机台 ↔ 安冬设备 ↔ 生产设备"的映射，并验证 API Key 的设备查询权限。
 */
@Tag(name = "FactoryLink 安冬设备")
@RestController
@RequestMapping("/factorylink/anton/device")
@RequiredArgsConstructor
public class AntonDeviceController {

    private final AntonDeviceClient antonDeviceClient;

    @Operation(summary = "查询安冬设备列表（代理 GET /api/anton/device/list，返回设备信息）")
    @GetMapping("/list")
    public ResponseDTO<AntonDeviceListDTO> list(
            @Parameter(description = "页码（可选，不传=第三方默认 1）")
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @Parameter(description = "每页条数（可选，不传=第三方默认 10，想看全量可传 200）")
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResponseDTO.ok(antonDeviceClient.listDevices(pageNum, pageSize));
    }

    @Operation(summary = "查询安冬设备已绑定的生产设备（内部先取设备列表拿到 deviceId，再逐个查询绑定）")
    @GetMapping("/bindings")
    public ResponseDTO<List<AntonDeviceBindingResultDTO>> bindings(
            @Parameter(description = "页码（可选，不传=第三方默认 1）")
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @Parameter(description = "每页条数（可选，不传=第三方默认 10，想看全量可传 200）")
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        return ResponseDTO.ok(antonDeviceClient.listDeviceBindings(pageNum, pageSize));
    }
}
