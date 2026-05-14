package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PlcDataPointService extends IService<PlcDataPointEntity> {

    /**
     *
     * id 升序。
     */
    List<PlcDataPointEntity> listAllByDeviceName(String deviceName);
}
