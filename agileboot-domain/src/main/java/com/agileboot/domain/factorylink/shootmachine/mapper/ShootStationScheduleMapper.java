package com.agileboot.domain.factorylink.shootmachine.mapper;

import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ShootStationScheduleMapper extends BaseMapper<ShootStationScheduleEntity> {

    @Select(
            "<script>"
                    + "SELECT s.id, s.machine_id, s.station_id, s.station_no, s.mold_id, "
                    + "s.start_time, s.end_time, s.status, s.remark, "
                    + "s.created_at, s.updated_at, s.deleted, "
                    + "s.mold_side AS moldSide, "
                    + "m.mold_model, m.color "
                    + "FROM shoot_station_schedule s "
                    + "LEFT JOIN shoot_mold m ON s.mold_id = m.id AND m.deleted = 0 "
                    + "WHERE s.deleted = 0 AND s.station_id = #{stationId} AND s.status != 'cancelled' "
                    + "<if test='startDate != null'>AND s.start_time &gt;= #{startDate} </if>"
                    + "<if test='endDate != null'>AND s.end_time &lt;= #{endDate} </if>"
                    + "<if test='moldSide != null'>AND s.mold_side = #{moldSide} </if>"
                    + "ORDER BY s.start_time"
                    + "</script>")
    List<ShootStationScheduleEntity> selectListByStationIdWithMoldAndDateRange(
            @Param("stationId") Long stationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("moldSide") String moldSide);

    @Select(
            "SELECT s.id, s.machine_id, s.station_id, s.station_no, s.mold_id, "
                    + "s.start_time, s.end_time, s.status, s.remark, "
                    + "s.created_at, s.updated_at, s.deleted, "
                    + "m.mold_model, m.color "
                    + "FROM shoot_station_schedule s "
                    + "LEFT JOIN shoot_mold m ON s.mold_id = m.id AND m.deleted = 0 "
                    + "WHERE s.deleted = 0 AND s.machine_id = #{machineId} AND s.status != 'cancelled' "
                    + "AND s.start_time <= #{now} AND s.end_time > #{now} "
                    + "ORDER BY s.station_no")
    List<ShootStationScheduleEntity> selectListCurrentByMachineIdWithMold(
            @Param("machineId") Long machineId, @Param("now") LocalDateTime now);

    @Select(
            "SELECT s.id, s.machine_id, s.station_id, s.station_no, s.mold_id, "
                    + "s.start_time, s.end_time, s.status, s.remark, "
                    + "s.created_at, s.updated_at, s.deleted, "
                    + "m.mold_model, m.color "
                    + "FROM shoot_station_schedule s "
                    + "LEFT JOIN shoot_mold m ON s.mold_id = m.id AND m.deleted = 0 "
                    + "WHERE s.deleted = 0 AND s.id = #{id}")
    ShootStationScheduleEntity selectByIdWithMold(@Param("id") Long id);
}
