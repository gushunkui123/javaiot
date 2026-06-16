package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.DataCenterDeviceEntity;
import com.agileboot.domain.factorylink.plc.mapper.DataCenterDeviceMapper;
import com.agileboot.domain.factorylink.plc.service.DataCenterDeviceService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import org.springframework.stereotype.Service;

@DS("data_center")
@Service
public class DataCenterDeviceServiceImpl extends ServiceImpl<DataCenterDeviceMapper, DataCenterDeviceEntity>
        implements DataCenterDeviceService {

    @Override
    public List<DataCenterDeviceEntity> listAll() {
        return baseMapper.selectAll();
    }

    @Override
    public List<DataCenterDeviceEntity> listByWorkshopId(Long workshopId) {
        if (workshopId == null) {
            return List.of();
        }
        return baseMapper.selectByWorkshopId(workshopId);
    }

    @Override
    public List<DataCenterDeviceEntity> listByAreaId(Long areaId) {
        if (areaId == null) {
            return List.of();
        }
        return baseMapper.selectByAreaId(areaId);
    }

    @Override
    public List<DataCenterDeviceEntity> searchByKeyword(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return List.of();
        }
        return baseMapper.selectByKeyword(keyword);
    }
}
