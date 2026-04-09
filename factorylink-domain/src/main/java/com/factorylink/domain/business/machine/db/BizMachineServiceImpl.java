package com.factorylink.domain.business.machine.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

}
