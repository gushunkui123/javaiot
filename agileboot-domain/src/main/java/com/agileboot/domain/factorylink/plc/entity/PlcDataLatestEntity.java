package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/** PLC 最新数据表，仅保留每个设备最新一批采集数据。 */
@Data
@TableName("plc_data_latest")
public class PlcDataLatestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_name")
    private String deviceName;

    /** 关联 shoot_machine.id */
    @TableField("machine_id")
    private Long machineId;

    /** 业务上的采集时间，映射列名 timestamp */
    @TableField("`timestamp`")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataTimestamp;

    @TableField("field_key")
    private String fieldKey;

    /** 展示用中文名，非表字段。 */
    @TableField(exist = false)
    private String name;

    @TableField("field_value")
    private String fieldValue;

    /** 分类名称（站位） */
    @TableField("category_name")
    private String categoryName;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
