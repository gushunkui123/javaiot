package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("field_dimension_config")
public class FieldDimensionConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dimension_name")
    private String dimensionName;

    @TableField("max_value")
    private Integer maxValue;

    @TableField("description")
    private String description;

    @TableField("enabled")
    private Boolean enabled;
}
