package com.factorylink.domain.business.machine;

import com.factorylink.common.config.MachineProperties;
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
import com.factorylink.infrastructure.machine.dto.request.ScalePartsRequest;
import com.factorylink.infrastructure.machine.dto.request.ScaleWorkOrderRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备下发编排服务
 * <p>
 * 负责将本地数据（原料、配方、工单）下发到主磅/微量设备。
 * 下发由用户手动触发，写入失败进行重试（3次/3秒间隔），超过失败次数记录 biz_sync_log。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScaleSyncService {

    private final MainScaleClient mainScaleClient;
    private final MicroScaleClient microScaleClient;
    private final MachineProperties machineProperties;
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
        return syncFormula(formulaId, operationType, null);
    }

    public SyncResultDTO syncFormula(Long formulaId, OperationType operationType,
            Set<DeviceType> deviceTypes) {
        BizFormulaEntity formula = formulaService.getById(formulaId);
        if (formula == null) {
            throw new IllegalArgumentException("配方不存在: " + formulaId);
        }

        boolean all = deviceTypes == null || deviceTypes.isEmpty();
        SyncResultDTO result = new SyncResultDTO();

        // 主磅
        if (all || deviceTypes.contains(DeviceType.MAIN_SCALE)) {
            result.setMainScale(syncFormulaToDevice("MAIN_SCALE", formula, operationType));
        } else {
            result.setMainScale(DeviceResult.skipped("未选择该设备"));
        }

        // 微量
        if (all || deviceTypes.contains(DeviceType.MICRO_SCALE)) {
            result.setMicroScale(syncFormulaToDevice("MICRO_SCALE", formula, operationType));
        } else {
            result.setMicroScale(DeviceResult.skipped("未选择该设备"));
        }

        return result;
    }

    private DeviceResult syncFormulaToDevice(String deviceType, BizFormulaEntity formula,
            OperationType operationType) {
        if (!isDeviceEnabled(deviceType)) {
            return DeviceResult.skipped("设备未启用");
        }

        try {
            ScaleApiResponse<Void> response;
            Object requestBody;

            if (operationType == OperationType.DELETE) {
                ScaleFormulaRequest request = formulaScaleConverter.toDeleteRequest(formula);
                requestBody = request;
                response = "MAIN_SCALE".equals(deviceType)
                    ? mainScaleClient.deleteFormula(request)
                    : microScaleClient.deleteFormula(request);
            } else {
                List<BizFormulaItemEntity> items = formulaItemService.list(
                    new LambdaQueryWrapper<BizFormulaItemEntity>()
                        .eq(BizFormulaItemEntity::getFormulaId, formula.getFormulaId())
                        .orderByAsc(BizFormulaItemEntity::getSortOrder));

                ScaleFormulaRequest request = "MAIN_SCALE".equals(deviceType)
                    ? formulaScaleConverter.toMainScaleRequest(formula, items)
                    : formulaScaleConverter.toMicroScaleRequest(formula, items);
                requestBody = request;

                response = switch (operationType) {
                    case ADD -> "MAIN_SCALE".equals(deviceType)
                        ? mainScaleClient.addFormula(request)
                        : microScaleClient.addFormula(request);
                    case UPDATE -> "MAIN_SCALE".equals(deviceType)
                        ? mainScaleClient.updateFormula(request)
                        : microScaleClient.updateFormula(request);
                    default -> throw new IllegalArgumentException("不支持的操作: " + operationType);
                };
            }

            String op = operationType.name() + "_FORMULA";
            if (response.isSuccess()) {
                saveSyncLog(deviceType, op, formula.getFormulaId(), requestBody, null, 0, 1);
                return DeviceResult.success();
            }

            saveSyncLog(deviceType, op, formula.getFormulaId(), requestBody, response.getRtnmsg(), 0, 0);
            return DeviceResult.fail(response.getRtnmsg());

        } catch (Exception e) {
            log.error("配方下发到{}失败", deviceType, e);
            saveSyncLog(deviceType, operationType.name() + "_FORMULA",
                formula.getFormulaId(), null, e.getMessage(), 3, 0);
            return DeviceResult.fail(e.getMessage());
        }
    }

    // ======================== 原料下发 ========================

    public SyncResultDTO syncMaterial(Long materialId, OperationType operationType) {
        BizMaterialEntity material = materialService.getById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("原料不存在: " + materialId);
        }

        SyncResultDTO result = new SyncResultDTO();
        result.setMainScale(syncMaterialToDevice("MAIN_SCALE", material, operationType));
        result.setMicroScale(syncMaterialToDevice("MICRO_SCALE", material, operationType));
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
        return switch (deviceType) {
            case "MAIN_SCALE" -> machineProperties.getMainScale() != null
                && machineProperties.getMainScale().isEnabled();
            case "MICRO_SCALE" -> machineProperties.getMicroScale() != null
                && machineProperties.getMicroScale().isEnabled();
            default -> false;
        };
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
