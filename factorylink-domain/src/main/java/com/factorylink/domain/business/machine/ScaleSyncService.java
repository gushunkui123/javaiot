package com.factorylink.domain.business.machine;

import com.factorylink.common.config.MachineConfigProvider;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.External;
import com.factorylink.domain.business.formula.db.BizFormulaEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemEntity;
import com.factorylink.domain.business.formula.db.BizFormulaItemService;
import com.factorylink.domain.business.formula.db.BizFormulaService;
import com.factorylink.domain.business.machine.converter.FormulaScaleConverter;
import com.factorylink.domain.business.machine.converter.MaterialScaleConverter;
import com.factorylink.domain.business.machine.converter.WorkOrderScaleConverter;
import com.factorylink.domain.business.machine.db.BizSyncLogEntity;
import com.factorylink.domain.business.machine.db.BizSyncLogService;
import com.factorylink.domain.business.machine.dto.SyncResultDTO;
import com.factorylink.domain.business.machine.dto.SyncResultDTO.DeviceResult;
import com.factorylink.domain.business.material.db.BizMaterialEntity;
import com.factorylink.domain.business.material.db.BizMaterialService;
import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.factorylink.domain.business.workorder.db.BizWorkOrderService;
import com.factorylink.infrastructure.machine.client.MainScaleClient;
import com.factorylink.infrastructure.machine.client.MicroScaleClient;
import com.factorylink.infrastructure.machine.dto.ScaleApiResponse;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaRequest;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaRequest.FormulaEntry;
import com.factorylink.infrastructure.machine.dto.request.ScalePartsRequest;
import com.factorylink.infrastructure.machine.dto.request.ScaleWorkOrderRequest;
import com.factorylink.infrastructure.machine.dto.response.MaterialInBucketData;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备下发编排服务
 * <p>
 * 负责将本地数据（原料、配方、工单）下发到主磅/微量设备。
 * 下发支持用户手动触发，也支持在部分业务流程中自动触发。
 * 写入失败进行重试（3次/3秒间隔），超过失败次数记录 biz_sync_log。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScaleSyncService {

    private final MainScaleClient mainScaleClient;
    private final MicroScaleClient microScaleClient;
    private final MachineConfigProvider machineConfigProvider;
    private final BizSyncLogService syncLogService;
    private final ObjectMapper objectMapper;

    // Converter
    private final FormulaScaleConverter formulaScaleConverter;
    private final MaterialScaleConverter materialScaleConverter;
    private final WorkOrderScaleConverter workOrderScaleConverter;

    // DB Service
    private final BizFormulaService formulaService;
    private final BizFormulaItemService formulaItemService;
    private final BizMaterialService materialService;
    private final BizWorkOrderService workOrderService;

    // ======================== 配方下发 ========================

    public SyncResultDTO syncFormula(Long formulaId, OperationType operationType) {
        BizFormulaEntity formula = formulaService.getById(formulaId);
        if (formula == null) {
            throw new IllegalArgumentException("配方不存在: " + formulaId);
        }

        // 校验两台设备都已启用且在线
        validateDeviceReady("MAIN_SCALE");
        validateDeviceReady("MICRO_SCALE");

        if (operationType == OperationType.DELETE) {
            return deleteFormulaFromBothDevices(formula);
        } else {
            return addOrUpdateFormula(formula);
        }
    }

    private SyncResultDTO deleteFormulaFromBothDevices(BizFormulaEntity formula) {
        deleteFormulaOrIgnoreNotFound("MAIN_SCALE", formula);
        deleteFormulaOrIgnoreNotFound("MICRO_SCALE", formula);

        saveSyncLog("MAIN_SCALE", "DELETE_FORMULA", formula.getFormulaId(), null, null, 0, 1);
        saveSyncLog("MICRO_SCALE", "DELETE_FORMULA", formula.getFormulaId(), null, null, 0, 1);

        SyncResultDTO result = new SyncResultDTO();
        result.setMainScale(DeviceResult.success());
        result.setMicroScale(DeviceResult.success());
        return result;
    }

    private SyncResultDTO addOrUpdateFormula(BizFormulaEntity formula) {
        // 加载配方明细
        List<BizFormulaItemEntity> items = formulaItemService.list(
            new LambdaQueryWrapper<BizFormulaItemEntity>()
                .eq(BizFormulaItemEntity::getFormulaId, formula.getFormulaId())
                .orderByAsc(BizFormulaItemEntity::getSortOrder));

        // 构建主磅和微量请求
        ScaleFormulaRequest mainRequest = formulaScaleConverter.toMainScaleRequest(formula, items);
        ScaleFormulaRequest microRequest = formulaScaleConverter.toMicroScaleRequest(formula, items);

        // 先删除旧配方
        deleteFormulaOrIgnoreNotFound("MAIN_SCALE", formula);
        deleteFormulaOrIgnoreNotFound("MICRO_SCALE", formula);

        // 校验料桶中的原料匹配
        validateMaterialsInBuckets("MAIN_SCALE", mainRequest.getFormulaEntryList());
        validateMaterialsInBuckets("MICRO_SCALE", microRequest.getFormulaEntryList());

        // 为主料机器添加配方名对应的原料（material_name 和 material_code 均为配方名，类型为主料）
        ScalePartsRequest formulaNameParts = new ScalePartsRequest();
        formulaNameParts.setPlant("");
        formulaNameParts.setPartNo(formula.getFormulaName());
        formulaNameParts.setPartName(formula.getFormulaName());
        formulaNameParts.setPartClass("5");
        mainScaleClient.addParts(formulaNameParts);

        // 为主磅请求添加配方名条目
        formulaScaleConverter.addFormulaNameEntry(mainRequest, formula);

        // 下发配方到两台设备
        ScaleApiResponse<Void> mainResp = mainScaleClient.addFormula(mainRequest);
        if (!mainResp.isSuccess()) {
            saveSyncLog("MAIN_SCALE", "ADD_FORMULA", formula.getFormulaId(), mainRequest, mainResp.getRtnmsg(), 0, 0);
            throw new ApiException(External.SCALE_SYNC_FAILED, "主磅: " + mainResp.getRtnmsg());
        }
        saveSyncLog("MAIN_SCALE", "ADD_FORMULA", formula.getFormulaId(), mainRequest, null, 0, 1);

        ScaleApiResponse<Void> microResp = microScaleClient.addFormula(microRequest);
        if (!microResp.isSuccess()) {
            saveSyncLog("MICRO_SCALE", "ADD_FORMULA", formula.getFormulaId(), microRequest, microResp.getRtnmsg(), 0, 0);
            throw new ApiException(External.SCALE_SYNC_FAILED, "微量: " + microResp.getRtnmsg());
        }
        saveSyncLog("MICRO_SCALE", "ADD_FORMULA", formula.getFormulaId(), microRequest, null, 0, 1);

        SyncResultDTO result = new SyncResultDTO();
        result.setMainScale(DeviceResult.success());
        result.setMicroScale(DeviceResult.success());
        return result;
    }

    private void validateDeviceReady(String deviceType) {
        if (!machineConfigProvider.isDeviceEnabled(deviceType)) {
            throw new ApiException(External.SCALE_DEVICE_NOT_ENABLED, deviceType);
        }
        if (!machineConfigProvider.isDeviceOnline(deviceType)) {
            throw new ApiException(External.SCALE_DEVICE_OFFLINE, deviceType);
        }
    }

    private void deleteFormulaOrIgnoreNotFound(String deviceType, BizFormulaEntity formula) {
        ScaleFormulaRequest request = formulaScaleConverter.toDeleteRequest(formula);
        ScaleApiResponse<Void> response = "MAIN_SCALE".equals(deviceType)
            ? mainScaleClient.deleteFormula(request)
            : microScaleClient.deleteFormula(request);

        if (response.isSuccess()) {
            return;
        }
        if (response.getRtnmsg() != null && response.getRtnmsg().contains("配方编号不存在")) {
            return;
        }
        throw new ApiException(External.SCALE_DELETE_BEFORE_SYNC_FAILED,
            deviceType + ": " + response.getRtnmsg());
    }

    private void validateMaterialsInBuckets(String deviceType, List<FormulaEntry> formulaEntries) {
        if (formulaEntries == null || formulaEntries.isEmpty()) {
            return;
        }

        ScaleApiResponse<MaterialInBucketData> bucketResponse = "MAIN_SCALE".equals(deviceType)
            ? mainScaleClient.getMaterialInBuckets()
            : microScaleClient.getMaterialInBuckets();

        if (!bucketResponse.isSuccess()) {
            throw new ApiException(External.SCALE_BUCKET_QUERY_FAILED,
                deviceType + ": " + bucketResponse.getRtnmsg());
        }

        Set<String> materialsInBuckets = bucketResponse.getRtndata() == null
            ? Set.of()
            : bucketResponse.getRtndata().stream()
                .map(MaterialInBucketData::getMaterialNo)
                .collect(Collectors.toSet());

        List<String> missingMaterials = formulaEntries.stream()
            .filter(e -> e.getMaterialNo() != null && !materialsInBuckets.contains(e.getMaterialNo()))
            .map(FormulaEntry::getMaterialNo)
            .toList();

        if (!missingMaterials.isEmpty()) {
            throw new ApiException(External.SCALE_MATERIAL_NOT_IN_BUCKET,
                deviceType + " - " + String.join(", ", missingMaterials));
        }
    }

    // ======================== 原料下发 ========================

    public SyncResultDTO syncMaterial(Long materialId, OperationType operationType) {
        return syncMaterial(materialId, operationType, null);
    }

    public SyncResultDTO syncMaterial(Long materialId, OperationType operationType,
            Set<DeviceType> deviceTypes) {
        BizMaterialEntity material = materialService.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("原料不存在: " + materialId);
        }

        boolean all = deviceTypes == null || deviceTypes.isEmpty();
        SyncResultDTO result = new SyncResultDTO();

        if (all || deviceTypes.contains(DeviceType.MAIN_SCALE)) {
            result.setMainScale(syncMaterialToDevice("MAIN_SCALE", material, operationType));
        } else {
            result.setMainScale(DeviceResult.skipped("未选择该设备"));
        }

        if (all || deviceTypes.contains(DeviceType.MICRO_SCALE)) {
            result.setMicroScale(syncMaterialToDevice("MICRO_SCALE", material, operationType));
        } else {
            result.setMicroScale(DeviceResult.skipped("未选择该设备"));
        }
        return result;
    }

    private DeviceResult syncMaterialToDevice(String deviceType, BizMaterialEntity material,
            OperationType operationType) {
        if (!isDeviceEnabled(deviceType)) {
            return DeviceResult.skipped("设备未启用");
        }

        try {
            ScaleApiResponse<Void> response;
            Object requestBody;

            if (operationType == OperationType.DELETE) {
                ScalePartsRequest request = materialScaleConverter.toDeleteRequest(material);
                requestBody = request;
                response = "MAIN_SCALE".equals(deviceType)
                    ? mainScaleClient.deleteParts(request)
                    : microScaleClient.deleteParts(request);
            } else {
                ScalePartsRequest request = materialScaleConverter.toRequest(material);
                requestBody = request;
                response = switch (operationType) {
                    case ADD -> "MAIN_SCALE".equals(deviceType)
                        ? mainScaleClient.addParts(request)
                        : microScaleClient.addParts(request);
                    case UPDATE -> "MAIN_SCALE".equals(deviceType)
                        ? mainScaleClient.updateParts(request)
                        : microScaleClient.updateParts(request);
                    default -> throw new IllegalArgumentException("不支持的操作: " + operationType);
                };
            }

            String op = operationType.name() + "_MATERIAL";
            if (response.isSuccess()) {
                saveSyncLog(deviceType, op, material.getMaterialId(), requestBody, null, 0, 1);
                return DeviceResult.success();
            }

            saveSyncLog(deviceType, op, material.getMaterialId(), requestBody, response.getRtnmsg(), 0, 0);
            return DeviceResult.fail(response.getRtnmsg());

        } catch (Exception e) {
            log.error("原料下发到{}失败", deviceType, e);
            saveSyncLog(deviceType, operationType.name() + "_MATERIAL",
                material.getMaterialId(), null, e.getMessage(), 3, 0);
            return DeviceResult.fail(e.getMessage());
        }
    }

    // ======================== 工单下发 ========================

    public SyncResultDTO syncWorkOrder(Long workOrderId, OperationType operationType) {
        BizWorkOrderEntity workOrder = workOrderService.getById(workOrderId);
        if (workOrder == null) {
            throw new IllegalArgumentException("工单不存在: " + workOrderId);
        }

        SyncResultDTO result = new SyncResultDTO();
        result.setMainScale(syncWorkOrderToDevice("MAIN_SCALE", workOrder, operationType));
        result.setMicroScale(syncWorkOrderToDevice("MICRO_SCALE", workOrder, operationType));
        return result;
    }

    private DeviceResult syncWorkOrderToDevice(String deviceType, BizWorkOrderEntity workOrder,
            OperationType operationType) {
        if (!isDeviceEnabled(deviceType)) {
            return DeviceResult.skipped("设备未启用");
        }

        try {
            ScaleApiResponse<Void> response;
            Object requestBody;

            if (operationType == OperationType.DELETE) {
                ScaleWorkOrderRequest request = workOrderScaleConverter.toDeleteRequest(workOrder);
                requestBody = request;
                response = "MAIN_SCALE".equals(deviceType)
                    ? mainScaleClient.deleteWorkOrder(request)
                    : microScaleClient.deleteWorkOrder(request);
            } else {
                ScaleWorkOrderRequest request = workOrderScaleConverter.toRequest(workOrder);
                requestBody = request;
                response = switch (operationType) {
                    case ADD -> "MAIN_SCALE".equals(deviceType)
                        ? mainScaleClient.addWorkOrder(request)
                        : microScaleClient.addWorkOrder(request);
                    case UPDATE -> "MAIN_SCALE".equals(deviceType)
                        ? mainScaleClient.updateWorkOrder(request)
                        : microScaleClient.updateWorkOrder(request);
                    default -> throw new IllegalArgumentException("不支持的操作: " + operationType);
                };
            }

            String op = operationType.name() + "_WORK_ORDER";
            if (response.isSuccess()) {
                saveSyncLog(deviceType, op, workOrder.getWorkOrderId(), requestBody, null, 0, 1);
                return DeviceResult.success();
            }

            saveSyncLog(deviceType, op, workOrder.getWorkOrderId(), requestBody, response.getRtnmsg(), 0, 0);
            return DeviceResult.fail(response.getRtnmsg());

        } catch (Exception e) {
            log.error("工单下发到{}失败", deviceType, e);
            saveSyncLog(deviceType, operationType.name() + "_WORK_ORDER",
                workOrder.getWorkOrderId(), null, e.getMessage(), 3, 0);
            return DeviceResult.fail(e.getMessage());
        }
    }

    // ======================== 通用方法 ========================

    private boolean isDeviceEnabled(String deviceType) {
        return machineConfigProvider.isDeviceEnabled(deviceType);
    }

    private void saveSyncLog(String deviceType, String operation, Long targetId,
            Object requestBody, String errorMsg, int retryCount, int status) {
        try {
            BizSyncLogEntity logEntity = new BizSyncLogEntity();
            logEntity.setDeviceType(deviceType);
            logEntity.setOperation(operation);
            logEntity.setTargetId(targetId);
            if (requestBody != null) {
                logEntity.setRequestBody(objectMapper.writeValueAsString(requestBody));
            }
            logEntity.setErrorMsg(errorMsg != null && errorMsg.length() > 500
                ? errorMsg.substring(0, 500) : errorMsg);
            logEntity.setRetryCount(retryCount);
            logEntity.setStatus(status);
            logEntity.setCreateTime(new Date());
            syncLogService.save(logEntity);
        } catch (Exception e) {
            log.error("保存同步日志失败", e);
        }
    }

    /**
     * 操作类型枚举
     */
    public enum OperationType {
        ADD, UPDATE, DELETE
    }

    /**
     * 设备类型枚举
     */
    public enum DeviceType {
        /** 主磅 */
        MAIN_SCALE,
        /** 微量 */
        MICRO_SCALE
    }
}
