package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcDataLatestEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** PLC 最新数据表 Mapper */
@Mapper
public interface PlcDataLatestMapper extends BaseMapper<PlcDataLatestEntity> {

    /**
     * 批量 upsert：存在则更新值和时间戳，不存在则插入。
     * 唯一索引：uk_device_field (device_name, field_key)
     */
    @Insert(
            "<script>"
                    + "INSERT INTO plc_data_latest (device_name, machine_id, `timestamp`, field_key, field_value, create_time) VALUES "
                    + "<foreach collection='rows' item='row' separator=','>"
                    + "(#{row.deviceName}, #{row.machineId}, #{row.dataTimestamp}, #{row.fieldKey}, #{row.fieldValue}, #{row.createTime})"
                    + "</foreach>"
                    + "ON DUPLICATE KEY UPDATE "
                    + "machine_id = VALUES(machine_id), "
                    + "`timestamp` = VALUES(`timestamp`), "
                    + "field_value = VALUES(field_value), "
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
                    + "field_key AS fieldKey, field_value AS fieldValue, create_time AS createTime "
                    + "FROM plc_data_latest "
                    + "WHERE machine_id = #{machineId} "
                    + "ORDER BY id ASC")
    List<PlcDataLatestEntity> selectListByMachineId(@Param("machineId") Long machineId);
}
