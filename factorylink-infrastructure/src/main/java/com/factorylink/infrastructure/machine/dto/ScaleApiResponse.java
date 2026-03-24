package com.factorylink.infrastructure.machine.dto;

import java.util.List;
import lombok.Data;

/**
 * 磅秤设备 API 通用响应结构
 */
@Data
public class ScaleApiResponse<T> {

    private Integer success;
    private String rtnmsg;
    private List<T> rtndata;

    public boolean isSuccess() {
        return success != null && success == 1;
    }
}
