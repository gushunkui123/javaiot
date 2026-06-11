package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 传感器设备表
 */
@Data
@TableName("sensor_device")
public class SensorDeviceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备名称 */
    private String deviceName;

    /** 照片 */
    private String photo;

    /** 传感器类型 */
    private String sensorType;

    /** 协议类型 */
    private String protocolType;

    /** 获取方式 */
    private String acquisitionMethod;

    /** 设备位置 */
    private String deviceLocation;

    /** 获取状态 0-离线 1-在线 */
    private Integer acquisitionStatus;

    /** 是否接入数据库 0-否 1-是 */
    private Integer isConnectedDb;

    /** 传感器点位 */
    private String sensorPoint;

    /** 获取频率(秒) */
    private Integer acquisitionFrequency;

    /** 型号 */
    private String model;

    /** 标准值 */
    private String standardValue;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 删除标志 0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;
}
