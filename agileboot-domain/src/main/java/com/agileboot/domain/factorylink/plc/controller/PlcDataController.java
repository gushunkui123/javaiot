package com.agileboot.domain.factorylink.plc.controller;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.core.dto.ResponseDTO;
import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.factorylink.plc.entity.EnvironmentDataEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDeviceEntity;
import com.agileboot.domain.factorylink.plc.service.EnvironmentDataService;
import com.agileboot.domain.factorylink.plc.service.PlcDataPointService;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.plc.service.PlcDeviceService;
import com.agileboot.domain.factorylink.plc.util.MinioUploadUtil;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.agileboot.domain.factorylink.plc.util.SignedRestTemplateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "FactoryLink PLC 数据")
@RestController
@RequestMapping("/factorylink/plc")
@Validated
@RequiredArgsConstructor
@Slf4j
public class PlcDataController {

    private final PlcDataService plcDataService;
    private final PlcDataPointService plcDataPointService;
    private final PlcDeviceService plcDeviceService;
    private final EnvironmentDataService environmentDataService;
    private final RestTemplate restTemplate;

    @Value("${factory-link.workshop.base-url:http://10.0.100.225}")
    private String workshopBaseUrl;

    @Value("${factory-link.workshop.plc-device-path:/api/device/queryPlcDataDeviceList}")
    private String plcDevicePath;

    @Value("${factory-link.workshop.plc-data-point-path:/api/device/queryPlcDataPointList}")
    private String plcDataPointPath;

    @Value("${factory-link.workshop.api-key:}")
    private String workshopApiKey;

    @Value("${factory-link.workshop.api-secret:}")
    private String workshopApiSecret;

    //射出机5号机的设备数据
    @Operation(summary = "查询设备最新一批 PLC 数据（同一时间）")
    @GetMapping("/data/recent")
    public ResponseDTO<List<PlcDataEntity>> recent(
            @Parameter(description = "设备名称", required = true, example = "射出机五号机")
            @RequestParam("deviceName")
            @NotBlank
            String deviceName) {
        return ResponseDTO.ok(plcDataService.listLatestSameTimestampByDeviceName(deviceName));
    }

    //不包含射出机5号的 设备
    @Operation(summary = "查询 PLC 设备列表")
    @GetMapping("/devices")
    public ResponseDTO<List<PlcDeviceEntity>> listDevices() {
        return ResponseDTO.ok(plcDeviceService.listAllDevices());
    }

    //包含射出机的点位数据
    @Operation(summary = "查询设备及其点位数据")
    @GetMapping("/dataPoints")
        public ResponseDTO<List<Map<String, Object>>> listDataPoints(
            @Parameter(description = "设备ID", required = false)
            @RequestParam(value = "deviceId", required = false) Long deviceId) {
        return ResponseDTO.ok(plcDeviceService.listDevicesWithDataPoints(deviceId));
    }
//    public ResponseDTO<List<Map<String, Object>>> listDataPoints(
//            @Parameter(description = "设备ID", required = false)
//            @RequestParam(value = "deviceId", required = false) Long deviceId,
//            @Parameter(description = "设备编码", required = false)
//            @RequestParam(value = "deviceCode", required = false) String deviceCode) {
//
//        log.info("调用外部PLC数据点接口: {}{}", workshopBaseUrl, plcDataPointPath);
//
//        Map<String, String> businessParams = new HashMap<>();
//        if (deviceId != null) {
//            businessParams.put("deviceId", deviceId.toString());
//        }
//        if (deviceCode != null && !deviceCode.isEmpty()) {
//            businessParams.put("deviceCode", deviceCode);
//        }
//
//        SignedRestTemplateUtil signedRestTemplate = new SignedRestTemplateUtil(restTemplate, workshopApiKey, workshopApiSecret);
//        ResponseEntity<Map<String, Object>> response = signedRestTemplate.get(workshopBaseUrl, plcDataPointPath, businessParams, new ParameterizedTypeReference<Map<String, Object>>() {});
//
//        Map<String, Object> result = response.getBody();
//        List<Map<String, Object>> data = (List<Map<String, Object>>) result.get("data");
//
//        Map<Long, Map<String, Object>> deviceMap = new HashMap<>();
//        for (Map<String, Object> point : data) {
//            Long devId = point.get("deviceId") instanceof Number ? ((Number) point.get("deviceId")).longValue() : null;
//            String devName = (String) point.get("deviceName");
//
//            Map<String, Object> device = deviceMap.computeIfAbsent(devId, k -> {
//                Map<String, Object> d = new HashMap<>();
//                d.put("deviceId", devId);
//                d.put("deviceName", devName);
//                d.put("dataPoints", new ArrayList<>());
//                return d;
//            });
//
//            Map<String, Object> pointData = new HashMap<>(point);
//            pointData.remove("deviceId");
//            pointData.remove("deviceName");
//            ((List<Map<String, Object>>) device.get("dataPoints")).add(pointData);
//        }
//
//        List<Map<String, Object>> groupedResult = new ArrayList<>(deviceMap.values());
//        return ResponseDTO.ok(groupedResult);
//    }

