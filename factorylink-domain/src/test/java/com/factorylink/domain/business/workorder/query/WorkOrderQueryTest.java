package com.factorylink.domain.business.workorder.query;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.factorylink.domain.business.workorder.db.BizWorkOrderEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.Test;

class WorkOrderQueryTest {

    @Test
    void toQueryWrapperShouldIncludeProcessStatusCondition() {
        WorkOrderQuery query = new WorkOrderQuery();
        query.setProcessStatus(2);

        QueryWrapper<BizWorkOrderEntity> wrapper = query.toQueryWrapper();

        assertTrue(wrapper.getSqlSegment().contains("process_status"));
    }
}
