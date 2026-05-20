package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface PlcDataService extends IService<PlcDataEntity> {

    /**
     * 将 MQTT 上一条 JSON 对象拆成多行写入 plc_data。
     *
     * @param machineId 机台 ID，可为 null（仅写 device_name）
     * @param deviceName 设备名称
     * @param jsonPayload JSON 字符串
     */
    void ingestFlatJsonTelemetry(Long machineId, String deviceName, String jsonPayload);

    /** 按 device_name 取该次采集时间下的全部字段行。 */
    List<PlcDataEntity> listLatestSameTimestampByDeviceName(String deviceName);

    /** 按 machine_id 取该次采集时间下的全部字段行。 */
    List<PlcDataEntity> listLatestSameTimestampByMachineId(Long machineId);

    /** 批量查询各 machine_id 最新采集时间。 */
    Map<Long, Date> mapLatestDataTimestampByMachineIds(Collection<Long> machineIds);
}
