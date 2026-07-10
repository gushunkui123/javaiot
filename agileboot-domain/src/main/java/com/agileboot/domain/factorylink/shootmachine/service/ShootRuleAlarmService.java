package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmExportDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface ShootRuleAlarmService extends IService<ShootRuleAlarmEntity> {

    /** 查询所有未处理的报警（关联机器/站位/模具信息） */
    List<ShootRuleAlarmEntity> listUnhandledWithRelation(Long machineId);

    /** 查询所有报警（包含已处理和未处理），用于导出Excel */
    List<ShootRuleAlarmExportDTO> listAllForExport(Long machineId, Integer days);

    /** 按ID查询报警 */
    ShootRuleAlarmEntity getByIdOrThrow(Long id);

    /** 根据站位号查询报警详情 */
    Map<String, Object> getDetailByStationNo(Integer stationNo);

    /** 创建报警记录（含去重逻辑） */
    ShootRuleAlarmEntity create(ShootRuleAlarmEntity entity);


    /** 根据报警ID列表批量处理报警 */
    void handleByStationIdAndField(List<Long> ids, String handleRemark);

    /** 统计概览：返回总数和报警数 */
    Map<String, Long> getStatisticsOverview(Long machineId);

    /** 根据 PLC 数据检测并创建报警（MQTT推送时调用，含模具规则检测） */
    void detectAndCreateAlarms(Long machineId, String jsonPayload);

    /** 根据 plc_data_latest 表数据检测黄色/红色报警（第三方接口同步后调用） */
    void detectAlarmsByPlcData(Long machineId);
}
