package com.agileboot.domain.factorylink.shootmachine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("shoot_machine")
public class ShootMachineEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("machine_name")
    private String machineName;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("remark")
    private String remark;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    /** MQTT 订阅主题，与 EMQX 发布 topic 一致 */
    @TableField("topic")
    private String topic;

    /** 最新 PLC 采集时间（非表字段） */
    @TableField(exist = false)
    private Date latestPlcDataTime;

    /** 最新采集时间在 2 分钟内为 true，否则停机（非表字段） */
    @TableField(exist = false)
    private Boolean running;
}
