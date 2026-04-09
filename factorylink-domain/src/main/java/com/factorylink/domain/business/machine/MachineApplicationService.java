package com.factorylink.domain.business.machine;

import com.factorylink.common.core.page.PageDTO;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.business.machine.command.AddMachineCommand;
import com.factorylink.domain.business.machine.command.UpdateMachineCommand;
import com.factorylink.domain.business.machine.db.BizMachineEntity;
import com.factorylink.domain.business.machine.db.BizMachineService;
import com.factorylink.domain.business.machine.dto.MachineDTO;
import com.factorylink.domain.business.machine.model.MachineModel;
import com.factorylink.domain.business.machine.model.MachineModelFactory;
import com.factorylink.domain.business.machine.query.MachineQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备管理应用服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MachineApplicationService {

    private final MachineModelFactory machineModelFactory;

    private final BizMachineService machineService;

    private final AuditUserEnricher auditUserEnricher;

    public PageDTO<MachineDTO> getMachineList(MachineQuery query) {
        Page<BizMachineEntity> page = machineService.page(query.toPage(), query.toQueryWrapper());
        List<MachineDTO> records = page.getRecords().stream().map(MachineDTO::new).toList();
        auditUserEnricher.enrich(records);
        return new PageDTO<>(records, page.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addMachine(AddMachineCommand addCommand) {
        MachineModel machineModel = machineModelFactory.create();
        machineModel.loadFromAddCommand(addCommand);
        machineModel.checkMachineCodeUnique();
        machineModel.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMachine(UpdateMachineCommand updateCommand) {
        MachineModel machineModel = machineModelFactory.loadById(updateCommand.getMachineId());
        machineModel.loadFromUpdateCommand(updateCommand);
        machineModel.checkMachineCodeUnique();
        machineModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteMachine(BulkOperationCommand<Long> deleteCommand) {
        machineService.removeBatchByIds(deleteCommand.getIds());
    }

}
