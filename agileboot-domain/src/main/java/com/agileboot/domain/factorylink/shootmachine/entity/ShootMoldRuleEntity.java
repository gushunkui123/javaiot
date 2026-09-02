package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
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

    /** 维度类型：GLOBAL（整体单行，如模具温度/射出压力/硫化时间）/ STAGE（按阶段，如射枪温度/射出速度） */
    @TableField("dimension_type")
    private String dimensionType;

    /** 阶段号（STAGE 维度时有效，1~N；GLOBAL 为 null） */
    @TableField("stage")
    private Integer stage;

    @TableField("min_value")
    private BigDecimal minValue;

    @TableField("max_value")
    private BigDecimal maxValue;

    @TableField("enabled")
    private Boolean enabled;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.UPDATE)
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

    /** 站位ID（非表字段，按机台查询规则时填充） */
    @TableField(exist = false)
    private Long stationId;

    /** 站位编号（非表字段，按机台查询规则时填充） */
    @TableField(exist = false)
    private Integer stationNo;

    /** 站位名称（非表字段，按机台查询规则时填充） */
    @TableField(exist = false)
    private String stationName;

    /** 全局规则站位编号（数据库字段） */
    @TableField("station_no")
    private Integer globalStationNo;
}
