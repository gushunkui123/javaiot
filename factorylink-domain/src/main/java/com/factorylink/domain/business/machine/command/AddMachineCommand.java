package com.factorylink.domain.business.machine.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增设备命令
 */
@Data
@Schema(name = "新增设备命令")
public class AddMachineCommand {

    @Schema(description = "设备编码（唯一标识）")
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    protected String machineCode;

    @Schema(description = "设备名称")
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    protected String machineName;

    @Schema(description = "设备类型: MAIN_SCALE / MICRO_SCALE / LABELING_MACHINE")
    @NotBlank(message = "设备类型不能为空")
    @Size(max = 30, message = "设备类型长度不能超过30个字符")
    protected String deviceType;

    @Schema(description = "所属产线")
    @Size(max = 50, message = "产线名称长度不能超过50个字符")
    protected String productionLine;

    @Schema(description = "是否启用")
    protected Boolean enabled = true;

    @Schema(description = "IP地址")
    @Size(max = 50, message = "IP地址长度不能超过50个字符")
    protected String ip;

    @Schema(description = "端口号")
    protected Integer port;

    @Schema(description = "查询接口地址（磅秤使用）")
    @Size(max = 255, message = "查询接口地址长度不能超过255个字符")
    protected String apiUrl;

    @Schema(description = "写入接口地址（磅秤使用）")
    @Size(max = 255, message = "写入接口地址长度不能超过255个字符")
    protected String apiWriteUrl;

    @Schema(description = "工厂编号（磅秤使用）")
    @Size(max = 50, message = "工厂编号长度不能超过50个字符")
    protected String plant;

    @Schema(description = "磅秤系统设备编号")
    protected Integer scaleMachineId;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    protected String remark;

}
