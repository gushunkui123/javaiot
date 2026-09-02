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

    /** 射枪数量（非表字段，按机台站位总数推导：10站位=4枪，6/8站位=2枪） */
    @TableField(exist = false)
    private Integer gunCount;

    /** 左模在产产品型号（非表字段，来自 shoot_station_schedule 当前在产记录） */
    @TableField(exist = false)
    private String leftMoldModel;

    /** 右模在产产品型号（非表字段） */
    @TableField(exist = false)
    private String rightMoldModel;

    /** 按机台站位总数推导射枪数量：10站位4枪，6/8站位2枪 */
    public static int resolveGunCount(int stationCount) {
        if (stationCount == 10) return 4;
        return 2;
    }
}
