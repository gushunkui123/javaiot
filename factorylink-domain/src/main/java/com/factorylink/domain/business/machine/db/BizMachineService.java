package com.factorylink.domain.business.machine.db;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

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

    /**
     * 查询所有启用的设备
     */
    List<BizMachineEntity> listEnabled();

    /**
     * 更新设备在线状态
     */
    void updateOnlineStatus(Long machineId, boolean online);

}
