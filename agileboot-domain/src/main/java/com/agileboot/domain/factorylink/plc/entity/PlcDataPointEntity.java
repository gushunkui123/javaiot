package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 从库表 plc_data_point：数据点定义与当前值。
 */
@Data
@TableName("plc_data_point")
public class PlcDataPointEntity implements Serializable {

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

    @TableField("current_value")
    private Double currentValue;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getSwitchText() {
        if (currentValue == null) {
            return null;
        }
        if (currentValue == 0) {
            return "关";
        }
        if (currentValue == 1) {
            return "开";
        }
        return null;
    }

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
