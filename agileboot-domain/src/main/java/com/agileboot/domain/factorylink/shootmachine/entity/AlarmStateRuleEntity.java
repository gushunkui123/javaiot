package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("alarm_state_rule")
public class AlarmStateRuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("rule_code")
    private String ruleCode;

    @TableField("rule_name")
    private String ruleName;

    @TableField("source_internal_key")
    private String sourceInternalKey;

    @TableField("trigger_type")
    private String triggerType;

    @TableField("trigger_value")
    private String triggerValue;

    @TableField("warn_after_seconds")
    private Integer warnAfterSeconds;

    @TableField("stop_after_seconds")
    private Integer stopAfterSeconds;

    @TableField("alarm_level")
    private String alarmLevel = "yellow";

    @TableField("priority")
    private Integer priority = 0;

    @TableField("enabled")
    private Boolean enabled;
}
