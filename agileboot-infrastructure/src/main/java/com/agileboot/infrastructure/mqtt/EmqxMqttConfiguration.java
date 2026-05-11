package com.agileboot.infrastructure.mqtt;

import java.util.List;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.messaging.MessageChannel;

@Configuration
@EnableIntegration
@EnableConfigurationProperties(EmqxProperties.class)
@ConditionalOnProperty(prefix = "factory-link.emqx", name = "enabled", havingValue = "true")
public class EmqxMqttConfiguration {

    public static final String INBOUND_CHANNEL = "emqxInboundChannel";

    // 创建 MQTT 客户端工厂
    @Bean
    public MqttPahoClientFactory emqxMqttClientFactory(EmqxProperties props) {
        if (props.getBrokerUrl() == null || props.getBrokerUrl().isBlank()) {
            throw new IllegalStateException("请在 yml 中配置 factory-link.emqx.broker-url");
        }
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(buildConnectOptions(props));
        return factory;
    }

    // 创建 MQTT 连接选项
    private static MqttConnectOptions buildConnectOptions(EmqxProperties props) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] {props.getBrokerUrl()});
        if (props.getUsername() != null && !props.getUsername().isBlank()) {
            options.setUserName(props.getUsername());
        }
        if (props.getPassword() != null && !props.getPassword().isEmpty()) {
            options.setPassword(props.getPassword().toCharArray());
        }
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        return options;
    }

    // 创建 MQTT 输入通道
    @Bean(name = INBOUND_CHANNEL)
    public MessageChannel emqxInboundChannel() {
        return new DirectChannel();
    }

    // 创建 MQTT 输入适配器 MQTTid 工厂 主题
    @Bean
    public MqttPahoMessageDrivenChannelAdapter emqxMqttInboundAdapter(
            EmqxProperties props, MqttPahoClientFactory emqxMqttClientFactory) {
        List<String> topics = props.getInboundTopics();
        if (topics == null || topics.stream().noneMatch(t -> t != null && !t.isBlank())) {
            throw new IllegalStateException(
                    "factory-link.emqx.enabled=true 时请在 factory-link.emqx.inbound-topics "
                            + "中配置至少一个订阅主题（与边缘网关在 EMQX 上的约定一致）。");
        }
        String[] topicArray =
                topics.stream().filter(t -> t != null && !t.isBlank()).distinct().toArray(String[]::new);
        if (props.getClientId() == null || props.getClientId().isBlank()) {
            throw new IllegalStateException("请在 yml 中配置 factory-link.emqx.client-id");
        }
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        props.getClientId() + "-inbound", emqxMqttClientFactory, topicArray);
        int qos = props.getInboundQos();
        if (qos < 0 || qos > 2) {
            throw new IllegalStateException("factory-link.emqx.inbound-qos 必须为 0、1 或 2");
        }
        adapter.setQos(qos);
        adapter.setCompletionTimeout(props.getCompletionTimeout());
        // 指定输出通道名称,消息路由到哪
        adapter.setOutputChannelName(INBOUND_CHANNEL);
        return adapter;
    }
}