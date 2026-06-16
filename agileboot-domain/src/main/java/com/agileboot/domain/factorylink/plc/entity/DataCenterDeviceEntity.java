package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * iot_data_center 库 plc_data_device 表实体。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("plc_data_device")
public class DataCenterDeviceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("ip")
    private String ip;

    @TableField("device_name")
    private String deviceName;

    @TableField("rack")
    private Integer rack;

    @TableField("slot")
    private Integer slot;

    @TableField("device_type")
    private String deviceType;

    @TableField("protocol")
    private String protocol;

    @TableField("port")
    private Integer port;

    @TableField("workshop_id")
    private Long workshopId;

    @TableField("area_id")
    private Long areaId;

    @TableField("category_id")
    private Long categoryId;

    @TableField("status")
    private String status;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField("update_by")
    private String updateBy;

    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableField("remark")
    private String remark;

    @TableField("connect_status")
    private String connectStatus;

    @TableField("mqtt_config_id")
    private Long mqttConfigId;

    @TableField("device_code")
    private String deviceCode;
}
