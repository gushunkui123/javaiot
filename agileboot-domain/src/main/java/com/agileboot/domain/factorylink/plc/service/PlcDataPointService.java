package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PlcDataPointService extends IService<PlcDataPointEntity> {

    /** 用设备名称查询全部点位：先 sort_order 升序，再按 mark_color 分组。 */
    List<PlcDataPointEntity> listAllByDeviceName(String deviceName);

    /** 批量更新点位 sortOrder、markColor取消勾选 markColor 传 null）。 */
    void batchUpdateSortOrder(String deviceName, List<PlcDataPointEntity> items);
}
