package com.agileboot.domain.factorylink.shootmachine.service.impl;

import com.agileboot.domain.factorylink.shootmachine.entity.SensorDataItemEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.SensorDataItemMapper;
import com.agileboot.domain.factorylink.shootmachine.service.SensorDataItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

/**
 * 数据小项 Service 实现类
 */
@Service
@DS("slave")
public class SensorDataItemServiceImpl extends ServiceImpl<SensorDataItemMapper, SensorDataItemEntity>
        implements SensorDataItemService {
}
