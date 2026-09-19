package com.agileboot.domain.factorylink.plc.util;

import com.agileboot.common.utils.jackson.JacksonUtil;
import com.agileboot.domain.factorylink.plc.dto.MoldRulePushDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 安冬硬件平台告警规则下发客户端。
 * <p>
 * 负责把内部构建好的规则报文序列化为 JSON，并按安冬的 apiKey + secret 签名方案
 * POST 到其规则同步接口（默认 {@code /api/anton/rules/sync}）。
 * 签名实现复用 {@link ApiClientPostJson}（apiKey/timestamp/nonce/bodyDigest → X-Signature）。
 * <p>
 * 日志：下发前打印目标地址与请求报文（格式化），收到后打印响应报文，便于人工核对送到安冬的数据。
 */
@Slf4j
@Component
public class AntonRulePushClient {

    @Value("${factory-link.anton.enabled:false}")
    private boolean enabled;

    @Value("${factory-link.anton.base-url:}")
    private String baseUrl;

    @Value("${factory-link.anton.rules-sync-path:/api/anton/rules/sync}")
    private String rulesSyncPath;

    @Value("${factory-link.anton.api-key:}")
    private String apiKey;

    @Value("${factory-link.anton.api-secret:}")
    private String apiSecret;

    /** 是否启用规则下发 */
    public boolean isEnabled() {
        return enabled;
    }

    /** 目标接口完整地址（用于结果回显与日志） */
    public String getTargetUrl() {
        return baseUrl + rulesSyncPath;
    }

    /**
     * 推送规则报文到安冬平台。
     *
     * @param payload 已构建好的规则组列表
     * @return 第三方响应原文
     * @throws Exception 网络或签名异常
     */
    public String pushRules(List<MoldRulePushDTO> payload) throws Exception {
        String json = JacksonUtil.to(payload);
        log.info("[AntonPush] 下发告警规则, url={}, 分组数={}, 规则数={}",
                getTargetUrl(), payload.size(), countRules(payload));
        log.info("[AntonPush] 发送给安冬的请求报文: {}", prettyOrRaw(json));

        ApiClientPostJson client = new ApiClientPostJson(apiKey, apiSecret);
        String response = client.postJson(getTargetUrl(), json);

        log.info("[AntonPush] 安冬响应报文: {}", prettyOrRaw(response));
        return response;
    }

    /** 规则条数合计（分组内 rules 为空时按 0 计） */
    private int countRules(List<MoldRulePushDTO> payload) {
        return payload.stream()
                .mapToInt(group -> group.getRules() == null ? 0 : group.getRules().size())
                .sum();
    }

    /** 是合法 JSON 时格式化输出，否则原样返回（例如返回的是 HTML 错误页） */
    private String prettyOrRaw(String text) {
        if (text == null) {
            return null;
        }
        try {
            return JacksonUtil.isJson(text) ? JacksonUtil.format(text) : text;
        } catch (Exception e) {
            return text;
        }
    }
}
