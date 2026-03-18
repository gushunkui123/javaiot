package com.agileboot.domain.business.workorder;

import com.agileboot.common.core.page.PageDTO;
import com.agileboot.domain.common.command.BulkOperationCommand;
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

/**
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class WorkOrderApplicationService {

    private final WorkOrderModelFactory workOrderModelFactory;

    private final BizWorkOrderService workOrderService;

    public PageDTO<WorkOrderDTO> getWorkOrderList(WorkOrderQuery query) {
        Page<BizWorkOrderEntity> page = workOrderService.page(query.toPage(), query.toQueryWrapper());
        List<WorkOrderDTO> records = page.getRecords().stream().map(WorkOrderDTO::new).toList();
        return new PageDTO<>(records, page.getTotal());
    }

    public WorkOrderDTO getWorkOrderInfo(Long workOrderId) {
        WorkOrderModel model = workOrderModelFactory.loadById(workOrderId);
        return new WorkOrderDTO(model);
    }

    public void addWorkOrder(AddWorkOrderCommand addCommand) {
        WorkOrderModel workOrderModel = workOrderModelFactory.create();
        workOrderModel.loadFromAddCommand(addCommand);
        workOrderModel.checkWorkOrderNoUnique();
        workOrderModel.insert();
    }

    public void updateWorkOrder(UpdateWorkOrderCommand updateCommand) {
        WorkOrderModel workOrderModel = workOrderModelFactory.loadById(updateCommand.getWorkOrderId());
        workOrderModel.loadFromUpdateCommand(updateCommand);
        workOrderModel.checkWorkOrderNoUnique();
        workOrderModel.updateById();
    }

    public void deleteWorkOrder(BulkOperationCommand<Long> deleteCommand) {
        workOrderService.removeBatchByIds(deleteCommand.getIds());
    }

}
