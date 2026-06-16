package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.DataCenterDeviceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataCenterDeviceMapper extends BaseMapper<DataCenterDeviceEntity> {

    @Select("SELECT * FROM plc_data_device ORDER BY id ASC")
    List<DataCenterDeviceEntity> selectAll();

    @Select("SELECT * FROM plc_data_device WHERE workshop_id = #{workshopId} ORDER BY id ASC")
    List<DataCenterDeviceEntity> selectByWorkshopId(@Param("workshopId") Long workshopId);

    @Select("SELECT * FROM plc_data_device WHERE area_id = #{areaId} ORDER BY id ASC")
    List<DataCenterDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    @Select("SELECT * FROM plc_data_device WHERE device_name LIKE CONCAT('%', #{keyword}, '%') ORDER BY id ASC")
    List<DataCenterDeviceEntity> selectByKeyword(@Param("keyword") String keyword);
}