    @Operation(summary = "查询环境数据最新一条")
    @GetMapping("/environment")
    public ResponseDTO<EnvironmentDataEntity> latestEnvironmentData(
            @Parameter(description = "设备 MAC", required = true, example = "78421CBF3C30")
            @RequestParam("mac")
            @NotBlank
            String mac) {
        return ResponseDTO.ok(environmentDataService.latestByMac(mac));
    }

    //射出机的点位
    @Operation(summary = "查询 PLC 数据点（按 sort_order 排序）")
    @GetMapping("/data/points/recent")
    public ResponseDTO<List<PlcDataPointEntity>> recentPoints(
            @Parameter(description = "设备 ID", required = true, example = "4-主磅")
            @RequestParam("deviceId")
            @NotNull
            Long deviceId) {
        return ResponseDTO.ok(plcDataPointService.listRecentPoints(deviceId));
    }

    @Operation(summary = "保存点位排序与勾选颜色")
    @PutMapping("/data/points/sort")
    public ResponseDTO<Void> updatePointSort(
            @Parameter(description = "设备 ID", required = true)
            @RequestParam("deviceId")
            @NotNull
            Long deviceId,
            @RequestBody List<PlcDataPointEntity> items) {
        plcDataPointService.batchUpdateSortOrder(deviceId, items);
        return ResponseDTO.ok();
    }

    //阈值页面的解析
    @Operation(summary = "解析 PLC 数据字段中文名称列表")
    @GetMapping("/data/resolveFieldNames")
    public ResponseDTO<List<Map<String, String>>> resolveFieldNames(
            @Parameter(description = "设备名称", required = true, example = "射出机五号机")
            @RequestParam("deviceName")
            @NotBlank
            String deviceName) {
        List<PlcDataEntity> dataList = plcDataService.listLatestSameTimestampByDeviceName(deviceName);
        List<Map<String, String>> result = dataList.stream()
                .map(PlcDataEntity::getFieldKey)
                .filter(StrUtil::isNotBlank)
                .map(PlcFieldKeyDisplayNames::extractBaseKey)
                .distinct()
                .sorted()
                .map(code -> Map.of(
                        "code", code,
                        "name", PlcFieldKeyDisplayNames.resolveOrCode(code)
                ))
                .toList();
        return ResponseDTO.ok(result);
    }
    @Operation(summary = "上传文件到 MinIO（可更新设备照片）")
    @PostMapping("/uploadToMinio")
    public ResponseDTO<Map<String, Object>> uploadFileToMinio(
            @Parameter(description = "上传目录（如 images/）", required = false)
            @RequestParam(value = "baseDir", required = false, defaultValue = "") String baseDir,
            @Parameter(description = "文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "设备ID（可选，传入则更新该设备的photo字段）", required = false)
            @RequestParam(value = "deviceId", required = false) Long deviceId) {
        
        String fileName = MinioUploadUtil.upload(baseDir, file);
        String url = MinioUploadUtil.getPublicUrl(fileName);
        
        boolean updated = false;
        if (deviceId != null) {
            updated = plcDeviceService.updateDevicePhoto(deviceId, url);
        }
        
        return ResponseDTO.ok(Map.of(
                "url", url,
                "fileName", fileName,
                "originalFilename", file.getOriginalFilename(),
                "devicePhotoUpdated", updated
        ));
    }
    //包含射出机5号的 数据
    @Operation(summary = "查询已抓取PLC数据列表")
    @GetMapping("/deviceData")
      public ResponseDTO<List<PlcDeviceEntity>> listDevicesData() {
        return ResponseDTO.ok(plcDeviceService.listAllDevicesData());
    }
//    public ResponseDTO<List<PlcDeviceEntity>> listDevicesData() {
//        log.info("调用外部PLC设备接口: {}{}", workshopBaseUrl, plcDevicePath);
//
//        SignedRestTemplateUtil signedRestTemplate = new SignedRestTemplateUtil(restTemplate, workshopApiKey, workshopApiSecret);
//        ResponseEntity<Map<String, Object>> response = signedRestTemplate.get(workshopBaseUrl, plcDevicePath, null, new ParameterizedTypeReference<Map<String, Object>>() {});
//
//        Map<String, Object> result = response.getBody();
//        List<PlcDeviceEntity> data = (List<PlcDeviceEntity>) result.get("data");
//        return ResponseDTO.ok(data);
//    }
}
