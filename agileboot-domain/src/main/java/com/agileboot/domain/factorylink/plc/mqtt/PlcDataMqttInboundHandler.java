package com.agileboot.domain.factorylink.plc.mqtt;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.agileboot.domain.factorylink.plc.service.EnvironmentDataService;
import com.agileboot.domain.factorylink.plc.service.PlcDataService;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMachineEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootMoldRuleEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootRuleAlarmEntity;
import com.agileboot.domain.factorylink.shootmachine.entity.ShootStationScheduleEntity;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMachineMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootMoldRuleMapper;
import com.agileboot.domain.factorylink.shootmachine.mapper.ShootStationScheduleMapper;
import com.agileboot.domain.factorylink.shootmachine.service.ShootRuleAlarmService;
import com.agileboot.infrastructure.mqtt.EmqxMqttConfiguration;
import com.agileboot.infrastructure.mqtt.EmqxProperties;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
    private final ShootMachineMapper shootMachineMapper;
    // 模具规则
    private final ShootMoldRuleMapper shootMoldRuleMapper;
    // 站位排期
    private final ShootStationScheduleMapper shootStationScheduleMapper;
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
                shootMachineMapper.selectOne(
                        Wrappers.<ShootMachineEntity>lambdaQuery().eq(ShootMachineEntity::getTopic, topic));
        //  如果是已知的机台设备，则处理PLC数据
        if (machine != null) {
            plcDataService.ingestFlatJsonTelemetry(machine.getId(), machine.getMachineName(), body);
            // 报警检测
            detectAndCreateAlarms(machine.getId(), body);
            return;
        }
        // 如果是未知的机台设备，则处理通用PLC数据
        if (topic.equals(emqxProperties.resolvePlcDataTopic())) {
            plcDataService.ingestFlatJsonTelemetry(null, emqxProperties.resolvePlcDataDeviceName(), body);
            return;
        }

        log.trace("MQTT skip topic={}", topic);
    }

    /**
     * 检测PLC数据并创建报警记录
     */
    private void detectAndCreateAlarms(Long machineId, String jsonPayload) {
        JSONObject root = parseJson(jsonPayload);
        if (root == null) {
            return;
        }
    // 获取当前排期信息
        List<ShootStationScheduleEntity> schedules = shootStationScheduleMapper.selectListCurrentByMachineIdWithMold(
                machineId, LocalDateTime.now());

        for (ShootStationScheduleEntity schedule : schedules) {
            if (schedule.getMoldId() == null || schedule.getStationId() == null) {
                continue;
            }
            // 获取模具规则信息
            List<ShootMoldRuleEntity> rules = shootMoldRuleMapper.selectListByMoldIdWithMold(schedule.getMoldId());
            for (ShootMoldRuleEntity rule : rules) {
                // 跳过禁用的规则信息
                if (Boolean.FALSE.equals(rule.getEnabled())) {
                    continue;
                }
                checkRuleAndCreateAlarm(machineId, schedule.getStationId(), rule, root);
            }
        }
    }

    private JSONObject parseJson(String json) {
        try {
            return JSONUtil.parseObj(json);
        } catch (Exception e) {
            log.warn("PLC JSON parse failed: {}", e.getMessage());
            return null;
        }
    }

    private void checkRuleAndCreateAlarm(Long machineId, Long stationId, ShootMoldRuleEntity rule, JSONObject root) {
        String fieldValue = root.getStr(rule.getFieldCode());
        if (StrUtil.isBlank(fieldValue)) {
            return;
        }

        BigDecimal currentValue = Convert.toBigDecimal(fieldValue, null);
        if (currentValue == null) {
            log.warn("Field value convert failed: fieldCode={}, value={}", rule.getFieldCode(), fieldValue);
            return;
        }
        // 判断是否超出范围
        boolean isOutOfRange = currentValue.compareTo(rule.getMinValue()) < 0
                || currentValue.compareTo(rule.getMaxValue()) > 0;
        if (isOutOfRange) {
            // 创建报警记录
            createAlarm(machineId, stationId, rule, currentValue);
        }
    }

    /**
     * 创建报警记录（含去重）
     */
    private void createAlarm(Long machineId, Long stationId, ShootMoldRuleEntity rule, BigDecimal currentValue) {
        ShootRuleAlarmEntity alarm = new ShootRuleAlarmEntity();
        alarm.setMachineId(machineId);
        alarm.setStationId(stationId);
        alarm.setMoldId(rule.getMoldId());
        alarm.setRuleId(rule.getId());
        alarm.setFieldCode(rule.getFieldCode());
        alarm.setFieldName(rule.getFieldName());
        alarm.setMinValue(rule.getMinValue());
        alarm.setMaxValue(rule.getMaxValue());
        alarm.setCurrentValue(currentValue);
        alarm.setAlarmTime(LocalDateTime.now());
        alarm.setHandleStatus("false");
        try {
            shootRuleAlarmService.create(alarm);
        } catch (Exception e) {
            log.error("Create alarm failed: machineId={}, stationId={}, ruleId={}, fieldCode={}",
                    machineId, stationId, rule.getId(), rule.getFieldCode(), e);
        }
    }
}
