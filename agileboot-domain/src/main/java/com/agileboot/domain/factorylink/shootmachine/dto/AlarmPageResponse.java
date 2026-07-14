package com.agileboot.domain.factorylink.shootmachine.dto;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import java.util.List;
import lombok.Data;

/** 未处理报警分页响应 */
@Data
public class AlarmPageResponse {

    private List<ShootRuleAlarmEntity> items;

    private long total;
}
