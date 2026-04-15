package com.factorylink.domain.business.machine;

import com.factorylink.common.config.MachineConfigProvider;
import com.factorylink.common.config.ScaleMachineConfig;
import com.factorylink.domain.business.machine.db.BizMachineEntity;
import com.factorylink.domain.business.machine.db.BizMachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 设备配置提供者实现（从数据库读取设备配置）
 */
@Component
@RequiredArgsConstructor
public class MachineConfigProviderImpl implements MachineConfigProvider {

    private final BizMachineService machineService;

    @Override
    public ScaleMachineConfig getMainScale() {
        return toScaleConfig(getEnabledMachine("MAIN_SCALE"));
    }

    @Override
    public ScaleMachineConfig getMicroScale() {
        return toScaleConfig(getEnabledMachine("MICRO_SCALE"));
    }

    @Override
    public boolean isDeviceEnabled(String machineCode) {
        return getEnabledMachine(machineCode) != null;
    }

    @Override
    public boolean isDeviceOnline(String machineCode) {
        BizMachineEntity machine = machineService.getByMachineCode(machineCode);
        return machine != null && Boolean.TRUE.equals(machine.getOnlineStatus());
    }

    private BizMachineEntity getEnabledMachine(String machineCode) {
        BizMachineEntity machine = machineService.getByMachineCode(machineCode);
        if (machine == null || !Boolean.TRUE.equals(machine.getEnabled())) {
            return null;
        }
        return machine;
    }

    private ScaleMachineConfig toScaleConfig(BizMachineEntity machine) {
        if (machine == null) {
            return null;
        }
        ScaleMachineConfig config = new ScaleMachineConfig();
        config.setScaleMachineId(machine.getScaleMachineId());
        config.setPlant(machine.getPlant());
        config.setApiUrl(machine.getApiUrl());
        config.setApiWriteUrl(machine.getApiWriteUrl());
        return config;
    }

}
