package com.agileboot.infrastructure.machine.client;

import com.agileboot.infrastructure.machine.dto.ScaleApiResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 磅秤设备 API 通用客户端
 * <p>
 * 封装 GET 查询与 POST 写入，写入操作支持重试（3次/3秒间隔）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScaleApiClient {

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_INTERVAL_MS = 3000L;

    private final RestTemplate scaleRestTemplate;
    private final ObjectMapper objectMapper;

    /**
     * GET 查询（实时查询设备数据）
     *
     * @param apiUrl 查询接口基础 URL
     * @param params 查询参数
     * @param dataType rtndata 元素类型
     */
    public <T> ScaleApiResponse<T> query(String apiUrl, Map<String, String> params, Class<T> dataType) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiUrl);
        params.forEach(builder::queryParam);
        String url = builder.toUriString();

        log.info("磅秤设备查询请求: {}", url);
        try {
            ResponseEntity<String> responseEntity = scaleRestTemplate.getForEntity(url, String.class);
            return parseResponse(responseEntity.getBody(), dataType);
        } catch (RestClientException e) {
            log.error("磅秤设备查询失败: {}", url, e);
            ScaleApiResponse<T> failResponse = new ScaleApiResponse<>();
            failResponse.setSuccess(0);
            failResponse.setRtnmsg("设备连接失败: " + e.getMessage());
            return failResponse;
        }
    }

    /**
     * POST 写入（带重试逻辑）
     *
     * @param apiWriteUrl 写入接口 URL
     * @param requestBody 请求体对象
     * @return 写入响应
     */
    public ScaleApiResponse<Void> write(String apiWriteUrl, Object requestBody) {
        return writeWithRetry(apiWriteUrl, requestBody, MAX_RETRIES);
    }

    private ScaleApiResponse<Void> writeWithRetry(String apiWriteUrl, Object requestBody, int remainingRetries) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("磅秤设备写入请求: url={}, body={}", apiWriteUrl, objectMapper.writeValueAsString(requestBody));
        } catch (Exception ignored) {
            // JSON 序列化日志失败不影响主流程
        }

        try {
            ResponseEntity<String> responseEntity = scaleRestTemplate.postForEntity(apiWriteUrl, httpEntity, String.class);
            ScaleApiResponse<Void> response = parseResponse(responseEntity.getBody(), Void.class);

            if (response.isSuccess()) {
                log.info("磅秤设备写入成功: url={}", apiWriteUrl);
                return response;
            }

            // 设备返回失败（业务错误，非网络错误），不重试
            log.warn("磅秤设备写入业务失败: url={}, msg={}", apiWriteUrl, response.getRtnmsg());
            return response;

        } catch (RestClientException e) {
            if (remainingRetries > 1) {
                log.warn("磅秤设备写入异常，{}秒后重试（剩余{}次）: {}", RETRY_INTERVAL_MS / 1000, remainingRetries - 1, e.getMessage());
                sleep(RETRY_INTERVAL_MS);
                return writeWithRetry(apiWriteUrl, requestBody, remainingRetries - 1);
            }

            log.error("磅秤设备写入失败（已耗尽重试次数）: url={}", apiWriteUrl, e);
            ScaleApiResponse<Void> failResponse = new ScaleApiResponse<>();
            failResponse.setSuccess(0);
            failResponse.setRtnmsg("设备连接失败（已重试" + MAX_RETRIES + "次）: " + e.getMessage());
            return failResponse;
        }
    }

    private <T> ScaleApiResponse<T> parseResponse(String body, Class<T> dataType) {
        try {
            JavaType responseType = objectMapper.getTypeFactory()
                .constructParametricType(ScaleApiResponse.class, dataType);
            return objectMapper.readValue(body, responseType);
        } catch (Exception e) {
            log.error("磅秤设备响应解析失败: body={}", body, e);
            ScaleApiResponse<T> failResponse = new ScaleApiResponse<>();
            failResponse.setSuccess(0);
            failResponse.setRtnmsg("响应解析失败: " + e.getMessage());
            return failResponse;
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
