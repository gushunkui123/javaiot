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

    /** 排序号：越小越靠前；默认 10000，未勾选可设为更大值 */
    @TableField("sort_order")
    private Integer sortOrder = 10000;

    /** 勾选标记色；未勾选为 null */
    @TableField("mark_color")
    private String markColor;

    @TableField("unit")
    private String unit;

    @TableField("current_value")
    private String currentValue;

    public String getCurrentValue() {
        if (currentValue == null) {
            return null;
        }
        if ("off".equalsIgnoreCase(currentValue)) {
            return "关";
        } else if ("on".equalsIgnoreCase(currentValue)) {
            return "开";
        } else {
            return currentValue;
        }
    }
    
    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;
}
