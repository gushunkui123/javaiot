package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.collection.CollUtil;
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
    public List<PlcDataPointEntity> listRecentPoints(Long deviceId) {
        if (deviceId == null) {
            return List.of();
        }
        return baseMapper.selectListByDeviceId(deviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateSortOrder(Long deviceId, List<PlcDataPointEntity> items) {
        if (deviceId == null || CollUtil.isEmpty(items)) {
            return;
        }
        List<PlcDataPointEntity> validItems = items.stream()
                .filter(item -> item.getId() != null && item.getSortOrder() != null)
                .toList();
        if (!validItems.isEmpty()) {
            baseMapper.batchUpdateSortAndMark(validItems);
        }
    }
}
