package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDeviceEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * PLC设备连接信息 Service 接口
 */
public interface PlcDeviceService extends IService<PlcDeviceEntity> {

    /**
     * 查询所有PLC设备
     */
    List<PlcDeviceEntity> listAllDevices();

    /**
     * 根据设备ID查询点位数据
     */
    List<Map<String, Object>> listDataPointsByDeviceId(Long deviceId);

    /**
     * 查询设备及其点位数据（可选按设备ID过滤）
     */
    List<Map<String, Object>> listDevicesWithDataPoints(Long deviceId);

    /**
     * 更新设备照片
     */
    boolean updateDevicePhoto(Long deviceId, String photoUrl);
}
