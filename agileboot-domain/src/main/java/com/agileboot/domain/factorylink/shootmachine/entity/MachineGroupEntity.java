package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 射出机分组（机台分组编码与显示名，车间1/车间2 ...）
 */
@Data
@TableName("machine_group")
public class MachineGroupEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 分组编码（shoot_machine.machine_group 存此值，如 workshop_1） */
    @TableField("group_code")
    private String groupCode;

    /** 分组显示名（如 车间1） */
    @TableField("group_name")
    private String groupName;

    /** 排序（升序，越小越靠前） */
    @TableField("sort")
    private Integer sort;

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
}
