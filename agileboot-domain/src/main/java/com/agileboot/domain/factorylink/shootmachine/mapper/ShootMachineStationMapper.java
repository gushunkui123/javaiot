package com.agileboot.domain.factorylink.shootmachine.mapper;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineStationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ShootMachineStationMapper extends BaseMapper<ShootMachineStationEntity> {

    @Update("UPDATE shoot_machine_station SET station_name = REPLACE(station_name, #{oldName}, #{newName}), updated_at = NOW() WHERE machine_id = #{machineId} AND deleted = 0")
    int updateStationName(@Param("machineId") Long machineId, @Param("oldName") String oldName, @Param("newName") String newName);
}
