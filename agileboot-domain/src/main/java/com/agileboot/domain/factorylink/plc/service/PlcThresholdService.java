package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcThresholdEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PlcThresholdService extends IService<PlcThresholdEntity> {

    /**
     * 根据设备名称查询阈值配置
     */
    PlcThresholdEntity getByDeviceName(String deviceName);
}
