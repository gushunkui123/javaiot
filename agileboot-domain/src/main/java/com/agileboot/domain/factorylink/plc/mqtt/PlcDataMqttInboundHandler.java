package com.agileboot.domain.factorylink.plc.mqtt;

import cn.hutool.core.util.StrUtil;
import com.agileboot.domain.factorylink.plc.service.EnvironmentDataService;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.service.ShootMachineService;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
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

    // plc数据
    private final PlcDataService plcDataService;
    // 环境数据
    private final EnvironmentDataService environmentDataService;
    // mqtt配置
    private final EmqxProperties emqxProperties;
    // 机台设备
    private final ShootMachineService shootMachineService;
    // 报警服务
    private final ShootRuleAlarmService shootRuleAlarmService;

    @ServiceActivator(inputChannel = EmqxMqttConfiguration.INBOUND_CHANNEL)
    public void handle(Message<?> message) {
        Object topicHeader = message.getHeaders().get("mqtt_receivedTopic");
        String topic = topicHeader != null ? topicHeader.toString().trim() : "";
        if (StrUtil.isBlank(topic)) {
            return;
        }
        Object payload = message.getPayload();
        if (!(payload instanceof String body)) {
            log.warn("MQTT expected string payload, got {}", payload != null ? payload.getClass() : "null");
            return;
        }
    // 环境数据处理
        if (topic.equals(emqxProperties.resolveEnvironmentDataTopic())) {
            environmentDataService.ingest(topic, body);
            return;
        }

        ShootMachineEntity machine =
                shootMachineService.getOne(
                        Wrappers.<ShootMachineEntity>lambdaQuery().eq(ShootMachineEntity::getTopic, topic));
        //  如果是已知的机台设备，则处理PLC数据
        if (machine != null) {
            plcDataService.ingestFlatJsonTelemetry(machine.getId(), machine.getMachineName(), body);
            // 报警检测
            shootRuleAlarmService.detectAndCreateAlarms(machine.getId(), body);
            return;
        }
        // 如果是未知的机台设备，则处理通用PLC数据
        if (topic.equals(emqxProperties.resolvePlcDataTopic())) {
            plcDataService.ingestFlatJsonTelemetry(null, emqxProperties.resolvePlcDataDeviceName(), body);
            return;
        }

        log.trace("MQTT skip topic={}", topic);
    }

}
