package com.agileboot.domain.factorylink.shootmachine.mapper;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShootRuleAlarmMapper extends BaseMapper<ShootRuleAlarmEntity> {

    @Select(
            "<script>" +
            "SELECT a.id, a.machine_id AS machineId, a.station_id AS stationId, a.mold_id AS moldId, a.rule_id AS ruleId, "
                    + "a.field_code AS fieldCode, a.field_name AS fieldName, a.min_value AS `minValue`, a.max_value AS `maxValue`, "
                    + "a.current_value AS currentValue, a.alarm_time AS alarmTime, "
                    + "a.handle_status AS handleStatus, a.handle_remark AS handleRemark, a.created_at AS createdAt, "
                    + "a.updated_at AS updatedAt, a.deleted, "
                    + "m.machine_name AS machineName, s.station_name AS stationName, mo.mold_model AS moldModel, mo.color AS moldColor "
                    + "FROM shoot_rule_alarm a "
                    + "LEFT JOIN shoot_machine m ON a.machine_id = m.id AND m.deleted = 0 "
                    + "LEFT JOIN shoot_machine_station s ON a.station_id = s.id AND s.deleted = 0 "
                    + "LEFT JOIN shoot_mold mo ON a.mold_id = mo.id AND mo.deleted = 0 "
                    + "WHERE a.deleted = 0 AND a.handle_status = 'false' "
                    + "<if test='machineId != null'>AND a.machine_id = #{machineId}</if> "
                    + "ORDER BY a.alarm_time DESC" +
            "</script>")
    List<ShootRuleAlarmEntity> selectUnhandledListWithRelation(@Param("machineId") Long machineId);

    @Select(
            "SELECT a.id, a.machine_id AS machineId, a.station_id AS stationId, a.mold_id AS moldId, a.rule_id AS ruleId, "
                    + "a.field_code AS fieldCode, a.field_name AS fieldName, a.min_value AS `minValue`, a.max_value AS `maxValue`, "
                    + "a.current_value AS currentValue, a.alarm_time AS alarmTime, "
                    + "a.handle_status AS handleStatus, a.handle_remark AS handleRemark, a.created_at AS createdAt, "
                    + "a.updated_at AS updatedAt, a.deleted, "
                    + "m.machine_name AS machineName, s.station_name AS stationName, mo.mold_model AS moldModel, mo.color AS moldColor "
                    + "FROM shoot_rule_alarm a "
                    + "LEFT JOIN shoot_machine m ON a.machine_id = m.id AND m.deleted = 0 "
                    + "LEFT JOIN shoot_machine_station s ON a.station_id = s.id AND s.deleted = 0 "
                    + "LEFT JOIN shoot_mold mo ON a.mold_id = mo.id AND mo.deleted = 0 "
                    + "WHERE a.deleted = 0 AND a.station_id = #{stationId} "
                    + "ORDER BY a.alarm_time DESC")
    List<ShootRuleAlarmEntity> selectListByStationIdWithRelation(@Param("stationId") Long stationId);

    @Select(
            "SELECT COUNT(1) FROM shoot_rule_alarm "
                    + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
                    + "AND rule_id = #{ruleId} AND handle_status = 'false' "
                    + "AND alarm_time >= #{sinceTime}")
    long countRecentSameAlarm(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId,
            @Param("sinceTime") LocalDateTime sinceTime);

    @Select(
            "<script>" +
            "SELECT COUNT(1) FROM shoot_rule_alarm "
                    + "WHERE deleted = 0 "
                    + "<if test='machineId != null'>AND machine_id = #{machineId}</if> "
                    + "<if test='moldId != null'>AND mold_id = #{moldId}</if> "
                    + "<if test='stationId != null'>AND station_id = #{stationId}</if> "
                    + "<if test='ruleId != null'>AND rule_id = #{ruleId}</if> " +
            "</script>")
    long countAlarms(
            @Param("machineId") Long machineId,
            @Param("moldId") Long moldId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId);

    @Select(
            "<script>" +
            "SELECT "
                    + "COUNT(1) AS totalCount, "
                    + "SUM(CASE WHEN handle_status = 'false' THEN 1 ELSE 0 END) AS alarmCount "
                    + "FROM shoot_rule_alarm "
                    + "WHERE deleted = 0 "
                    + "<if test='machineId != null'>AND machine_id = #{machineId}</if> " +
            "</script>")
    Map<String, Long> selectStatisticsOverview(@Param("machineId") Long machineId);
}
