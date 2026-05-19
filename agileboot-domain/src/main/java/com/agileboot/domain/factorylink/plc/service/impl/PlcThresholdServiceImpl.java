package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcThresholdEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcThresholdMapper;
import com.agileboot.domain.factorylink.plc.service.PlcThresholdService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlcThresholdServiceImpl extends ServiceImpl<PlcThresholdMapper, PlcThresholdEntity> implements PlcThresholdService {

    @Override
    public PlcThresholdEntity getByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return null;
        }
        return lambdaQuery()
                .eq(PlcThresholdEntity::getDeviceName, deviceName.trim())
                .one();
    }
}
