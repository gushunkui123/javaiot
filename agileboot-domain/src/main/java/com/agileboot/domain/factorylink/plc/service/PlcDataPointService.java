package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PlcDataPointService extends IService<PlcDataPointEntity> {

    /**
     *
     *  用主表的名称得到id查询数据表中全部字段
     */
    List<PlcDataPointEntity> listAllByDeviceName(String deviceName);
}
