package com.factorylink.domain.business.machine.dto;

import cn.hutool.core.bean.BeanUtil;
import com.factorylink.domain.business.machine.db.BizMachineEntity;
import com.factorylink.domain.common.audit.AuditableDTO;
import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备信息 DTO
 */
@Data
@NoArgsConstructor
public class MachineDTO implements AuditableDTO {

    public MachineDTO(BizMachineEntity entity) {
        if (entity != null) {
            BeanUtil.copyProperties(entity, this);
        }
    }

    private Long machineId;

    private String machineCode;

    private String machineName;

    private String deviceType;

    private Boolean enabled;

    private String ip;

    private Integer port;

    private String apiUrl;

    private String apiWriteUrl;

    private String plant;

    private Integer scaleMachineId;

    private String remark;

    private Long creatorId;

    private String creatorName;

    private Date createTime;

    private Long updaterId;

    private String updaterName;

    private Date updateTime;

}
