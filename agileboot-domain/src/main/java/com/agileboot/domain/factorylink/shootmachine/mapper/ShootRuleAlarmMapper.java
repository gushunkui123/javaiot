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

    /**
     * 聚合查询未处理报警：按机器+站位分组，统计红/黄数量（首屏看板用，数据量极小）
     */
    @Select("<script>" +
            "SELECT a.machine_id AS machineId, m.machine_name AS machineName, " +
            "a.station_id AS stationId, s.station_name AS stationName, " +
            "SUM(CASE WHEN a.alarm_level = 'red' THEN 1 ELSE 0 END) AS redCount, " +
            "SUM(CASE WHEN a.alarm_level = 'yellow' THEN 1 ELSE 0 END) AS yellowCount " +
            "FROM shoot_rule_alarm a " +
            "LEFT JOIN shoot_machine m ON a.machine_id = m.id AND m.deleted = 0 " +
            "LEFT JOIN shoot_machine_station s ON a.station_id = s.id AND s.deleted = 0 " +
            "WHERE a.deleted = 0 AND a.handle_status = 'false' " +
            "<if test='machineId != null'>AND a.machine_id = #{machineId}</if> " +
            "GROUP BY a.machine_id, a.station_id, m.machine_name, s.station_name " +
            "ORDER BY a.machine_id, a.station_id" +
            "</script>")
    List<Map<String, Object>> selectUnhandledSummary(@Param("machineId") Long machineId);

    /**
     * 分页查询未处理报警明细（按机器+站位过滤），用于点开站位后按需加载
     */
    @Select("<script>" +
            "SELECT a.id, a.machine_id AS machineId, a.station_id AS stationId, a.mold_id AS moldId, a.rule_id AS ruleId, " +
            "a.field_code AS fieldCode, a.field_name AS fieldName, a.min_value AS `minValue`, a.max_value AS `maxValue`, " +
            "a.current_value AS currentValue, a.alarm_level AS alarmLevel, a.alarm_time AS alarmTime, " +
            "a.handle_status AS handleStatus, a.handle_remark AS handleRemark, a.created_at AS createdAt, " +
            "a.updated_at AS updatedAt, a.deleted, " +
            "m.machine_name AS machineName, s.station_name AS stationName, mo.mold_model AS moldModel, mo.color AS moldColor " +
            "FROM shoot_rule_alarm a " +
            "LEFT JOIN shoot_machine m ON a.machine_id = m.id AND m.deleted = 0 " +
            "LEFT JOIN shoot_machine_station s ON a.station_id = s.id AND s.deleted = 0 " +
            "LEFT JOIN shoot_mold mo ON a.mold_id = mo.id AND mo.deleted = 0 " +
            "WHERE a.deleted = 0 AND a.handle_status = 'false' " +
            "<if test='machineId != null'>AND a.machine_id = #{machineId}</if> " +
            "<if test='stationId != null'>AND a.station_id = #{stationId}</if> " +
            "ORDER BY a.alarm_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<ShootRuleAlarmEntity> selectUnhandledPaged(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("offset") long offset,
            @Param("pageSize") long pageSize);

    /**
     * 统计未处理报警总数（配合分页使用）
     */
    @Select("<script>" +
            "SELECT COUNT(1) FROM shoot_rule_alarm a " +
            "WHERE a.deleted = 0 AND a.handle_status = 'false' " +
            "<if test='machineId != null'>AND a.machine_id = #{machineId}</if> " +
            "<if test='stationId != null'>AND a.station_id = #{stationId}</if> " +
            "</script>")
    long countUnhandled(@Param("machineId") Long machineId, @Param("stationId") Long stationId);

    /**
     * 批量查询存在未处理报警的机器ID（大屏机台列表用，单次 IN 查询）
     */
    @Select("<script>" +
            "SELECT DISTINCT machine_id FROM shoot_rule_alarm " +
            "WHERE deleted = 0 AND handle_status = 'false' " +
            "AND machine_id IN " +
            "<foreach collection='machineIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<Long> selectMachineIdsWithUnhandledAlarm(@Param("machineIds") List<Long> machineIds);

    @Select(
            "<script>" +
            "SELECT COUNT(1) FROM shoot_rule_alarm "
                    + "WHERE deleted = 0 AND handle_status = 'false' "
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
     * 插入黄色报警（数据超时/操作超时，关联模具和规则）
     */
    @Insert("<script>" +
            "INSERT INTO shoot_rule_alarm (machine_id, station_id, mold_id, rule_id, field_code, field_name, min_value, max_value, current_value, alarm_level, alarm_time, handle_status, deleted, created_at, updated_at) " +
            "VALUES (#{machineId}, #{stationId}, #{moldId}, #{ruleId}, #{fieldCode}, #{fieldName}, 0, 900, #{currentValue}, 'yellow', NOW(), 'false', 0, NOW(), NOW()) " +
            "</script>")
    int insertYellowAlarm(@Param("machineId") Long machineId, @Param("stationId") Long stationId,
                          @Param("moldId") Long moldId, @Param("ruleId") Long ruleId,
                          @Param("fieldCode") String fieldCode, @Param("fieldName") String fieldName,
                          @Param("currentValue") Long currentValue);

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
     * 查询最近是否有相同的黄色报警（按规则去重）
     */
    @Select("SELECT COUNT(1) FROM shoot_rule_alarm "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND rule_id = #{ruleId} AND alarm_level = 'yellow' AND handle_status = 'false' "
            + "AND alarm_time >= #{sinceTime}")
    long countRecentSameYellowAlarm(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId,
            @Param("sinceTime") LocalDateTime sinceTime);

    /**
     * 查询是否有未处理的相同黄色报警（按fieldCode去重）
     * 只要有未处理的同fieldCode报警就不创建新的，确保一次停机事件只产生一条报警
     */
    @Select("SELECT COUNT(1) FROM shoot_rule_alarm "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND field_code = #{fieldCode} AND alarm_level = 'yellow' AND handle_status = 'false'")
    long countRecentSameYellowAlarmByFieldCode(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("fieldCode") String fieldCode);

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
     * 状态恢复时仅取消该站位指定 field_code 的黄色报警，不影响其他黄色报警
     */
    @Update("<script>UPDATE shoot_rule_alarm SET handle_status = 'true', handle_remark = '状态恢复自动取消' "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND alarm_level = 'yellow' AND handle_status = 'false' "
            + "AND field_code IN "
            + "<foreach collection='fieldCodes' item='code' open='(' separator=',' close=')'>#{code}</foreach>"
            + "</script>")
    int handleYellowAlarmsByFieldCodes(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("fieldCodes") List<String> fieldCodes);

    /**
     * 按fieldCode处理超过指定秒数的黄色报警（基于alarm_time判断）
     * operation_timeout → handle_remark = '操作超时'
     * stop_no_mold_close → handle_remark = '5分钟未合模停机'
     */
    @Update("<script>UPDATE shoot_rule_alarm SET handle_status = 'true', "
            + "handle_remark = field_name "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND field_code = #{fieldCode} AND alarm_level = 'yellow' AND handle_status = 'false' "
            + "AND alarm_time &lt; DATE_SUB(NOW(), INTERVAL #{expireSeconds} SECOND)"
            + "</script>")
    int handleExpiredYellowAlarmsByFieldCode(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("fieldCode") String fieldCode,
            @Param("expireSeconds") int expireSeconds);

    /**
     * 合模止恢复ON时，更新操作超时报警的currentValue为超出时间，并标记为已处理
     */
    @Update("UPDATE shoot_rule_alarm SET current_value = #{currentValue}, "
            + "handle_status = 'true', handle_remark = '操作完成自动处理' "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND field_code = #{fieldCode} AND alarm_level = 'yellow' AND handle_status = 'false'")
    int updateCurrentValueAndHandle(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("fieldCode") String fieldCode,
            @Param("currentValue") BigDecimal currentValue);

    /**
     * 更新未处理报警的currentValue（不改变handle_status）
     */
    @Update("UPDATE shoot_rule_alarm SET current_value = #{currentValue}, updated_at = NOW() "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND field_code = #{fieldCode} AND alarm_level = 'yellow' AND handle_status = 'false'")
    int updateCurrentValueByFieldCode(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("fieldCode") String fieldCode,
            @Param("currentValue") BigDecimal currentValue);

    /**
     * 获取未处理报警的创建时间（用于计算超出时间）
     */
    @Select("SELECT alarm_time FROM shoot_rule_alarm "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND field_code = #{fieldCode} AND alarm_level = 'yellow' AND handle_status = 'false' "
            + "ORDER BY alarm_time DESC LIMIT 1")
    LocalDateTime getAlarmCreateTime(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("fieldCode") String fieldCode);

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
     * 判断是否已存在同字段的未处理红色报警（按 机器+站位+规则+字段 维度去重，不看当前值）。
     * 用于「同一超标字段只保留一条未处理报警」：只要该字段仍超标且未处理，无论值如何变化都只更新 current_value，不再新增记录。
     * fieldCode 为空时仅按 ruleId 判定；非空时按 ruleId + fieldCode 判定（支持同规则多阶段各自独立）。
     */
    @Select("<script>SELECT COUNT(1) FROM shoot_rule_alarm "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND rule_id = #{ruleId} AND alarm_level = 'red' AND handle_status = 'false' "
            + "<if test='fieldCode != null and fieldCode != \"\"'> AND field_code = #{fieldCode}</if>"
            + "</script>")
    long existsUnhandledRedAlarm(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId,
            @Param("fieldCode") String fieldCode);

    /**
     * 更新已存在未处理红色报警的 current_value 为最新超标值（不新增记录）。
     */
    @Update("<script>UPDATE shoot_rule_alarm SET current_value = #{currentValue}, updated_at = NOW() "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND rule_id = #{ruleId} AND alarm_level = 'red' AND handle_status = 'false'"
            + "<if test='fieldCode != null and fieldCode != \"\"'> AND field_code = #{fieldCode}</if>"
            + "</script>")
    int updateRedAlarmCurrentValue(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId,
            @Param("fieldCode") String fieldCode,
            @Param("currentValue") BigDecimal currentValue);

    /**
     * 自动取消指定规则的红色报警（参数恢复正常时）
     * fieldCode 为空时取消该规则全部红色报警；非空时仅取消该 fieldCode 对应的报警
     */
    @Update("<script>UPDATE shoot_rule_alarm SET handle_status = 'true', handle_remark = '参数恢复正常自动取消' "
            + "WHERE deleted = 0 AND machine_id = #{machineId} AND station_id = #{stationId} "
            + "AND rule_id = #{ruleId} AND alarm_level = 'red' AND handle_status = 'false'"
            + "<if test='fieldCode != null and fieldCode != \"\"'> AND field_code = #{fieldCode}</if>"
            + "</script>")
    int autoCancelRedAlarms(
            @Param("machineId") Long machineId,
            @Param("stationId") Long stationId,
            @Param("ruleId") Long ruleId,
            @Param("fieldCode") String fieldCode);

    /**
     * 自动取消「所属模具已无当前排期」的红色报警（孤儿报警清理）。
     * 业务约定：只在有排期时才做阈值检测与处理；一旦告警所属的模具在该站台当前无有效排期，
     * 该告警即为失效告警，应自动标记为已处理。
     * 有效排期定义：未删除、且 start_time <= NOW() < end_time。
     */
    @Update("<script>"
            + "UPDATE shoot_rule_alarm a "
            + "LEFT JOIN ("
            + "    SELECT sc.station_id, sc.mold_id "
            + "    FROM shoot_station_schedule sc "
            + "    WHERE sc.deleted = 0 AND sc.start_time &lt;= NOW() AND sc.end_time &gt; NOW()"
            + ") cur ON cur.station_id = a.station_id AND cur.mold_id = a.mold_id "
            + "SET a.handle_status = 'true', a.handle_remark = '排期变更自动取消' "
            + "WHERE a.deleted = 0 AND a.handle_status = 'false' AND a.alarm_level = 'red' "
            + "  AND cur.station_id IS NULL"
            + "</script>")
    int autoCancelAlarmsWithoutCurrentSchedule();

    /**
     * 自动更新/取消红色报警：用 plc_data_latest 的最新实时值刷新 current_value；
     * 若最新值已恢复到「告警自身 rule_id 对应规则」的阈值内，则自动标记为已处理。
     * 匹配规则：
     *   1. 阈值取自 shoot_mold_rule，按告警自己的 rule_id 关联（避免多排期/换模歧义）；
     *   2. plc_data_latest 的 field_key 与告警 field_code 做「左右模前缀归一化」匹配
     *      （规则表存 "第一阶段 射出速度"，告警/PLC 存 "右模第一阶段 射出速度"）。
     */
    @Update("<script>"
            + "UPDATE shoot_rule_alarm a "
            + "JOIN shoot_machine_station s ON s.id = a.station_id "
            + "LEFT JOIN shoot_mold_rule r "
            + "  ON r.id = a.rule_id AND r.deleted = 0 "
            + "LEFT JOIN plc_data_latest p "
            + "  ON p.machine_id = a.machine_id "
            + "  AND REPLACE(REPLACE(p.field_key, '左模', ''), '右模', '') = REPLACE(REPLACE(a.field_code, '左模', ''), '右模', '') "
            + "  AND p.category_name = CONCAT('站台', s.station_no) "
            + "SET a.current_value = CAST(p.field_value AS DECIMAL), "
            + "    a.handle_status = CASE "
            + "        WHEN CAST(p.field_value AS DECIMAL) BETWEEN r.min_value AND r.max_value "
            + "        THEN 'true' ELSE a.handle_status END, "
            + "    a.handle_remark = CASE "
            + "        WHEN CAST(p.field_value AS DECIMAL) BETWEEN r.min_value AND r.max_value "
            + "        THEN '参数恢复正常自动取消' ELSE a.handle_remark END "
            + "WHERE a.deleted = 0 AND a.handle_status = 'false' AND a.alarm_level = 'red' "
            + "  AND r.id IS NOT NULL "
            + "  AND p.field_value IS NOT NULL"
            + "</script>")
    int autoCancelAlarmsByCurrentValue();

    /**
     * 查询所有报警（包含已处理和未处理），用于导出Excel
     */
    @Select(
            "<script>" +
            "SELECT a.machine_id AS machineId, a.station_id AS stationId, "
                    + "a.field_code AS fieldCode, MAX(a.field_name) AS fieldName, "
                    + "MAX(a.min_value) AS `minValue`, MAX(a.current_value) AS currentValue, MAX(a.max_value) AS `maxValue`, "
                    + "a.alarm_level AS alarmLevel, a.handle_status AS handleStatus, "
                    + "MAX(a.alarm_time) AS alarmTime, "
                    + "MAX(m.machine_name) AS machineName, MAX(s.station_name) AS stationName, "
                    + "MAX(mo.mold_model) AS moldModel, MAX(mo.color) AS moldColor, "
                    + "COUNT(*) AS occurrenceCount, MIN(a.alarm_time) AS firstAlarmTime, MAX(a.alarm_time) AS lastAlarmTime "
                    + "FROM shoot_rule_alarm a "
                    + "LEFT JOIN shoot_machine m ON a.machine_id = m.id AND m.deleted = 0 "
                    + "LEFT JOIN shoot_machine_station s ON a.station_id = s.id AND s.deleted = 0 "
                    + "LEFT JOIN shoot_mold mo ON a.mold_id = mo.id AND mo.deleted = 0 "
                    + "WHERE a.deleted = 0 "
                    + "AND a.alarm_time >= DATE_SUB(CURDATE(), INTERVAL #{days} - 1 DAY) "
                    + "<if test='machineId != null'>AND a.machine_id = #{machineId}</if> "
                    + "GROUP BY a.machine_id, a.station_id, a.field_code, a.alarm_level, a.handle_status "
                    + "ORDER BY MAX(a.alarm_time) DESC" +
            "</script>")
    List<ShootRuleAlarmEntity> selectAllWithRelation(@Param("machineId") Long machineId, @Param("days") Integer days);


}
