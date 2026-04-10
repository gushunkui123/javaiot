package com.factorylink.domain.business.machine.db;

import com.factorylink.common.core.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 设备信息表
 */
@Getter
@Setter
@TableName("biz_machine")
@Schema(name = "BizMachineEntity对象", description = "设备信息表")
public class BizMachineEntity extends BaseEntity<BizMachineEntity> {

    private static final long serialVersionUID = 1L;

    @Schema(description = "设备ID")
    @TableId(value = "machine_id", type = IdType.AUTO)
    private Long machineId;

    @Schema(description = "设备编码")
    @TableField("machine_code")
    private String machineCode;

    @Schema(description = "设备名称")
    @TableField("machine_name")
    private String machineName;

    @Schema(description = "设备类型: MAIN_SCALE / MICRO_SCALE / LABELING_MACHINE")
    @TableField("device_type")
    private String deviceType;

    @Schema(description = "是否启用（0-禁用 1-启用）")
    @TableField("enabled")
    private Boolean enabled;

    @Schema(description = "IP地址")
    @TableField("ip")
    private String ip;

    @Schema(description = "端口号")
    @TableField("port")
    private Integer port;

    @Schema(description = "查询接口地址（磅秤使用）")
    @TableField("api_url")
    private String apiUrl;

    @Schema(description = "写入接口地址（磅秤使用）")
    @TableField("api_write_url")
    private String apiWriteUrl;

    @Schema(description = "工厂编号（磅秤使用）")
    @TableField("plant")
    private String plant;

    @Schema(description = "磅秤系统设备编号")
    @TableField("scale_machine_id")
    private Integer scaleMachineId;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "在线状态（0-离线 1-在线）")
    @TableField("online_status")
    private Boolean onlineStatus;

    @Schema(description = "最后检测时间")
    @TableField("last_check_time")
    private Date lastCheckTime;

    @Override
    public Serializable pkVal() {
        return this.machineId;
    }

}
