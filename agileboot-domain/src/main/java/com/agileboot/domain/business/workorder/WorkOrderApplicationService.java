package com.agileboot.domain.business.workorder;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.common.command.BulkOperationCommand;
import com.agileboot.domain.common.audit.AuditUserEnricher;
import com.agileboot.domain.business.workorder.command.AddWorkOrderCommand;
import com.agileboot.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.agileboot.domain.business.workorder.db.BizWorkOrderEntity;
import com.agileboot.domain.business.workorder.db.BizWorkOrderService;
import com.agileboot.domain.business.workorder.dto.WorkOrderDTO;
import com.agileboot.domain.business.workorder.model.WorkOrderModel;
import com.agileboot.domain.business.workorder.model.WorkOrderModelFactory;
import com.agileboot.domain.business.workorder.query.WorkOrderQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class WorkOrderApplicationService {

    private final WorkOrderModelFactory workOrderModelFactory;

    private final BizWorkOrderService workOrderService;

    private final AuditUserEnricher auditUserEnricher;

    public PageDTO<WorkOrderDTO> getWorkOrderList(WorkOrderQuery query) {
        Page<BizWorkOrderEntity> page = workOrderService.page(query.toPage(), query.toQueryWrapper());
        List<WorkOrderDTO> records = page.getRecords().stream().map(WorkOrderDTO::new).toList();
        auditUserEnricher.enrich(records);
        return new PageDTO<>(records, page.getTotal());
    }

    public List<WorkOrderDTO> getWorkOrderListAll(WorkOrderQuery query) {
        List<BizWorkOrderEntity> all = workOrderService.list(query.toQueryWrapper());
        List<WorkOrderDTO> records = all.stream().map(WorkOrderDTO::new).toList();
        auditUserEnricher.enrich(records);
        return records;
    }

    public WorkOrderDTO getWorkOrderInfo(Long workOrderId) {
        WorkOrderModel model = workOrderModelFactory.loadById(workOrderId);
        WorkOrderDTO dto = new WorkOrderDTO(model);
        auditUserEnricher.enrich(dto);
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public void addWorkOrder(AddWorkOrderCommand addCommand) {
        WorkOrderModel workOrderModel = workOrderModelFactory.create();
        workOrderModel.loadFromAddCommand(addCommand);
        workOrderModel.checkWorkOrderNoUnique();
        workOrderModel.insert();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateWorkOrder(UpdateWorkOrderCommand updateCommand) {
        WorkOrderModel workOrderModel = workOrderModelFactory.loadById(updateCommand.getWorkOrderId());
        workOrderModel.loadFromUpdateCommand(updateCommand);
        workOrderModel.checkWorkOrderNoUnique();
        workOrderModel.updateById();
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkOrder(BulkOperationCommand<Long> deleteCommand) {
        workOrderService.removeBatchByIds(deleteCommand.getIds());
    }

}
