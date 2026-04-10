package com.factorylink.domain.business.machine.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 设备信息表 服务实现类
 */
@Service
public class BizMachineServiceImpl extends ServiceImpl<BizMachineMapper, BizMachineEntity> implements BizMachineService {

    @Override
    public boolean isMachineCodeDuplicated(Long machineId, String machineCode) {
        QueryWrapper<BizMachineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(machineId != null, "machine_id", machineId)
            .eq("machine_code", machineCode);
        return baseMapper.exists(queryWrapper);
    }

    @Override
    public BizMachineEntity getByMachineCode(String machineCode) {
        return lambdaQuery().eq(BizMachineEntity::getMachineCode, machineCode).one();
    }

    @Override
    public List<BizMachineEntity> listEnabled() {
        return lambdaQuery().eq(BizMachineEntity::getEnabled, true).list();
    }

    @Override
    public void updateOnlineStatus(Long machineId, boolean online) {
        lambdaUpdate()
            .eq(BizMachineEntity::getMachineId, machineId)
            .set(BizMachineEntity::getOnlineStatus, online)
            .set(BizMachineEntity::getLastCheckTime, new Date())
            .update();
    }

}
