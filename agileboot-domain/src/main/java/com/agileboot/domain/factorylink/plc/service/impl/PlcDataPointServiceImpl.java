package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataPointMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataPointService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@DS("slave")
@Service
public class PlcDataPointServiceImpl extends ServiceImpl<PlcDataPointMapper, PlcDataPointEntity>
        implements PlcDataPointService {

    @Override
    public List<PlcDataPointEntity> listAllByDeviceName(String deviceName) {
        if (StrUtil.isBlank(deviceName)) {
            return List.of();
        }
        return baseMapper.selectListByDeviceName(deviceName.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSortOrder(String deviceName, List<PlcDataPointEntity> items) {
        if (StrUtil.isBlank(deviceName) || CollUtil.isEmpty(items)) {
            return;
        }
        items.forEach(item -> {
            if (item.getId() == null || item.getSortOrder() == null) {
                return;
            }
            // markColor 允许 null，用于取消勾选时清空
            baseMapper.updateSortAndMarkById(item.getId(), item.getSortOrder(), item.getMarkColor());
        });
    }
}
