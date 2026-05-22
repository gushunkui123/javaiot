package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcDataEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** PLC 遥测表 Mapper */
public interface PlcDataMapper extends BaseMapper<PlcDataEntity> {

  // 按机台ID查询最新数据时间
  @Select(
      "<script>"
          + "SELECT machine_id, DATE_FORMAT(MAX(`timestamp`), '%Y-%m-%d %H:%i:%s') AS data_timestamp "
          + "FROM plc_data WHERE deleted = 0 AND machine_id IN "
          + "<foreach collection='machineIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
          + "GROUP BY machine_id"
          + "</script>")
  List<Map<String, Object>> selectLatestTimestampByMachineIds(
      @Param("machineIds") Collection<Long> machineIds);

  // 按设备名称查询最新数据
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

  // 按机台ID查询最新数据
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
