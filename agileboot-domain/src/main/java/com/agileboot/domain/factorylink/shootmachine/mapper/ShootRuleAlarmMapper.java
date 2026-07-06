package com.agileboot.domain.factorylink.shootmachine.mapper;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.*;

@Mapper
public interface ShootRuleAlarmMapper extends BaseMapper<ShootRuleAlarmEntity> {

    @Select(
            "<script>" +
            "SELECT a.id, a.machine_id AS machineId, a.station_id AS stationId, a.mold_id AS moldId, a.rule_id AS ruleId, "
                    + "a.field_code AS fieldCode, a.field_name AS fieldName, a.min_value AS `minValue`, a.max_value AS `maxValue`, "
                    + "a.current_value AS currentValue, a.alarm_level AS alarmLevel, a.alarm_time AS alarmTime, "
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

 @Select(
        "<script>" +
        "SELECT "
                + "COUNT(a.id) AS alarmCount, "
                + "MIN(a.alarm_time) AS minAlarmTime, "
                + "MAX(a.alarm_time) AS maxAlarmTime, "
                + "m.machine_name AS machineName, "
                + "s.station_name AS stationName, "
                + "a.field_name AS fieldName, "
                + "a.current_value AS currentValue, "
                + "a.min_value AS `minValue`, "
                + "a.max_value AS `maxValue`, "
                + "mo.mold_model AS moldModel, "
                + "mo.color AS moldColor "
                + "FROM shoot_rule_alarm a "
                + "LEFT JOIN shoot_machine m ON a.machine_id = m.id AND m.deleted = 0 "
                + "LEFT JOIN shoot_machine_station s ON a.station_id = s.id AND s.deleted = 0 "
                + "LEFT JOIN shoot_mold mo ON a.mold_id = mo.id AND mo.deleted = 0 "
                + "WHERE a.deleted = 0 AND s.station_no = #{stationNo} "
                + "GROUP BY a.field_code, a.station_id, m.machine_name, s.station_name, "
                + "a.field_name, a.current_value, a.min_value, a.max_value, mo.mold_model, mo.color "
                + "ORDER BY MAX(a.alarm_time) DESC" +
        "</script>")
    List<Map<String, Object>> selectAlarmDetailGroupByField(@Param("stationNo") Integer stationNo);

    @Update("<script>" +
            "UPDATE shoot_rule_alarm SET handle_status = 'true', handle_remark = #{handleRemark} " +
            "WHERE deleted = 0 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    void updateHandleByStationIdAndField(
            @Param("ids") List<Long> ids,
            @Param("handleRemark") String handleRemark);

    /**
     * 插入停机黄色报警
     */
    @Insert("<script>" +
            "INSERT INTO shoot_rule_alarm (machine_id, station_id, field_code, field_name, min_value, max_value, current_value, alarm_level, alarm_time, handle_status, deleted, created_at, updated_at) " +
            "VALUES (#{machineId}, #{stationId}, 'dang_qian_jia_liu_time', '当前加硫时间', 0, 900, #{currentValue}, 'yellow', NOW(), 'false', 0, NOW(), NOW()) " +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertYellowAlarm(@Param("machineId") Long machineId, @Param("stationId") Long stationId, @Param("currentValue") Long currentValue);

    /**
     * 查询最近是否有未处理的黄色报警
     */
    @Select("SELECT COUNT(1) FROM shoot_rule_alarm "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND alarm_level = 'yellow' AND handle_status = 'false' "
            + "AND alarm_time >= #{sinceTime}")
    long countRecentYellowAlarm(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * 处理停机恢复后的黄色报警
     */
    @Update("UPDATE shoot_rule_alarm SET handle_status = 'true', handle_remark = '停机恢复' "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND alarm_level = 'yellow' AND handle_status = 'false'")
    int handleYellowAlarms(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId);

    /**
     * 批量自动处理超过指定秒数的黄色报警（基于updated_at判断，使用数据库NOW()避免时间不同步）
     */
    @Update("UPDATE shoot_rule_alarm SET handle_status = 'true', handle_remark = '超时自动取消' "
            + "WHERE deleted = 0 AND alarm_level = 'yellow' AND handle_status = 'false' "
            + "AND updated_at < DATE_SUB(NOW(), INTERVAL #{expireSeconds} SECOND)")
    int handleExpiredYellowAlarms(@Param("expireSeconds") int expireSeconds);

    /**
     * 插入红色报警
     */
    @Insert("INSERT INTO shoot_rule_alarm (machine_id, station_id, mold_id, rule_id, field_code, field_name, " +
            "min_value, max_value, current_value, alarm_level, alarm_time, handle_status, deleted, created_at, updated_at) " +
            "VALUES (#{machineId}, #{stationId}, #{moldId}, #{ruleId}, #{fieldCode}, #{fieldName}, " +
            "#{minValue}, #{maxValue}, #{currentValue}, 'red', NOW(), 'false', 0, NOW(), NOW())")
    int insertRedAlarm(@Param("machineId") Long machineId, @Param("stationId") Long stationId,
                       @Param("moldId") Long moldId, @Param("ruleId") Long ruleId,
                       @Param("fieldCode") String fieldCode, @Param("fieldName") String fieldName,
                       @Param("minValue") BigDecimal minValue, @Param("maxValue") BigDecimal maxValue,
                       @Param("currentValue") BigDecimal currentValue);

    /**
     * 查询指定规则的未处理红色报警数量
     */
    @Select("SELECT COUNT(1) FROM shoot_rule_alarm "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND rule_id = #{ruleId} AND alarm_level = 'red' AND handle_status = 'false'")
    long countUnhandledRedAlarms(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId);

    /**
     * 查询最近是否有相同的红色报警（去重用）
     */
    @Select("SELECT COUNT(1) FROM shoot_rule_alarm "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND rule_id = #{ruleId} AND alarm_level = 'red' AND handle_status = 'false' "
            + "AND alarm_time >= #{sinceTime}")
    long countRecentSameRedAlarm(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId,
            @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * 自动取消指定规则的红色报警（参数恢复正常时）
     */
    @Update("UPDATE shoot_rule_alarm SET handle_status = 'true', handle_remark = '参数恢复正常自动取消' "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND rule_id = #{ruleId} AND alarm_level = 'red' AND handle_status = 'false'")
    int autoCancelRedAlarms(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId);

    /**
     * 查询所有报警（包含已处理和未处理），用于导出Excel
     */
    @Select(
            "<script>" +
            "SELECT a.id, a.machine_id AS machineId, a.station_id AS stationId, "
                    + "a.field_code AS fieldCode, a.field_name AS fieldName, a.min_value AS `minValue`, a.max_value AS `maxValue`, "
                    + "a.current_value AS currentValue, a.alarm_level AS alarmLevel, a.alarm_time AS alarmTime, "
                    + "a.handle_status AS handleStatus, a.handle_remark AS handleRemark, "
                    + "m.machine_name AS machineName, s.station_name AS stationName, mo.mold_model AS moldModel, mo.color AS moldColor "
                    + "FROM shoot_rule_alarm a "
                    + "LEFT JOIN shoot_machine m ON a.machine_id = m.id AND m.deleted = 0 "
                    + "LEFT JOIN shoot_machine_station s ON a.station_id = s.id AND s.deleted = 0 "
                    + "LEFT JOIN shoot_mold mo ON a.mold_id = mo.id AND mo.deleted = 0 "
                    + "WHERE a.deleted = 0 "
                    + "AND a.alarm_time >= DATE_SUB(CURDATE(), INTERVAL #{days} - 1 DAY) "
                    + "<if test='machineId != null'>AND a.machine_id = #{machineId}</if> "
                    + "ORDER BY a.alarm_time DESC" +
            "</script>")
    List<ShootRuleAlarmEntity> selectAllWithRelation(@Param("machineId") Long machineId, @Param("days") Integer days);
}
