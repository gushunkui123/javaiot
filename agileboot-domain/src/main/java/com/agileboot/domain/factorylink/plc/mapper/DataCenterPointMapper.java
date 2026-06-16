package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.DataCenterPointEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DataCenterPointMapper extends BaseMapper<DataCenterPointEntity> {

    /** 按设备ID查询所有数据点 */
    @Select("SELECT * FROM plc_data_point WHERE device_id = #{deviceId} ORDER BY id ASC")
    List<DataCenterPointEntity> selectByDeviceId(@Param("deviceId") Long deviceId);

    /** 按分类ID查询 */
    @Select("SELECT * FROM plc_data_point WHERE category_id = #{categoryId} ORDER BY id ASC")
    List<DataCenterPointEntity> selectByCategoryId(@Param("categoryId") Long categoryId);
}
