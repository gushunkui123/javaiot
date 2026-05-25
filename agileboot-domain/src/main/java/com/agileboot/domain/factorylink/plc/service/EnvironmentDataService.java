package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.EnvironmentDataEntity;
import com.baomidou.mybatisplus.extension.service.IService;

public interface EnvironmentDataService extends IService<EnvironmentDataEntity> {

    void ingest(String topic, String jsonPayload);

    EnvironmentDataEntity latestByMac(String mac);
}
