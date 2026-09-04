package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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
@TableName("shoot_machine")
public class ShootMachineEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("machine_name")
    private String machineName;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("remark")
    private String remark;

    /** 机台分组编码（对应 machine_group.group_code，如 workshop_1） */
    @TableField("machine_group")
    private String machineGroup;

    /** 站位数量，新增机台时前端输入，后端自动生成对应站位记录 */
    @TableField("station_count")
    private Integer stationCount;

    @TableField("gun_count")
    private Integer gunCount;

    /** 排序（升序，越小越靠前） */
    @TableField("sort")
    private Integer sort;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    /** MQTT 订阅主题，与 EMQX 发布 topic 一致 */
    @TableField("topic")
    private String topic;

    /** 最新 PLC 采集时间（非表字段，与 plc_data.timestamp 字面一致） */
    @TableField(exist = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestPlcDataTime;

    /** 最新采集时间在 2 分钟内为 true，否则停机（非表字段） */
    @TableField(exist = false)
    private Boolean running;

    /** 是否存在未处理报警（非表字段，大屏机台列表用） */
    @TableField(exist = false)
    private Boolean hasUnhandledAlarm;
}
