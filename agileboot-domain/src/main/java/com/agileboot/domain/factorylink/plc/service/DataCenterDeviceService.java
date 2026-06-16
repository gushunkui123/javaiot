package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.DataCenterDeviceEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface DataCenterDeviceService extends IService<DataCenterDeviceEntity> {

    List<DataCenterDeviceEntity> listAll();

    List<DataCenterDeviceEntity> listByWorkshopId(Long workshopId);

    List<DataCenterDeviceEntity> listByAreaId(Long areaId);

    List<DataCenterDeviceEntity> searchByKeyword(String keyword);
}
