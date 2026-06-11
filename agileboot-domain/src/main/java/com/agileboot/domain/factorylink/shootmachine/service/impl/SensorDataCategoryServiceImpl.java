package com.agileboot.domain.factorylink.shootmachine.service.impl;

import com.agileboot.domain.factorylink.shootmachine.entity.SensorDataCategoryEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.SensorDataCategoryMapper;
import com.agileboot.domain.factorylink.shootmachine.service.SensorDataCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;

/**
 * 数据分类 Service 实现类
 */
@Service
@DS("slave")
public class SensorDataCategoryServiceImpl extends ServiceImpl<SensorDataCategoryMapper, SensorDataCategoryEntity>
        implements SensorDataCategoryService {
}
