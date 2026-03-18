package com.agileboot.domain.business.workorder.db;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 工单信息表 服务实现类
 *
 * @author Codex
 */
@Service
public class BizWorkOrderServiceImpl extends ServiceImpl<BizWorkOrderMapper, BizWorkOrderEntity> implements BizWorkOrderService {

    @Override
    public boolean isWorkOrderNoDuplicated(Long workOrderId, String workOrderNo) {
        QueryWrapper<BizWorkOrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne(workOrderId != null, "work_order_id", workOrderId)
            .eq("work_order_no", workOrderNo);
        return baseMapper.exists(queryWrapper);
    }

}
