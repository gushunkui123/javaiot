package com.agileboot.domain.factorylink.plc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 机台 dataCode/configCode 映射配置（替代原 ShootMachineCode 枚举中的硬编码条目）。
 */
@Data
@TableName("plc_machine_data_config")
public class PlcMachineDataConfigEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("machine_name")
    private String machineName;

    @TableField("data_code")
    private String dataCode;

    @TableField("config_code")
    private String configCode;

    @TableField("enabled")
    private Boolean enabled;

    @TableField("remark")
    private String remark;
}
