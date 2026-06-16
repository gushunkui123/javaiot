package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * iot_data_center 库 plc_data_point 表实体。
 */
@Data
@JsonInclude(Include.NON_NULL)
@TableName("plc_data_point")
public class DataCenterPointEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_id")
    private Long deviceId;

    @TableField("area")
    private String area;

    @TableField("`number`")
    private Integer number;

    @TableField("byte_offset")
    private Integer byteOffset;

    @TableField("bit_offset")
    private Integer bitOffset;

    @TableField("data_type")
    private String dataType;

    @TableField("display_name")
    private String displayName;

    @TableField("unit")
    private String unit;

    @TableField("mark_color")
    private String markColor;

    @TableField("current_value")
    private String currentValue;

    @TableField("category_id")
    private Long categoryId;

    @TableField("data_updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataUpdatedAt;

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

    @TableField("data_code")
    private String dataCode;

    @TableField("tags")
    private String tags;
}
