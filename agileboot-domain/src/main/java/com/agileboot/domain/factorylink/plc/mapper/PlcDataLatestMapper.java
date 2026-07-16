package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** PLC 最新数据表 Mapper */
@Mapper
public interface PlcDataLatestMapper extends BaseMapper<PlcDataLatestEntity> {

    /**
     * 批量 upsert：存在则更新值和时间戳，不存在则插入。
     * value_changed_at 仅在 field_value 变化时更新，用于超时检测（合模止 OFF 持续时间）
     */
    @Insert(
            "<script>"
                    + "INSERT INTO plc_data_latest (device_name, machine_id, `timestamp`, data_code, field_key, field_value, category_name, create_time, value_changed_at) VALUES "
                    + "<foreach collection='rows' item='row' separator=','>"
                    + "(#{row.deviceName}, #{row.machineId}, #{row.dataTimestamp}, #{row.dataCode}, #{row.fieldKey}, #{row.fieldValue}, #{row.categoryName}, #{row.createTime}, #{row.valueChangedAt})"
                    + "</foreach>"
                    + "ON DUPLICATE KEY UPDATE "
                    + "machine_id = VALUES(machine_id), "
                    + "`timestamp` = VALUES(`timestamp`), "
                    + "field_key = VALUES(field_key), "
                    + "value_changed_at = IF(VALUES(field_value) != field_value, NOW(), value_changed_at), "
                    + "field_value = VALUES(field_value), "
                    + "category_name = VALUES(category_name), "
                    + "create_time = VALUES(create_time)"
                    + "</script>")
    int batchUpsert(@Param("rows") List<PlcDataLatestEntity> rows);

    @Select(
            "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, create_time AS createTime "
                    + "FROM plc_data_latest "
                    + "WHERE device_name = #{deviceName} "
                    + "ORDER BY id ASC")
    List<PlcDataLatestEntity> selectListByDeviceName(@Param("deviceName") String deviceName);

    @Select(
            "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, category_name AS categoryName, create_time AS createTime "
                    + "FROM plc_data_latest "
                    + "WHERE machine_id = #{machineId} "
                    + "ORDER BY id ASC")
    List<PlcDataLatestEntity> selectListByMachineId(@Param("machineId") Long machineId);

    /**
     * 查询 field_key 不包含"当前"或"实时"的数据
     */
    @Select(
            "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, category_name AS categoryName, create_time AS createTime "
                    + "FROM plc_data_latest "
                    + "WHERE device_name = #{deviceName} "
                    + "AND field_key NOT LIKE '%当前%' AND field_key NOT LIKE '%实时%' "
                    + "ORDER BY category_name, field_key")
    List<PlcDataLatestEntity> selectExcludeCurrentOrRealtimeByDeviceName(@Param("deviceName") String deviceName);

    /**
     * 查询所有数据（包含"当前"和"实时"），用于大屏展示
     */
    @Select(
            "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, category_name AS categoryName, create_time AS createTime "
                    + "FROM plc_data_latest "
                    + "WHERE device_name = #{deviceName} "
                    + "ORDER BY category_name, field_key")
    List<PlcDataLatestEntity> selectAllByDeviceName(@Param("deviceName") String deviceName);

    /**
     * 查询指定机器的特定字段的最新更新时间
     */
    @Select(
            "SELECT `timestamp` AS dataTimestamp, create_time AS createTime "
                    + "FROM plc_data_latest "
                    + "WHERE machine_id = #{machineId} AND field_key = #{fieldKey} "
                    + "ORDER BY create_time DESC LIMIT 1")
    PlcDataLatestEntity selectLatestByMachineIdAndFieldKey(@Param("machineId") Long machineId, @Param("fieldKey") String fieldKey);

    /**
     * 查询指定机器、字段名、分类名的最新记录（用于停机检测）
     */
    @Select(
            "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, category_name AS categoryName, create_time AS createTime, "
                    + "value_changed_at AS valueChangedAt "
                    + "FROM plc_data_latest "
                    + "WHERE machine_id = #{machineId} AND field_key = #{fieldKey} AND category_name = #{categoryName} "
                    + "ORDER BY create_time DESC LIMIT 1")
    PlcDataLatestEntity selectLatestByMachineIdAndFieldKeyAndCategory(@Param("machineId") Long machineId,
                                                                      @Param("fieldKey") String fieldKey,
                                                                      @Param("categoryName") String categoryName);

    /**
     * 查询指定机器、字段名的最新记录（不按category_name过滤，用于全局字段如射枪温度）
     */
    @Select(
            "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, category_name AS categoryName, create_time AS createTime, "
                    + "value_changed_at AS valueChangedAt "
                    + "FROM plc_data_latest "
                    + "WHERE machine_id = #{machineId} AND field_key = #{fieldKey} "
                    + "ORDER BY create_time DESC LIMIT 1")
    PlcDataLatestEntity selectLatestByMachineIdAndFieldKeyForGlobal(@Param("machineId") Long machineId,
                                                                    @Param("fieldKey") String fieldKey);

    /**
     * 查询指定机器下多个字段名、分类名的最新记录列表（用于状态切换超时检测）
     */
    @Select(
            "<script>"
                    + "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, category_name AS categoryName, create_time AS createTime "
                    + "FROM plc_data_latest "
                    + "WHERE machine_id = #{machineId} "
                    + "AND (field_key, category_name) IN "
                    + "<foreach collection='fieldCategories' item='fc' open='(' separator=',' close=')'>"
                    + "(#{fc.fieldKey}, #{fc.categoryName})"
                    + "</foreach> "
                    + "ORDER BY field_key, category_name, create_time DESC"
                    + "</script>")
    List<PlcDataLatestEntity> selectLatestByFieldAndCategoryList(@Param("machineId") Long machineId,
                                                                 @Param("fieldCategories") List<Map<String, String>> fieldCategories);
}
