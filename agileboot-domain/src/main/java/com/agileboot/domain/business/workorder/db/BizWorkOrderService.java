package com.agileboot.domain.business.workorder.db;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 工单信息表 服务类
 *
 * @author Codex
 */
public interface BizWorkOrderService extends IService<BizWorkOrderEntity> {

    /**
     * 校验工单编号是否重复
     *
     * @param workOrderId 工单ID
     * @param workOrderNo 工单编号
     * @return 是否重复
     */
    boolean isWorkOrderNoDuplicated(Long workOrderId, String workOrderNo);

}
