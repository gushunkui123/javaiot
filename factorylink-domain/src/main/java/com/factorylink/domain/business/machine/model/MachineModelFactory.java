package com.factorylink.domain.business.machine.model;

import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.machine.db.BizMachineEntity;
import com.factorylink.domain.business.machine.db.BizMachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 设备模型工厂
 */
@Component
@RequiredArgsConstructor
public class MachineModelFactory {

    private final BizMachineService machineService;

    public MachineModel loadById(Long machineId) {
        BizMachineEntity byId = machineService.getById(machineId);
        if (byId == null) {
            throw new ApiException(Business.COMMON_OBJECT_NOT_FOUND, machineId, "设备");
        }
        return new MachineModel(byId, machineService);
    }

    public MachineModel create() {
        return new MachineModel(machineService);
    }

}
