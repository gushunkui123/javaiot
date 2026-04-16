package com.factorylink.domain.business.workorder.model;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.factorylink.common.exception.ApiException;
import com.factorylink.common.exception.error.ErrorCode.Business;
import com.factorylink.domain.business.workorder.command.AddWorkOrderCommand;
import com.factorylink.domain.business.workorder.command.AssignFormulaCommand;
import com.factorylink.domain.business.workorder.command.UpdateWorkOrderCommand;
import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.factorylink.domain.business.workorder.db.BizWorkOrderService;
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

    private static final int PROCESS_STATUS_FORMULA_ASSIGNED = 2;

    private static final int PROCESS_STATUS_PRODUCING = 3;

    private static final int PROCESS_STATUS_COMPLETED = 4;

    private static final int PROCESS_STATUS_CANCELLED = 5;

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

            if (getOrderDate() == null) {
                setOrderDate(new Date());
            }

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
            checkProcessStatus(PROCESS_STATUS_CREATED);
            setFormulaId(command.getFormulaId());
            setFormulaCode(StrUtil.trim(command.getFormulaCode()));
            setProcessStatus(PROCESS_STATUS_FORMULA_ASSIGNED);
        }
    }

    public void startProduction() {
        checkProcessStatus(PROCESS_STATUS_FORMULA_ASSIGNED);
        setOrderState(ORDER_STATE_PRODUCING);
        setProcessStatus(PROCESS_STATUS_PRODUCING);
        setStartTime(new Date());
    }

    public void checkWorkOrderNoUnique() {
        if (StrUtil.isBlank(getWorkOrderNo())) {
            return;
        }
        if (workOrderService.isWorkOrderNoDuplicated(getWorkOrderId(), getWorkOrderNo())) {
            throw new ApiException(Business.WORK_ORDER_NO_IS_NOT_UNIQUE, getWorkOrderNo());
        }
    }

    public void cancel() {
        int status = getProcessStatus() != null ? getProcessStatus() : -1;
        if (status == PROCESS_STATUS_CANCELLED
                || (getOrderState() != null && getOrderState() == ORDER_STATE_CANCELLED)) {
            throw new ApiException(Business.WORK_ORDER_ALREADY_CANCELLED);
        }
        if (status < PROCESS_STATUS_CREATED || status >= PROCESS_STATUS_COMPLETED) {
            throw new ApiException(Business.WORK_ORDER_CANCEL_INVALID_STATUS);
        }
        applyCancel();
    }

    /**
     * 校验工单是否允许修改配方。
     * @param confirmed 生产中工单修改确认标识
     * @return true 表示生产中修改（需告警），false 表示普通修改
     */
    public boolean checkCanModifyFormula(Boolean confirmed) {
        int status = getProcessStatus() != null ? getProcessStatus() : -1;
        if (status == PROCESS_STATUS_FORMULA_ASSIGNED) {
            return false;
        }
        if (status == PROCESS_STATUS_PRODUCING) {
            if (!Boolean.TRUE.equals(confirmed)) {
                throw new ApiException(Business.WORK_ORDER_FORMULA_MODIFY_NEED_CONFIRM);
            }
            return true;
        }
        throw new ApiException(Business.WORK_ORDER_FORMULA_MODIFY_NOT_ALLOWED);
    }

    public void checkCanDelete() {
        int status = getProcessStatus() != null ? getProcessStatus() : -1;
        if (status == PROCESS_STATUS_PRODUCING) {
            throw new ApiException(Business.WORK_ORDER_PRODUCING_CAN_NOT_BE_DELETED);
        }
        if (status == PROCESS_STATUS_COMPLETED) {
            throw new ApiException(Business.WORK_ORDER_COMPLETED_CAN_NOT_BE_DELETED);
        }
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
