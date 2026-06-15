package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcDeviceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * PLC设备连接信息 Mapper
 */
@Mapper
public interface PlcDeviceMapper extends BaseMapper<PlcDeviceEntity> {

    /**
     * 查询点位数据（可选按设备ID过滤）
     * 
     */
    @Select("<script>" +
            "SELECT display_name AS displayName, current_value AS currentValue, mark_color AS markColor, sort_order AS sortOrder FROM plc_data_point" +
            "<where>" +
            "<if test='deviceId != null'>AND device_id = #{deviceId}</if>" +
            "</where>" +
            " ORDER BY " +
            "sort_order ASC, " +
            "display_name" +
            "</script>")
    List<Map<String, Object>> selectDataPointsByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 查询设备列表（可选按设备ID过滤）
     */
    @Select("<script>" +
            "SELECT id, ip, device_name AS deviceName, device_type AS deviceType, protocol, port, rack, slot, " +
            "created_at AS createdAt, updated_at AS updatedAt, photo, sensor_type AS sensorType, " +
            "protocol_type AS protocolType, acquisition_method AS acquisitionMethod, device_location AS deviceLocation, " +
            "acquisition_status AS acquisitionStatus, is_connected_db AS isConnectedDb " +
            "FROM plc_device" +
            "<where>" +
            "<if test='deviceId != null'>AND id = #{deviceId}</if>" +
            "</where>" +
            "</script>")
    List<Map<String, Object>> selectDevices(@Param("deviceId") Long deviceId);
}
