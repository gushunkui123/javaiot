package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.SensorDeviceEntity;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

/**
 * 传感器设备 Service 接口
 */
public interface SensorDeviceService extends IService<SensorDeviceEntity> {

    /**
     * 分页查询设备列表
     */
    IPage<SensorDeviceEntity> page(Page<SensorDeviceEntity> page, String deviceName);
}
