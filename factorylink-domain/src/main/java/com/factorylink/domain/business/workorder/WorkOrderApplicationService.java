package com.factorylink.domain.business.workorder;

import com.factorylink.common.core.page.PageDTO;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.common.audit.AuditUserEnricher;
import com.factorylink.domain.common.command.BulkOperationCommand;
import com.factorylink.domain.business.workorder.command.AddWorkOrderCommand;
import com.factorylink.domain.business.workorder.command.AssignFormulaCommand;
import com.factorylink.domain.business.workorder.command.ModifyWorkOrderFormulaCommand;
import com.factorylink.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.factorylink.domain.business.workorder.db.BizWorkOrderService;
import com.factorylink.domain.business.workorder.dto.WorkOrderDTO;
import com.factorylink.domain.business.workorder.model.WorkOrderModel;
import com.factorylink.domain.business.workorder.model.WorkOrderModelFactory;
import com.factorylink.domain.business.workorder.query.WorkOrderQuery;
import com.factorylink.domain.business.formula.model.FormulaModel;
import com.factorylink.domain.business.formula.model.FormulaModelFactory;
import com.factorylink.domain.business.machine.ScaleSyncService;
import com.factorylink.domain.business.machine.ScaleSyncService.OperationType;
import com.factorylink.domain.business.machine.dto.SyncResultDTO;
import com.factorylink.domain.business.notification.event.FormulaModifiedEvent;
import com.factorylink.domain.business.notification.event.WorkOrderCreatedEvent;
import com.factorylink.infrastructure.sse.SseMessageLevel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Codex
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderApplicationService {

    private final WorkOrderModelFactory workOrderModelFactory;

    private final BizWorkOrderService workOrderService;

    private final AuditUserEnricher auditUserEnricher;

    private final ScaleSyncService scaleSyncService;

    private final FormulaModelFactory formulaModelFactory;

    private final ApplicationEventPublisher applicationEventPublisher;

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

        if (StrUtil.isNotBlank(workOrderModel.getEmail())) {
            applicationEventPublisher.publishEvent(new WorkOrderCreatedEvent(
                this,
                workOrderModel.getWorkOrderNo(),
                workOrderModel.getLineNo(),
                workOrderModel.getMoldCode(),
                workOrderModel.getModelColor(),
                workOrderModel.getOrderBatchNum(),
                workOrderModel.getEmail()
            ));
        }
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
        for (Long workOrderId : deleteCommand.getIds()) {
            WorkOrderModel workOrderModel = workOrderModelFactory.loadById(workOrderId);
            workOrderModel.checkCanDelete();
            workOrderModel.deleteById();
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignFormula(AssignFormulaCommand command) {
        WorkOrderModel model = workOrderModelFactory.loadById(command.getWorkOrderId());
        model.assignFormula(command);
        model.updateById();
    }

    public List<WorkOrderDTO> getDispatchedWorkOrders() {
        QueryWrapper<BizWorkOrderEntity> wrapper = new QueryWrapper<BizWorkOrderEntity>()
            .eq("process_status", 2)
            .eq("deleted", 0)
            .orderByDesc("create_time");
        List<BizWorkOrderEntity> list = workOrderService.list(wrapper);
        List<WorkOrderDTO> records = list.stream().map(WorkOrderDTO::new).toList();
        auditUserEnricher.enrich(records);
        return records;
    }

    @Transactional(rollbackFor = Exception.class)
    public SyncResultDTO startProduction(Long workOrderId) {
        WorkOrderModel model = workOrderModelFactory.loadById(workOrderId);
        model.startProduction();
        model.updateById();

        // 先下发配方
        Long formulaId = model.getFormulaId();
        if (formulaId != null) {
            try {
                scaleSyncService.syncFormula(formulaId, OperationType.ADD);
            } catch (Exception e) {
                log.warn("配方下发失败，formulaId={}，后续需重试: {}", formulaId, e.getMessage());
            }
        }

        // 再下发工单
        SyncResultDTO syncResult = null;
        try {
            syncResult = scaleSyncService.syncWorkOrder(workOrderId, OperationType.ADD);
        } catch (Exception e) {
            log.warn("工单下发失败，工单ID={}，后续需重试: {}", workOrderId, e.getMessage());
        }
        return syncResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelWorkOrder(Long workOrderId) {
        WorkOrderModel model = workOrderModelFactory.loadById(workOrderId);
        model.cancel();
        model.updateById();
    }

    public SyncResultDTO syncWorkOrderWithFormula(Long workOrderId, OperationType operationType) {
        BizWorkOrderEntity workOrder = workOrderService.getById(workOrderId);

        // 先下发配方（DELETE操作时不下发配方）
        Long formulaId = workOrder.getFormulaId();
        if (formulaId != null && operationType != OperationType.DELETE) {
            try {
                scaleSyncService.syncFormula(formulaId, operationType);
            } catch (Exception e) {
                log.warn("配方下发失败，formulaId={}，后续需重试: {}", formulaId, e.getMessage());
            }
        }

        // 再下发工单
        return scaleSyncService.syncWorkOrder(workOrderId, operationType);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyWorkOrderFormula(ModifyWorkOrderFormulaCommand command) {
        WorkOrderModel workOrder = workOrderModelFactory.loadById(command.getWorkOrderId());
        boolean isProducing = workOrder.checkCanModifyFormula(command.getConfirmed());

        Long formulaId = workOrder.getFormulaId();
        if (formulaId == null) {
            throw new ApiException(Business.WORK_ORDER_NO_FORMULA_ASSIGNED);
        }

        FormulaModel formula = formulaModelFactory.loadById(formulaId);
        formula.setItems(command.getItems());
        formula.updateById();

        try {
            scaleSyncService.syncFormula(formulaId, OperationType.UPDATE);
        } catch (Exception e) {
            log.warn("配方下发失败，formulaId={}，后续需重试: {}", formulaId, e.getMessage());
        }

        SseMessageLevel level = isProducing ? SseMessageLevel.ALERT : SseMessageLevel.NOTIFICATION;
        applicationEventPublisher.publishEvent(
                new FormulaModifiedEvent(this, command.getWorkOrderId(), formulaId, level));
    }

}
