package com.agileboot.domain.factorylink.plc.service.impl;

import com.agileboot.domain.factorylink.plc.entity.PlcDeviceEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDeviceMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.plc.service.PlcDeviceService;
import com.agileboot.domain.factorylink.plc.util.PlcFieldKeyDisplayNames;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PLC设备连接信息 Service 实现类
 */
@Service
@RequiredArgsConstructor
public class PlcDeviceServiceImpl extends ServiceImpl<PlcDeviceMapper, PlcDeviceEntity>
        implements PlcDeviceService {

    private final PlcDataService plcDataService;
    @DS("slave")
    @Override
    public List<PlcDeviceEntity> listAllDevices() {
        return list().stream().filter(device->device.getId()!=8L || device.getId()==null).toList();
    }

    @DS("slave")
    @Override
    public List<Map<String, Object>> listDataPointsByDeviceId(Long deviceId) {
        return baseMapper.selectDataPointsByDeviceId(deviceId);
    }

    @DS("slave")
    @Override
    public List<Map<String, Object>> listDevicesWithDataPoints(Long deviceId) {
        // 获取设备信息
        List<Map<String, Object>> devices = baseMapper.selectDevices(deviceId);
        
        for (Map<String, Object> device : devices) {
            BigInteger idBigInt = (BigInteger) device.get("id");
            Long id = idBigInt != null ? idBigInt.longValue() : null;
            
            // 如果deviceId为8，查询最新射出机5号机数据
            if (id != null && id == 8) {
                String deviceName = (String) device.get("deviceName");
                if (deviceName != null) {
                    List<PlcDataEntity> dataList = plcDataService.listLatestSameTimestampByDeviceName(deviceName);
                    
                    List<Map<String, Object>> dataPoints = new ArrayList<>();
                    for (PlcDataEntity data : dataList) {
                        Map<String, Object> point = new HashMap<>();
                        // 解析为中文名称
                        point.put("displayName", PlcFieldKeyDisplayNames.resolveWithStationNo(data.getFieldKey()));
                        point.put("currentValue", data.getFieldValue());
                        dataPoints.add(point);
                    }
                    device.put("dataPoints", dataPoints);
                }
            } else {
                // 其他deviceId使用原有逻辑
                device.put("dataPoints", baseMapper.selectDataPointsByDeviceId(id));
            }
        }

        return devices;
    }

    @Override
    public boolean updateDevicePhoto(Long deviceId, String photoUrl) {
        PlcDeviceEntity device = getById(deviceId);
        if (device != null) {
            device.setPhoto(photoUrl);
            return updateById(device);
        }
        return false;
    }

    @DS("slave")
    @Override
    public List<PlcDeviceEntity> listAllDevicesData() {
        return list();
    }
}
