package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcDataPointEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Select;

public interface PlcDataPointMapper extends BaseMapper<PlcDataPointEntity> {

    @Select("SELECT pdp.id, pdp.device_id, pdp.area, pdp.number, pdp.byte_offset, pdp.bit_offset, " +
            "pdp.data_type, pdp.display_name, pdp.unit, pdp.current_value, pdp.created_at, pdp.updated_at " +
            "FROM plc_data_point pdp " +
            "INNER JOIN plc_device pd ON pdp.device_id = pd.id " +
            "WHERE pd.device_name = #{deviceName} " +
            "ORDER BY pdp.id ASC")
    List<PlcDataPointEntity> selectListByDeviceName(String deviceName);
}
