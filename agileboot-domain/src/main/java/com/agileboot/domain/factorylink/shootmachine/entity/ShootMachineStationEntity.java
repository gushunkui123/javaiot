package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("shoot_machine_station")
public class ShootMachineStationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("machine_id")
    private Long machineId;

    @TableField("station_no")
    private Integer stationNo;

    @TableField("station_name")
    private String stationName;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("remark")
    private String remark;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    /** 射枪数量（非表字段，按机台站位总数推导：10站位机台=4枪，8站位机台=2枪） */
    @TableField(exist = false)
    private Integer gunCount;

    /** 按机台站位总数推导射枪数量：10站位机台4枪、8站位机台2枪，其余默认2枪 */
    public static int resolveGunCount(int stationCount) {
        if (stationCount == 10) return 4;
        if (stationCount == 8) return 2;
        return 2;
    }
}
