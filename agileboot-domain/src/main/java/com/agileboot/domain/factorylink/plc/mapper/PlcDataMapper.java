package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** PLC 遥测表 Mapper */
@Mapper
public interface PlcDataMapper extends BaseMapper<PlcDataEntity> {

    @Select(
            "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, "
                    + "create_time AS createTime, deleted "
                    + "FROM plc_data "
                    + "WHERE deleted = 0 AND device_name = #{deviceName} "
                    + "AND `timestamp` = ("
                    + "  SELECT `timestamp` FROM plc_data "
                    + "  WHERE deleted = 0 AND device_name = #{deviceName} "
                    + "  ORDER BY `timestamp` DESC LIMIT 1"
                    + ") "
                    + "ORDER BY id ASC")
    List<PlcDataEntity> selectListLatestSameTimestampByDeviceName(
            @Param("deviceName") String deviceName);

    @Select(
            "SELECT id, device_name AS deviceName, machine_id AS machineId, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, "
                    + "create_time AS createTime, deleted "
                    + "FROM plc_data "
                    + "WHERE deleted = 0 AND machine_id = #{machineId} "
                    + "AND `timestamp` = ("
                    + "  SELECT `timestamp` FROM plc_data "
                    + "  WHERE deleted = 0 AND machine_id = #{machineId} "
                    + "  ORDER BY `timestamp` DESC LIMIT 1"
                    + ") "
                    + "ORDER BY id ASC")
    List<PlcDataEntity> selectListLatestSameTimestampByMachineId(
            @Param("machineId") Long machineId);
}
