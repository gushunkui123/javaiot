package com.factorylink.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 外部设备连接配置
 *
 * @deprecated 设备配置已迁移到数据库（biz_machine 表），请使用 MachineConfigProvider 替代。
 *             此类将在后续版本移除。
 */
@Deprecated
@Component
@ConfigurationProperties(prefix = "factorylink.machines")
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
         * 设备编号（对应磅秤系统的 machineId 参数）
         */
        private Integer machineId = 1;

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

        /**
         * 工厂编号（对应磅秤系统的 plant 参数）
         */
        private String plant = "";
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
