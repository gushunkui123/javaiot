package com.agileboot.domain.factorylink.plc.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 数据中心 Mapper - 查询 iot_data_center 数据库
 */
@Mapper
@DS("data_center")
public interface DataCenterMapper extends BaseMapper<Object> {

    /**
     * 根据设备ID查询点位数据
     */
    @Select("<script>" +
            "SELECT display_name AS displayName, current_value AS currentValue, " +
            "DATE_FORMAT(data_updated_at, '%Y-%m-%d %H:%i:%s') AS dataUpdatedAt FROM plc_data_point" +
            "<where>" +
            "<if test='deviceId != null'>AND device_id = #{deviceId}</if>" +
            "</where>" +
            " ORDER BY sort_order ASC, display_name" +
            "</script>")
    List<Map<String, Object>> selectDataPointsByDeviceId(@Param("deviceId") Long deviceId);
}
