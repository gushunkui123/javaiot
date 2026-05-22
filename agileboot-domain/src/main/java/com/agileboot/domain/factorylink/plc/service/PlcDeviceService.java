package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDeviceEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PlcDeviceService extends IService<PlcDeviceEntity> {

    List<PlcDeviceEntity> listAll();
}
