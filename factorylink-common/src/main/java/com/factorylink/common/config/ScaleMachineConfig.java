package com.factorylink.common.config;

import lombok.Data;

/**
 * 磅秤设备配置（从数据库加载）
 */
@Data
public class ScaleMachineConfig {

    /**
     * 磅秤系统设备编号
     */
    private Integer scaleMachineId;

    /**
     * 工厂编号
     */
    private String plant;

    /**
     * 查询接口地址
     */
    private String apiUrl;

    /**
     * 写入接口地址
     */
    private String apiWriteUrl;

}
