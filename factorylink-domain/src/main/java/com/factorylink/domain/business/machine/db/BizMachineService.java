package com.factorylink.domain.business.machine.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 设备信息表 服务类
 */
public interface BizMachineService extends IService<BizMachineEntity> {

    /**
     * 校验设备编码是否重复
     */
    boolean isMachineCodeDuplicated(Long machineId, String machineCode);

    /**
     * 根据设备编码查询
     */
    BizMachineEntity getByMachineCode(String machineCode);

}
