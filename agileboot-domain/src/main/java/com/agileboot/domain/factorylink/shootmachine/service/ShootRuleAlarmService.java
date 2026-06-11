package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface ShootRuleAlarmService extends IService<ShootRuleAlarmEntity> {

    /** 查询所有未处理的报警（关联机器/站位/模具信息） */
    List<ShootRuleAlarmEntity> listUnhandledWithRelation(Long machineId);

    /** 按ID查询报警 */
    ShootRuleAlarmEntity getByIdOrThrow(Long id);

    /** 根据站位号查询报警详情 */
    Map<String, Object> getDetailByStationNo(Integer stationNo);

    /** 创建报警记录（含去重逻辑） */
    ShootRuleAlarmEntity create(ShootRuleAlarmEntity entity);


    /** 根据站位号和字段名称批量处理报警 */
    void handleByStationNoAndField(Integer stationNo, String fieldName, String handleRemark);

    /** 统计概览：返回总数和报警数 */
    Map<String, Long> getStatisticsOverview(Long machineId);

    /** 根据 PLC 数据检测并创建报警 */
    void detectAndCreateAlarms(Long machineId, String jsonPayload);
}
