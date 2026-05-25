package com.agileboot.infrastructure.mqtt;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** 自建 EMQX（MQTT）连接参数； */
@Data
@ConfigurationProperties(prefix = "factory-link.emqx")
public class EmqxProperties {

    private boolean enabled;

    private String brokerUrl;

    private String clientId;

    private String username;

    private String password;

    private List<String> inboundTopics;

    private int completionTimeout;

    private int inboundQos;

    /** 与 JSON 遥测对应的 MQTT 主题名，默认 plc_data */
    private String plcDataTopic;

    /**
     * 写入 plc_data.device_name。
     */
    private String plcDataDeviceName;

    /** 环境数据 MQTT 主题，默认 HWY01 */
    private String environmentDataTopic;

    public String resolvePlcDataTopic() {
        return plcDataTopic != null && !plcDataTopic.isBlank() ? plcDataTopic.trim() : "plc_data";
    }

    public String resolvePlcDataDeviceName() {
        return plcDataDeviceName != null && !plcDataDeviceName.isBlank()
                ? plcDataDeviceName.trim()
                : "unknown";
    }

    public String resolveEnvironmentDataTopic() {
        return environmentDataTopic != null && !environmentDataTopic.isBlank()
                ? environmentDataTopic.trim()
                : "HWY01";
    }
}
