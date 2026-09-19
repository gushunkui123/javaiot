package com.agileboot.domain.factorylink.plc.util;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 安冬平台 GET 接口签名客户端。
 * <p>
 * 与 {@link ApiClientPostJson} 同属一套 {@code apiKey + apiSecret} 的 HMAC-SHA256 签名体系，差异有两点：
 * <ul>
 *     <li>GET 请求没有请求体，因此<b>不参与 bodyDigest</b>；</li>
 *     <li>业务筛选参数（分页、条件查询）同样位于 Query 上，<b>必须与公共参数一起参与签名</b>，
 *         否则第三方按实际收到的参数计算会与本地签名不一致。</li>
 * </ul>
 * 签名串为<b>全部 Query 参数</b>按参数名字典序拼接的 {@code key=value&key=value}，
 * 使用 apiSecret 做 HMAC-SHA256 后取十六进制（小写）写入 {@code X-Signature} 请求头。
 * <p>
 * 注意：<b>路径参数（如 {@code /device/{deviceId}/bindings} 中的 deviceId）按第三方约定不参与签名</b>，
 * 因此调用方只需把路径拼进 url，无需在 queryParams 中重复传递。
 */
public class ApiClientGetJson {

    private final String apiKey;
    private final String secret;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    public ApiClientGetJson(String apiKey, String secret) {
        this.apiKey = apiKey;
        this.secret = secret;
    }

    /**
     * 发送带签名的 GET 请求。
     *
     * @param url         接口地址（不含查询参数）
     * @param queryParams 业务查询参数（可为空），非空值会与公共参数一起参与签名
     * @return 服务端响应原文
     * @throws Exception 网络或签名异常
     */
    public String getJson(String url, Map<String, String> queryParams) throws Exception {
        return getJsonWithStatus(url, queryParams).getBody();
    }

    /**
     * 发送带签名的 GET 请求，并额外返回 HTTP 状态码。
     * <p>
     * 第三方会用 404 表达"设备不存在或已删除"这类业务语义，只有拿到状态码才能把
     * "接口/网络异常"与"业务语义"区分开。
     *
     * @param url         接口地址（不含查询参数）
     * @param queryParams 业务查询参数（可为空），非空值会与公共参数一起参与签名
     * @return HTTP 状态码 + 响应体
     * @throws Exception 网络或签名异常
     */
    public ApiResponse getJsonWithStatus(String url, Map<String, String> queryParams) throws Exception {
        HttpUrl parsed = HttpUrl.parse(url);
        if (parsed == null) {
            throw new IllegalArgumentException("非法的接口地址：" + url);
        }

        // 1. 组装全部 Query 参数：公共认证参数 + 业务参数（GET 无 bodyDigest）
        Map<String, String> params = new LinkedHashMap<>();
        params.put("apiKey", apiKey);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", generateNonce());
        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                if (key != null && value != null && !value.isEmpty()) {
                    params.put(key, value);
                }
            });
        }

        // 2. 使用全部 Query 参数计算签名
        String signature = sign(params, secret);

        // 3. 构建 URL（追加 Query 参数）
        HttpUrl.Builder urlBuilder = parsed.newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        // 4. 构建 GET 请求（无请求体）
        Request request = new Request.Builder()
                .url(urlBuilder.build().toString())
                .get()
                .addHeader("X-Signature", signature)
                .build();

        // 5. 发送并返回状态码 + 响应体
        try (Response response = client.newCall(request).execute()) {
            return new ApiResponse(response.code(), response.body().string());
        }
    }

    /**
     * 按参数名字典序拼接 {@code key=value&key=value} 后做 HMAC-SHA256，输出十六进制小写。
     */
    private String sign(Map<String, String> params, String secret) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            return bytesToHex(mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("安冬接口签名失败", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /** GET 调用结果：HTTP 状态码 + 响应体 */
    public static class ApiResponse {

        private final int statusCode;
        private final String body;

        public ApiResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getBody() {
            return body;
        }
    }
}
