package com.agileboot.domain.factorylink.plc.service;

import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface PlcDataPointService extends IService<PlcDataPointEntity> {

    List<PlcDataPointEntity> listRecentPoints(Long deviceId);

    /** 批量更新点位 sortOrder、markColor取消勾选 markColor 传 null）。 */
    void batchUpdateSortOrder(Long deviceId, List<PlcDataPointEntity> items);
}
