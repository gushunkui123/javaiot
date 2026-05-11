package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * PLC 对应表 plc_data。
 */
@Getter
@Setter
@TableName("plc_data")
public class PlcDataEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_name")
    private String deviceName;

    /** 业务上的采集时间，映射列名 timestamp */
    @TableField("`timestamp`")
    private Date dataTimestamp;

    @TableField("field_key")
    private String fieldKey;

    /** 展示用中文名，非表字段。 */
    @TableField(exist = false)
    private String name;

    @TableField("field_value")
    private String fieldValue;

    @TableField("create_time")
    private Date createTime;

    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
