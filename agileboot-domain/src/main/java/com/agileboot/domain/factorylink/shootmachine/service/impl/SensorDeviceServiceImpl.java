package com.agileboot.domain.factorylink.shootmachine.service.impl;

import com.agileboot.domain.factorylink.shootmachine.entity.SensorDeviceEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.SensorDeviceMapper;
import com.agileboot.domain.factorylink.shootmachine.service.SensorDeviceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

/**
 * 传感器设备 Service 实现类
 */
@Service
@DS("slave")
public class SensorDeviceServiceImpl extends ServiceImpl<SensorDeviceMapper, SensorDeviceEntity>
        implements SensorDeviceService {

    @Override
    public IPage<SensorDeviceEntity> page(Page<SensorDeviceEntity> page, String deviceName) {
        LambdaQueryWrapper<SensorDeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (deviceName != null && !deviceName.isEmpty()) {
            queryWrapper.like(SensorDeviceEntity::getDeviceName, deviceName);
        }
        return baseMapper.selectPage(page, queryWrapper);
    }
}
