package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 数据表（小类）
 */
@Data
@TableName("sensor_data_item")
public class SensorDataItemEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属大类ID */
    private Long categoryId;

    /** 数据小项名称（如：合模压力、开模压力） */
    private String itemName;

    /** 数据值 */
    private String itemValue;

    /** 标准值 */
    private String standardValue;

    /** 单位（如：bar、g、s） */
    private String unit;

    /** 排序号 */
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 采集状态 */
    private String status;

    /** 是否接入数据库 1=是 0=否 */
    private Integer isConnectedDb;

    /** 传感器点位 */
    private String sensorPoint;

    /** 采集频率 */
    private Integer fetchFrequency;

    /** 设备型号 */
    private String model;
}
