package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("shoot_station_schedule")
public class ShootStationScheduleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_RUNNING = "running";
    public static final String STATUS_FINISHED = "finished";
    public static final String STATUS_CANCELLED = "cancelled";

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("machine_id")
    private Long machineId;

    @TableField("station_id")
    private Long stationId;

    @TableField("station_no")
    private Integer stationNo;

    @TableField("mold_id")
    private Long moldId;

    @TableField("start_time")
    private Date startTime;

    @TableField("end_time")
    private Date endTime;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
