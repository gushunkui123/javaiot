package com.agileboot.domain.factorylink.shootmachine.dto;

import java.util.List;
import lombok.Data;

@Data
public class BatchHandleAlarmDTO {

    private List<Long> ids;

    private String handleRemark;
}
