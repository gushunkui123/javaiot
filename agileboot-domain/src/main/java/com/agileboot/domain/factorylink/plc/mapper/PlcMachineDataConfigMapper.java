package com.agileboot.domain.factorylink.plc.mapper;

import com.agileboot.domain.factorylink.plc.entity.PlcMachineDataConfigEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PlcMachineDataConfigMapper extends BaseMapper<PlcMachineDataConfigEntity> {

    @Select("SELECT * FROM plc_machine_data_config WHERE enabled = 1 ORDER BY machine_name, id")
    List<PlcMachineDataConfigEntity> listEnabled();
}
