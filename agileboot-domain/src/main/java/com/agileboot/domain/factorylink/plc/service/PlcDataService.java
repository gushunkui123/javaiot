package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PlcDataService extends IService<PlcDataEntity> {

    /**
     * 将 MQTT 上一条 JSON 对象拆成多行写入 plc_data。
     *
     * @param deviceName 设备名称（
     * @param jsonPayload JSON 字符串
     */
    void ingestFlatJsonTelemetry(String deviceName, String jsonPayload);

    /**
     * 按设备返回「最新一次采集」的全部字段行：取该设备最大的 时间，返回该时间下所有行。
     */
    List<PlcDataEntity> listLatestSameTimestampByDeviceName(String deviceName);
}
