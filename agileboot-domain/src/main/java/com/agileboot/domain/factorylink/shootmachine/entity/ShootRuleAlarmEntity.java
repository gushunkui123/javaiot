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
import java.util.List;
import lombok.Data;

@Data
@TableName("shoot_rule_alarm")
public class ShootRuleAlarmEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("machine_id")
    private Long machineId;

    @TableField("station_id")
    private Long stationId;

    @TableField("mold_id")
    private Long moldId;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("field_code")
    private String fieldCode;

    @TableField("field_name")
    private String fieldName;

    @TableField("min_value")
    private BigDecimal minValue;

    @TableField("max_value")
    private BigDecimal maxValue;

    @TableField("current_value")
    private BigDecimal currentValue;

    @TableField("alarm_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime alarmTime;

    @TableField("handle_status")
    private String handleStatus;

    @TableField("handle_remark")
    private String handleRemark;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    /** 机器名称（非表字段，关联查询时填充） */
    @TableField(exist = false)
    private String machineName;

    /** 站位名称（非表字段，关联查询时填充） */
    @TableField(exist = false)
    private String stationName;

    /** 模具型号（非表字段，关联查询时填充） */
    @TableField(exist = false)
    private String moldModel;

    /** 模具颜色（非表字段，关联查询时填充） */
    @TableField(exist = false)
    private String moldColor;

    /** 报警ID列表（非表字段，批量处理时传入） */
    @TableField(exist = false)
    private List<Long> alarmIds;


}
