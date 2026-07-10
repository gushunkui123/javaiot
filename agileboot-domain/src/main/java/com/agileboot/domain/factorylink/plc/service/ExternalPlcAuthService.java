package com.agileboot.domain.factorylink.plc.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部 PLC 服务鉴权 & HTTP 调用工具
 * <p>
 * 职责：登录拿 token，用 token 发起带鉴权的 GET 请求。
 * 后续如有其他外部接口调用，统一走这里。
 */
@Service
@Slf4j
public class ExternalPlcAuthService {

    private final RestTemplate restTemplate;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${factory-link.external-plc.login-url:http://10.0.100.225:8088/login}")
    private String loginUrl;

    @Value("${factory-link.external-plc.username:admin}")
    private String username;

    @Value("${factory-link.external-plc.password:admin123}")
    private String password;

    public ExternalPlcAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ======================== 登录 ========================

    /**
     * 登录并返回 token，失败返回 null（错误日志已打印）
     */
    public String login() {
        log.info("外部 PLC 登录: {}", loginUrl);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, String> body = new LinkedHashMap<>();
            body.put("username", username);
            body.put("password", password);

            ResponseEntity<String> resp = restTemplate.exchange(
                    loginUrl, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);
            log.info("外部 PLC 登录返回: {}", resp.getBody());

            String token = extractToken(resp.getBody());
            if (token != null) {
                log.info("外部 PLC 登录成功, token={}", token);
            } else {
                log.error("外部 PLC 登录失败，无法提取 token，响应: {}", resp.getBody());
            }
            return token;
        } catch (Exception e) {
            log.error("外部 PLC 登录异常: {}", e.getMessage(), e);
            return null;
        }
    }

    // ======================== 带鉴权的 GET 请求 ========================

    /**
     * 用 Bearer token 调用外部接口，返回响应体字符串；失败返回 null
     */
    public String getWithBearerToken(String token, String url) {
        log.info("调用外部 PLC 接口: {}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            ResponseEntity<String> resp = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
            log.info("外部 PLC 接口返回 [{}] 状态码={} 响应体={}", url, resp.getStatusCode(), resp.getBody());
            return resp.getBody();
        } catch (Exception e) {
            log.error("调用外部 PLC 接口失败 [{}]: {}", url, e.getMessage(), e);
            return null;
        }
    }

    // ======================== token 解析 ========================

    /**
     * 用 Bearer token 调用外部接口，返回完全转换为标准 Java 类型的 Map；失败返回 null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getJsonMapWithBearerToken(String token, String url) {
        String body = getWithBearerToken(token, url);
        if (body == null) return null;
        try {
            return MAPPER.readValue(body, Map.class);
        } catch (Exception e) {
            log.warn("解析外部响应JSON失败, url={}, 原文={}", url, body);
            return null;
        }
    }

    /**
     * 从登录响应 JSON 中提取 token，兼容常见返回格式
     */
    private String extractToken(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            JSONObject obj = JSONUtil.parseObj(json);
            // 尝试 data.token
            JSONObject data = obj.getJSONObject("data");
            if (data != null && data.containsKey("token")) {
                return data.getStr("token");
            }
            // 尝试顶层 token
            if (obj.containsKey("token")) {
                return obj.getStr("token");
            }
        } catch (Exception e) {
            log.warn("解析登录响应失败: {}", e.getMessage());
        }
        return null;
    }
}
