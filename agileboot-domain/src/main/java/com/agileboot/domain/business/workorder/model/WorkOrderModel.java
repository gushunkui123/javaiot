package com.agileboot.domain.business.workorder.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.agileboot.common.exception.ApiException;
import com.agileboot.common.exception.error.ErrorCode.Business;
import com.agileboot.domain.business.workorder.command.AddWorkOrderCommand;
import com.agileboot.domain.business.workorder.command.AssignFormulaCommand;
import com.agileboot.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.agileboot.domain.business.workorder.db.BizWorkOrderEntity;
import com.agileboot.domain.business.workorder.db.BizWorkOrderService;
import java.math.BigDecimal;
import java.util.Date;
import lombok.NoArgsConstructor;

/**
 * @author Codex
 */
@NoArgsConstructor
public class WorkOrderModel extends BizWorkOrderEntity {

    private static final BigDecimal DEFAULT_BATCH_WEIGHT = new BigDecimal("75");

    private static final int PROCESS_STATUS_CREATED = 1;

    private static final int PROCESS_STATUS_CONFIRMED = 2;

    private static final int PROCESS_STATUS_FORMULA_ASSIGNED = 3;

    private static final int PROCESS_STATUS_PRODUCING = 4;

    private static final int PROCESS_STATUS_COMPLETED = 5;

    private static final int PROCESS_STATUS_CANCELLED = 6;

    private static final int ORDER_STATE_PRODUCING = 2;

    private static final int ORDER_STATE_CANCELLED = 4;

    private BizWorkOrderService workOrderService;

    public WorkOrderModel(BizWorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    public WorkOrderModel(BizWorkOrderEntity entity, BizWorkOrderService workOrderService) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
        this.workOrderService = workOrderService;
    }

    public void loadFromAddCommand(AddWorkOrderCommand addCommand) {
        if (addCommand != null) {
            BeanUtil.copyProperties(addCommand, this, "workOrderId");
            setWorkOrderNo(StrUtil.trim(getWorkOrderNo()));
            setFormulaCode(StrUtil.trim(getFormulaCode()));

            if (getBatchWeight() == null) {
                setBatchWeight(DEFAULT_BATCH_WEIGHT);
            }
            setOrderWeight(getBatchWeight().multiply(new BigDecimal(getOrderBatchNum())));

            if (getProcessStatus() == null) {
                setProcessStatus(PROCESS_STATUS_CREATED);
            }
        }
    }

    public void loadFromUpdateCommand(UpdateWorkOrderCommand updateCommand) {
        if (updateCommand != null) {
            loadFromAddCommand(updateCommand);
        }
    }

    public void assignFormula(AssignFormulaCommand command) {
        if (command != null) {
            checkProcessStatus(PROCESS_STATUS_CONFIRMED);
            setFormulaId(command.getFormulaId());
            setFormulaCode(StrUtil.trim(command.getFormulaCode()));
            setProcessStatus(PROCESS_STATUS_FORMULA_ASSIGNED);
        }
    }

    public void confirm() {
        checkProcessStatus(PROCESS_STATUS_CREATED);
        setProcessStatus(PROCESS_STATUS_CONFIRMED);
    }

    public void startProduction() {
        checkProcessStatus(PROCESS_STATUS_FORMULA_ASSIGNED);
        setOrderState(ORDER_STATE_PRODUCING);
        setProcessStatus(PROCESS_STATUS_PRODUCING);
        setStartTime(new Date());
    }

    public void checkWorkOrderNoUnique() {
        if (workOrderService.isWorkOrderNoDuplicated(getWorkOrderId(), getWorkOrderNo())) {
            throw new ApiException(Business.WORK_ORDER_NO_IS_NOT_UNIQUE, getWorkOrderNo());
        }
    }

    public void cancel() {
        int status = getProcessStatus() != null ? getProcessStatus() : -1;
        if (status < PROCESS_STATUS_CREATED || status >= PROCESS_STATUS_COMPLETED) {
            throw new ApiException(Business.WORK_ORDER_CANCEL_INVALID_STATUS);
        }
        applyCancel();
    }

    private void checkProcessStatus(int expectedStatus) {
        if (getProcessStatus() == null || getProcessStatus() != expectedStatus) {
            throw new ApiException(Business.WORK_ORDER_PROCESS_STATUS_INVALID);
        }
    }

    private void applyCancel() {
        setOrderState(ORDER_STATE_CANCELLED);
        setProcessStatus(PROCESS_STATUS_CANCELLED);
    }

}
