package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataPointMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataPointService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@DS("slave")
@Service
@RequiredArgsConstructor
public class PlcDataPointServiceImpl extends ServiceImpl<PlcDataPointMapper, PlcDataPointEntity>
        implements PlcDataPointService {

    @Override
    public List<PlcDataPointEntity> listAllByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return List.of();
        }
        return baseMapper.selectListByDeviceName(deviceName.trim());
    }
}
