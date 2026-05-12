package com.agileboot.domain.factorylink.plc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.agileboot.domain.factorylink.plc.mapper.PlcDataPointMapper;
import com.agileboot.domain.factorylink.plc.service.PlcDataPointService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

@DS("slave")
@Service
public class PlcDataPointServiceImpl extends ServiceImpl<PlcDataPointMapper, PlcDataPointEntity>
        implements PlcDataPointService {

    @Override
    public List<PlcDataPointEntity> listLatestSameSecondByDisplayName(String displayName) {
        if (StrUtil.isBlank(displayName)) {
            return List.of();
        }
        PlcDataPointEntity anchor =
                lambdaQuery()
                        .eq(PlcDataPointEntity::getDisplayName, displayName.trim())
                        .orderByDesc(PlcDataPointEntity::getUpdatedAt)
                        .orderByDesc(PlcDataPointEntity::getId)
                        .last("LIMIT 1")
                        .one();
        if (anchor == null || anchor.getUpdatedAt() == null || anchor.getDeviceId() == null) {
            return List.of();
        }
        // 去掉毫秒即「这一秒」的左闭右开区间 [from, to)
        long ms = anchor.getUpdatedAt().getTime();
        long secondStart = ms - ms % 1000L;
        Date from = new Date(secondStart);
        Date toExclusive = new Date(secondStart + 1000L);
        return lambdaQuery()
                .eq(PlcDataPointEntity::getDeviceId, anchor.getDeviceId())
                .ge(PlcDataPointEntity::getUpdatedAt, from)
                .lt(PlcDataPointEntity::getUpdatedAt, toExclusive)
                .orderByAsc(PlcDataPointEntity::getId)
                .list();
    }
}
