package com.factorylink.domain.business.machine;

import com.factorylink.common.config.MachineProperties;
import com.factorylink.infrastructure.machine.client.MainScaleClient;
import com.factorylink.infrastructure.machine.client.MicroScaleClient;
import com.factorylink.infrastructure.machine.dto.ScaleApiResponse;
import com.factorylink.infrastructure.machine.dto.response.AlarmLogData;
import com.factorylink.infrastructure.machine.dto.response.MaterialConsumptionData;
import com.factorylink.infrastructure.machine.dto.response.ProcessData;
import com.factorylink.infrastructure.machine.dto.response.WeighingRecordData;
import com.factorylink.infrastructure.machine.dto.response.WorkOrderData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 设备数据实时查询服务
 */
@Service
@RequiredArgsConstructor
public class ScaleQueryService {

    private final MainScaleClient mainScaleClient;
    private final MicroScaleClient microScaleClient;
    private final MachineProperties machineProperties;

    // ======================== 主磅查询 ========================

    public ScaleApiResponse<WorkOrderData> queryMainScaleWorkOrder(
            String workOrderNo, String formulaCode,
            String orderDateFrom, String orderDateTo, String orderState) {
        return mainScaleClient.getWorkOrder(workOrderNo, formulaCode,
            orderDateFrom, orderDateTo, orderState);
    }

    public ScaleApiResponse<WeighingRecordData> queryMainScaleWeighingRecord(
            String workOrderNo, String formulaCode,
            String weighTimeFrom, String weighTimeTo) {
        return mainScaleClient.getWeighingRecord(workOrderNo, formulaCode,
            weighTimeFrom, weighTimeTo);
    }

    public ScaleApiResponse<AlarmLogData> queryMainScaleAlarmLog(
            String alarmGroup, String alarmTimeFrom, String alarmTimeTo) {
        return mainScaleClient.getAlarmLog(alarmGroup, alarmTimeFrom, alarmTimeTo);
    }

    public ScaleApiResponse<MaterialConsumptionData> queryMainScaleMaterialConsumption(
            String workOrderNo, String materialNo,
            String consTimeFrom, String consTimeTo) {
        return mainScaleClient.getMaterialConsumption(workOrderNo, materialNo,
            consTimeFrom, consTimeTo);
    }

    public ScaleApiResponse<ProcessData> queryMainScaleProcessData(
            String workOrderNo, Integer batch) {
        return mainScaleClient.getProcessData(workOrderNo, batch);
    }

    // ======================== 微量查询 ========================

    public ScaleApiResponse<WorkOrderData> queryMicroScaleWorkOrder(
            String workOrderNo, String formulaCode,
            String orderDateFrom, String orderDateTo, String orderState) {
        return microScaleClient.getWorkOrder(workOrderNo, formulaCode,
            orderDateFrom, orderDateTo, orderState);
    }

    public ScaleApiResponse<WeighingRecordData> queryMicroScaleWeighingRecord(
            String workOrderNo, String formulaCode,
            String weighTimeFrom, String weighTimeTo) {
        return microScaleClient.getWeighingRecord(workOrderNo, formulaCode,
            weighTimeFrom, weighTimeTo);
    }

    public ScaleApiResponse<AlarmLogData> queryMicroScaleAlarmLog(
            String alarmGroup, String alarmTimeFrom, String alarmTimeTo) {
        return microScaleClient.getAlarmLog(alarmGroup, alarmTimeFrom, alarmTimeTo);
    }

    public ScaleApiResponse<MaterialConsumptionData> queryMicroScaleMaterialConsumption(
            String workOrderNo, String materialNo,
            String consTimeFrom, String consTimeTo) {
        return microScaleClient.getMaterialConsumption(workOrderNo, materialNo,
            consTimeFrom, consTimeTo);
    }
}
