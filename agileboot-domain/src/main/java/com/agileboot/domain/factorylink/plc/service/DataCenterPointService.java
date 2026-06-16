package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.DataCenterPointEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface DataCenterPointService extends IService<DataCenterPointEntity> {

    /** 按设备ID查询数据点列表 */
    List<DataCenterPointEntity> listByDeviceId(Long deviceId);

    /** 按分类ID查询数据点列表 */
    List<DataCenterPointEntity> listByCategoryId(Long categoryId);
}
