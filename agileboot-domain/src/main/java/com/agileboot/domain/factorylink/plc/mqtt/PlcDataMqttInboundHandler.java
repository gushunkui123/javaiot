package com.agileboot.domain.factorylink.plc.mqtt;

import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.infrastructure.mqtt.EmqxMqttConfiguration;
import com.agileboot.infrastructure.mqtt.EmqxProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "factory-link.emqx", name = "enabled", havingValue = "true")
public class PlcDataMqttInboundHandler {

    private final PlcDataService plcDataService;
    private final EmqxProperties emqxProperties;

    //消息接收
    @ServiceActivator(inputChannel = EmqxMqttConfiguration.INBOUND_CHANNEL)
    public void handle(Message<?> message) {
        Object topicHeader = message.getHeaders().get("mqtt_receivedTopic");
        String topic = topicHeader != null ? topicHeader.toString() : "";
        if (!topic.equals(emqxProperties.resolvePlcDataTopic())) {
            log.trace("MQTT skip topic={}", topic);
            return;
        }
        Object payload = message.getPayload();
        if (!(payload instanceof String body)) {
            log.warn("MQTT plc_data expected string payload, got {}", payload != null ? payload.getClass() : "null");
            return;
        }
        String deviceName = emqxProperties.resolvePlcDataDeviceName();
        plcDataService.ingestFlatJsonTelemetry(deviceName, body);
    }
}
