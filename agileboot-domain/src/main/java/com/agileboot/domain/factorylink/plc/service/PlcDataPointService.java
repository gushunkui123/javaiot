package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PlcDataPointService extends IService<PlcDataPointEntity> {

    /**
     * 按展示名找到  最新的一条，再取同一设备、同一秒内的全部数据点，按 id 升序。
     */
    List<PlcDataPointEntity> listLatestSameSecondByDisplayName(String displayName);
}
