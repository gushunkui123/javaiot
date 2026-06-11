package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PLC设备连接信息 Entity
 */
@Data
@TableName("plc_device")
public class PlcDeviceEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** PLC IP 地址 */
    private String ip;

    /** 设备名称 */
    private String deviceName;

    /** PLC/设备型号说明 */
    private String deviceType;

    /** 通讯协议标识 */
    private String protocol;

    /** 协议端口（S7 常用 102或者另外的502） */
    private Integer port;

    /** 机架号 */
    private Short rack;

    /** 槽号 */
    private Short slot;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

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

     /** 获取状态（0-离线 1-在线） */
     private Integer acquisitionStatus;

     /** 是否接入数据库（0-否 1-是） */
     private Integer isConnectedDb;
}
