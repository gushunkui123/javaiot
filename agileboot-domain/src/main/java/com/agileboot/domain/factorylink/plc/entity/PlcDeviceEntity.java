package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/** 从库表 plc_device：PLC 设备连接信息。 */
@Data
@TableName("plc_device")
public class PlcDeviceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("ip")
    private String ip;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_type")
    private String deviceType;

    @TableField("protocol")
    private String protocol;

    @TableField("port")
    private Integer port;

    @TableField("rack")
    private Integer rack;

    @TableField("slot")
    private Integer slot;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
