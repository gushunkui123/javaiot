package com.agileboot.domain.factorylink.plc.service.impl;

import com.agileboot.domain.factorylink.plc.entity.PlcDeviceEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDeviceMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDeviceService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

@DS("slave")
@Service
public class PlcDeviceServiceImpl extends ServiceImpl<PlcDeviceMapper, PlcDeviceEntity>
        implements PlcDeviceService {

    @Override
    public List<PlcDeviceEntity> listAll() {
        return lambdaQuery().orderByAsc(PlcDeviceEntity::getId).list();
    }
}
