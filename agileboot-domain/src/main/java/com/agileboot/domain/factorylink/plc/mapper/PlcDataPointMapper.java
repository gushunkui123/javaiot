package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PlcDataPointMapper extends BaseMapper<PlcDataPointEntity> {

    /** 批量更新排序号与标记色；不刷新 updated_at。 */
    @Update("<script>" +
            "<foreach collection='items' item='item' separator=';'>" +
            "UPDATE plc_data_point SET sort_order = #{item.sortOrder}, mark_color = #{item.markColor}, updated_at = updated_at WHERE id = #{item.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateSortAndMark(@Param("items") List<PlcDataPointEntity> items);

    @Select(
            "SELECT pdp.id, pdp.device_id, pdp.area, pdp.number, pdp.byte_offset, pdp.bit_offset, "
                    + "pdp.data_type, pdp.display_name, "
                    + "pdp.sort_order, pdp.mark_color, pdp.unit, pdp.current_value, pdp.updated_at "
                    + "FROM plc_data_point pdp FORCE INDEX (idx_point_device_sort) "
                    + "WHERE pdp.device_id = #{deviceId} "
                    + "ORDER BY pdp.sort_order ASC, pdp.mark_color ASC, pdp.id ASC")
    List<PlcDataPointEntity> selectListByDeviceId(@Param("deviceId") Long deviceId);
}
