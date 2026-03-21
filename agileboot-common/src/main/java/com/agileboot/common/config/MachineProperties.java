package com.agileboot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 外部设备连接配置
 */
@Component
@ConfigurationProperties(prefix = "agileboot.machines")
@Data
public class MachineProperties {

    /**
     * 主磅
     */
    private ScaleConfig mainScale;

    /**
     * 微量
     */
    private ScaleConfig microScale;

    /**
     * 贴标机
     */
    private LabelingMachineConfig labelingMachine;

    /**
     * 磅称设备配置（主磅/微量共用结构）
     */
    @Data
    public static class ScaleConfig {

        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 设备IP地址，如 192.168.99.202
         */
        private String ip;

        /**
         * 查询接口地址
         */
        private String apiUrl;

        /**
         * 写入接口地址
         */
        private String apiWriteUrl;
    }

    /**
     * 贴标机配置（TCP 协议）
     */
    @Data
    public static class LabelingMachineConfig {

        /**
         * 是否启用
         */
        private boolean enabled = true;

        private String host;

        private Integer port;
    }
}
