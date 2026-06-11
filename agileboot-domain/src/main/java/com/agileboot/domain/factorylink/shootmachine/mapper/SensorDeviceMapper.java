package com.agileboot.domain.factorylink.shootmachine.mapper;

import com.agileboot.domain.factorylink.shootmachine.entity.SensorDeviceEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.SensorDataCategoryEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.SensorDataItemEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 传感器设备 Mapper
 */
@Mapper
public interface SensorDeviceMapper extends BaseMapper<SensorDeviceEntity> {

    /**
     * 查询所有设备列表（用于分页）
     */
    @Select("SELECT * FROM sensor_device WHERE deleted = 0 ORDER BY id DESC")
    List<SensorDeviceEntity> selectDeviceList();

    /**
     * 根据设备ID查询数据大类
     */
    @Select("SELECT * FROM sensor_data_category WHERE device_id = #{deviceId} ORDER BY sort_order")
    List<SensorDataCategoryEntity> selectCategoriesByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 根据大类ID查询数据小项
     */
    @Select("SELECT * FROM sensor_data_item WHERE category_id = #{categoryId} ORDER BY sort_order")
    List<SensorDataItemEntity> selectItemsByCategoryId(@Param("categoryId") Long categoryId);
}
