package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("shoot_mold_rule")
public class ShootMoldRuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("mold_id")
    private Long moldId;

    @TableField("field_code")
    private String fieldCode;

    @TableField("field_name")
    private String fieldName;

    @TableField("min_value")
    private BigDecimal minValue;

    @TableField("max_value")
    private BigDecimal maxValue;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    /** 模具型号（非表字段，按模具查询规则时填充） */
    @TableField(exist = false)
    private String moldModel;

    /** 模具颜色（非表字段，按模具查询规则时填充） */
    @TableField(exist = false)
    private String color;
}
