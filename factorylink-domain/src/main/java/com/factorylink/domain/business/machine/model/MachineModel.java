package com.factorylink.domain.business.machine.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.machine.command.AddMachineCommand;
import com.factorylink.domain.business.machine.command.UpdateMachineCommand;
import com.factorylink.domain.business.machine.db.BizMachineEntity;
import com.factorylink.domain.business.machine.db.BizMachineService;
import lombok.NoArgsConstructor;

/**
 * 设备领域模型
 */
@NoArgsConstructor
public class MachineModel extends BizMachineEntity {

    private BizMachineService machineService;

    public MachineModel(BizMachineService machineService) {
        this.machineService = machineService;
    }

    public MachineModel(BizMachineEntity entity, BizMachineService machineService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.machineService = machineService;
    }

    public void loadFromAddCommand(AddMachineCommand addCommand) {
        if (addCommand != null) {
            BeanUtil.copyProperties(addCommand, this, "machineId");
            setMachineCode(StrUtil.trim(getMachineCode()));
            setMachineName(StrUtil.trim(getMachineName()));
        }
    }

    public void loadFromUpdateCommand(UpdateMachineCommand updateCommand) {
        if (updateCommand != null) {
            loadFromAddCommand(updateCommand);
        }
    }

    public void checkMachineCodeUnique() {
        if (machineService.isMachineCodeDuplicated(getMachineId(), getMachineCode())) {
            throw new ApiException(Business.MACHINE_CODE_IS_NOT_UNIQUE, getMachineCode());
        }
    }

}
