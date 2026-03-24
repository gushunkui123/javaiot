package com.factorylink.infrastructure.machine.client;

import com.factorylink.common.config.MachineProperties;
import com.factorylink.common.config.MachineProperties.ScaleConfig;
import com.factorylink.infrastructure.machine.dto.ScaleApiResponse;
import com.factorylink.infrastructure.machine.dto.request.ScaleBarcodeRequest;
import com.factorylink.infrastructure.machine.dto.request.ScaleFormulaRequest;
import com.factorylink.infrastructure.machine.dto.request.ScalePartsRequest;
import com.factorylink.infrastructure.machine.dto.request.ScaleWorkOrderRequest;
import com.factorylink.infrastructure.machine.dto.response.AlarmLogData;
import com.factorylink.infrastructure.machine.dto.response.MaterialConsumptionData;
import com.factorylink.infrastructure.machine.dto.response.WeighingRecordData;
import com.factorylink.infrastructure.machine.dto.response.WorkOrderData;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 微量（AWS）客户端
 */
@Component
@RequiredArgsConstructor
public class MicroScaleClient {

    private final ScaleApiClient scaleApiClient;
    private final MachineProperties machineProperties;

    private ScaleConfig config() {
        return machineProperties.getMicroScale();
    }

    // ======================== 写入操作 ========================

    public ScaleApiResponse<Void> addParts(ScalePartsRequest request) {
        request.setOption("addParts");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> updateParts(ScalePartsRequest request) {
        request.setOption("updateParts");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> deleteParts(ScalePartsRequest request) {
        request.setOption("deleteParts");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> addFormula(ScaleFormulaRequest request) {
        request.setOption("addFormula");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> updateFormula(ScaleFormulaRequest request) {
        request.setOption("updateFormula");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> deleteFormula(ScaleFormulaRequest request) {
        request.setOption("deleteFormula");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> addWorkOrder(ScaleWorkOrderRequest request) {
        request.setOption("addWorkOrder");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> updateWorkOrder(ScaleWorkOrderRequest request) {
        request.setOption("updateWorkOrder");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> deleteWorkOrder(ScaleWorkOrderRequest request) {
        request.setOption("deleteWorkOrder");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> addMaterialBarcode(ScaleBarcodeRequest request) {
        request.setOption("addMaterialBarcode");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> updateMaterialBarcode(ScaleBarcodeRequest request) {
        request.setOption("updateMaterialBarcode");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    public ScaleApiResponse<Void> deleteMaterialBarcode(ScaleBarcodeRequest request) {
        request.setOption("deleteMaterialBarcode");
        request.setMachineId(config().getMachineId());
        request.setPlant(config().getPlant());
        return scaleApiClient.write(config().getApiWriteUrl(), request);
    }

    // ======================== 查询操作 ========================

    public ScaleApiResponse<WorkOrderData> getWorkOrder(String workOrderNo, String formulaCode,
            String orderDateFrom, String orderDateTo, String orderState) {
        Map<String, String> params = baseQueryParams("getWorkOrder");
        putIfNotEmpty(params, "workOrderNo", workOrderNo);
        putIfNotEmpty(params, "formulaCode", formulaCode);
        putIfNotEmpty(params, "orderDateFrom", orderDateFrom);
        putIfNotEmpty(params, "orderDateTo", orderDateTo);
        putIfNotEmpty(params, "orderState", orderState);
        return scaleApiClient.query(config().getApiUrl(), params, WorkOrderData.class);
    }

    public ScaleApiResponse<WeighingRecordData> getWeighingRecord(String workOrderNo, String formulaCode,
            String weighTimeFrom, String weighTimeTo) {
        Map<String, String> params = baseQueryParams("getWeighingRecord");
        putIfNotEmpty(params, "workOrderNo", workOrderNo);
        putIfNotEmpty(params, "formulaCode", formulaCode);
        params.put("weighTimeFrom", weighTimeFrom);
        params.put("weighTimeTo", weighTimeTo);
        return scaleApiClient.query(config().getApiUrl(), params, WeighingRecordData.class);
    }

    public ScaleApiResponse<AlarmLogData> getAlarmLog(String alarmGroup,
            String alarmTimeFrom, String alarmTimeTo) {
        Map<String, String> params = baseQueryParams("getAlarmLog");
        putIfNotEmpty(params, "alarmGroup", alarmGroup);
        params.put("alarmTimeFrom", alarmTimeFrom);
        params.put("alarmTimeTo", alarmTimeTo);
        return scaleApiClient.query(config().getApiUrl(), params, AlarmLogData.class);
    }

    public ScaleApiResponse<MaterialConsumptionData> getMaterialConsumption(String workOrderNo,
            String materialNo, String consTimeFrom, String consTimeTo) {
        Map<String, String> params = baseQueryParams("getMaterialConsumption");
        putIfNotEmpty(params, "workOrderNo", workOrderNo);
        putIfNotEmpty(params, "materialNo", materialNo);
        params.put("consTimeFrom", consTimeFrom);
        params.put("consTimeTo", consTimeTo);
        return scaleApiClient.query(config().getApiUrl(), params, MaterialConsumptionData.class);
    }

    // ======================== 内部方法 ========================

    private Map<String, String> baseQueryParams(String option) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("option", option);
        params.put("plant", config().getPlant());
        params.put("machineId", String.valueOf(config().getMachineId()));
        return params;
    }

    private void putIfNotEmpty(Map<String, String> params, String key, String value) {
        if (value != null && !value.isEmpty()) {
            params.put(key, value);
        }
    }
}
