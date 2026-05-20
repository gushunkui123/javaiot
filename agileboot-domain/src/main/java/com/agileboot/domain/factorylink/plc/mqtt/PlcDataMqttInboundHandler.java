package com.agileboot.domain.factorylink.plc.mqtt;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineMapper;
import com.agileboot.infrastructure.mqtt.EmqxMqttConfiguration;
import com.agileboot.infrastructure.mqtt.EmqxProperties;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
    private final ShootMachineMapper shootMachineMapper;

    @ServiceActivator(inputChannel = EmqxMqttConfiguration.INBOUND_CHANNEL)
    public void handle(Message<?> message) {
        Object topicHeader = message.getHeaders().get("mqtt_receivedTopic");
        String topic = topicHeader != null ? topicHeader.toString().trim() : "";
        if (StrUtil.isBlank(topic)) {
            return;
        }
        Object payload = message.getPayload();
        if (!(payload instanceof String body)) {
            log.warn("MQTT plc_data expected string payload, got {}", payload != null ? payload.getClass() : "null");
            return;
        }

        ShootMachineEntity machine =
                shootMachineMapper.selectOne(
                        Wrappers.<ShootMachineEntity>lambdaQuery().eq(ShootMachineEntity::getTopic, topic));
        if (machine != null) {
            plcDataService.ingestFlatJsonTelemetry(
                    machine.getId(), machine.getMachineName(), body);
            return;
        }

        if (topic.equals(emqxProperties.resolvePlcDataTopic())) {
            plcDataService.ingestFlatJsonTelemetry(
                    null, emqxProperties.resolvePlcDataDeviceName(), body);
            return;
        }

        log.trace("MQTT skip topic={}", topic);
    }
}
