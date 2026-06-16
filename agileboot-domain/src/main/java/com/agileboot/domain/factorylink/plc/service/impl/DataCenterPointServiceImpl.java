package com.agileboot.domain.factorylink.plc.service.impl;

import com.agileboot.domain.factorylink.plc.entity.DataCenterPointEntity;
import com.agileboot.domain.factorylink.plc.mapper.DataCenterPointMapper;
import com.agileboot.domain.factorylink.plc.service.DataCenterPointService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

@DS("data_center")
@Service
public class DataCenterPointServiceImpl extends ServiceImpl<DataCenterPointMapper, DataCenterPointEntity>
        implements DataCenterPointService {

    @Override
    public List<DataCenterPointEntity> listByDeviceId(Long deviceId) {
        if (deviceId == null) {
            return List.of();
        }
        return baseMapper.selectByDeviceId(deviceId);
    }

    @Override
    public List<DataCenterPointEntity> listByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        return baseMapper.selectByCategoryId(categoryId);
    }
}
