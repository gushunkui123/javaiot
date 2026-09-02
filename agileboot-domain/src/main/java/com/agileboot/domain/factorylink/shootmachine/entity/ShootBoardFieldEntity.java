package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;

/**
 * 看板顶部固定展示字段（前端渲染用实体）。
 * 基于 shoot_board_field_config 配置，并合并 plc_data_latest 实时值与未处理报警状态。
 * fieldValue / alarming / alarmLevel 为非表字段，仅用于接口返回。
 */
@Data
public class ShootBoardFieldEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 英文 field_key */
    private String fieldKey;

    /** 显示名 */
    private String displayName;

    /** 分组名 */
    private String groupName;

    /** 实时值（取不到为 null） */
    @TableField(exist = false)
    private String fieldValue;

    /** 是否处于报警状态（匹配 shoot_rule_alarm.field_code 且未处理） */
    @TableField(exist = false)
    private Boolean alarming;

    /** 报警等级（yellow/red），无报警为 null */
    @TableField(exist = false)
    private String alarmLevel;
}
