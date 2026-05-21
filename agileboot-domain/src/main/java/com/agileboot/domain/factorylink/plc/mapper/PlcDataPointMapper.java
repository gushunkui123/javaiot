package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface PlcDataPointMapper extends BaseMapper<PlcDataPointEntity> {

    @Select(
            "SELECT pdp.id, pdp.device_id, pdp.area, pdp.number, pdp.byte_offset, pdp.bit_offset, "
                    + "pdp.data_type, pdp.display_name, pdp.sort_order, pdp.mark_color, pdp.unit, "
                    + "pdp.current_value, pdp.created_at, pdp.updated_at "
                    + "FROM plc_data_point pdp "
                    + "INNER JOIN plc_device pd ON pdp.device_id = pd.id "
                    + "WHERE pd.device_name = #{deviceName} "
                    + "ORDER BY pdp.sort_order ASC, pdp.mark_color ASC, pdp.id ASC")
    List<PlcDataPointEntity> selectListByDeviceName(String deviceName);

    /** 更新排序号与标记色；不刷新 updated_at。 */
    @Update(
            "UPDATE plc_data_point SET sort_order = #{sortOrder}, mark_color = #{markColor}, "
                    + "updated_at = updated_at WHERE id = #{id}")
    int updateSortAndMarkById(
            @Param("id") Long id,
            @Param("sortOrder") Integer sortOrder,
            @Param("markColor") String markColor);
}
