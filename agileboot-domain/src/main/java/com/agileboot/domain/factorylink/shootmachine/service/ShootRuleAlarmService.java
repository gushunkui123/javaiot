package com.agileboot.domain.factorylink.shootmachine.service;

import com.agileboot.domain.factorylink.plc.dto.MoldRulePushDTO;
import com.agileboot.domain.factorylink.plc.dto.RulePushItem;
import com.agileboot.domain.factorylink.shootmachine.dto.AlarmPageResponse;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.AlarmExportResult;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface ShootRuleAlarmService extends IService<ShootRuleAlarmEntity> {

    /** 查询所有未处理的报警（关联机器/站位/模具信息） */
    List<ShootRuleAlarmEntity> listUnhandledWithRelation(Long machineId);

    /** 聚合查询未处理报警：按机器+站位分组汇总红/黄数量 */
    List<Map<String, Object>> listUnhandledSummary(Long machineId);

    /** 分页查询未处理报警明细（可按机器+站位过滤） */
    AlarmPageResponse listUnhandledPaged(Long machineId, Long stationId, long page, long pageSize);

    /** 查询所有报警（包含已处理和未处理），用于导出Excel；按红/黄分成两部分 */
    AlarmExportResult listAllForExport(Long machineId, Integer days);

    /** 按ID查询报警 */
    ShootRuleAlarmEntity getByIdOrThrow(Long id);

    /** 根据站位号查询报警详情 */
    Map<String, Object> getDetailByStationNo(Integer stationNo);

    /** 根据报警ID列表批量处理报警 */
    void handleByStationIdAndField(List<Long> ids, String handleRemark);

    /** 统计概览：返回总数和报警数 */
    Map<String, Long> getStatisticsOverview(Long machineId);

    /** 根据 PLC 数据检测并创建报警（MQTT推送时调用，含模具规则检测） */
    void detectAndCreateAlarms(Long machineId, String jsonPayload);

    /** 根据 plc_data_latest 表数据检测黄色/红色报警（第三方接口同步后调用） */
    void detectAlarmsByPlcData(Long machineId);

    /** 构建下发给第三方的告警规则报文：按(deviceCode, 模具)分组的数组，每组含设备编码、模具ID与规则列表(datacode + max/min)。
     *  按当前在产排期遍历，将规则的 field_code 解析为完整 field_key 后，
     *  从 plc_data_latest 定位出第三方唯一点位码 datacode 与设备编码 device_code。machineId 为 null 时遍历全部启用机台。 */
    List<MoldRulePushDTO> buildRulePushPayload(Long machineId);
}
