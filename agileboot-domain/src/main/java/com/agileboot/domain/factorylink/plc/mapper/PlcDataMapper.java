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

    /**
     * 批量查询各设备最新采集时间。Map：device_name、data_timestamp。
     */
    @Select(
            "<script>"
                    + "SELECT device_name, MAX(`timestamp`) AS data_timestamp "
                    + "FROM plc_data WHERE deleted = 0 AND device_name IN "
                    + "<foreach collection='deviceNames' item='name' open='(' separator=',' close=')'>#{name}</foreach> "
                    + "GROUP BY device_name"
                    + "</script>")
    List<Map<String, Object>> selectLatestTimestampByDeviceNames(
            @Param("deviceNames") Collection<String> deviceNames);

    /**
     * 查询设备最新一次采集时刻下的全部字段行。
     */
    @Select(
            "SELECT id, device_name AS deviceName, `timestamp` AS dataTimestamp, "
                    + "field_key AS fieldKey, field_value AS fieldValue, "
                    + "create_time AS createTime, deleted "
                    + "FROM plc_data "
                    + "WHERE deleted = 0 AND device_name = #{deviceName} "
                    + "AND `timestamp` = ("
                    + "  SELECT MAX(`timestamp`) FROM plc_data "
                    + "  WHERE deleted = 0 AND device_name = #{deviceName}"
                    + ") "
                    + "ORDER BY id ASC")
    List<PlcDataEntity> selectListLatestSameTimestampByDeviceName(@Param("deviceName") String deviceName);
}
