package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.agileboot.domain.factorylink.plc.entity.PlcDeviceEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataPointMapper;
import com.agileboot.domain.factorylink.plc.mapper.PlcDeviceMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataPointService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@DS("slave")
@Service
@RequiredArgsConstructor
public class PlcDataPointServiceImpl extends ServiceImpl<PlcDataPointMapper, PlcDataPointEntity>
        implements PlcDataPointService {

    private final PlcDeviceMapper plcDeviceMapper;

    @Override
    public List<PlcDataPointEntity> listAllByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return List.of();
        }
        PlcDeviceEntity device =
                plcDeviceMapper.selectOne(
                        Wrappers.<PlcDeviceEntity>lambdaQuery()
                                .eq(PlcDeviceEntity::getDeviceName, deviceName.trim())
                                .orderByAsc(PlcDeviceEntity::getId)
                                .last("LIMIT 1"));
        if (device == null || device.getId() == null) {
            return List.of();
        }
        return lambdaQuery()
                .eq(PlcDataPointEntity::getDeviceId, device.getId())
                .orderByAsc(PlcDataPointEntity::getId)
                .list();
    }
}
