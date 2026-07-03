package com.agileboot.domain.factorylink.plc.util;

import okhttp3.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class ApiClientPostJson {

    private final String apiKey;
    private final String secret;
    private final OkHttpClient client = new OkHttpClient();

    public ApiClientPostJson(String apiKey, String secret) {
        this.apiKey = apiKey;
        this.secret = secret;
    }

    /**
     * 发送 POST JSON 请求
     * @param url         接口地址（不含查询参数）
     * @param jsonBody    JSON 字符串
     * @return 服务端响应
     */
    public String postJson(String url, String jsonBody) throws Exception {
        // 1. 计算 Body 摘要（SHA-256 十六进制）
        String bodyDigest = sha256Hex(jsonBody);

        // 2. 组装公共参数
        Map<String, String> params = new LinkedHashMap<>();
        params.put("apiKey", apiKey);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", generateNonce());
        params.put("bodyDigest", bodyDigest);  // 重要：防止 Body 被篡改

        // 3. 计算签名（使用所有 Query 参数 + secret）
        String signature = sign(params, secret);

        // 4. 构建 URL（追加 Query 参数）
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String fullUrl = urlBuilder.build().toString();

        // 5. 构建 Request Body
        RequestBody body = RequestBody.create(
                jsonBody,
                MediaType.parse("application/json; charset=utf-8")
        );

        // 6. 构建完整请求
        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .addHeader("X-Signature", signature)
                .build();

        // 7. 发送并返回结果
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    // ---------- 工具方法 ----------
    private String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(digest);
    }

    private String sign(Map<String, String> params, String secret) {
        // 按 key 字典序排序
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        String stringToSign = sb.toString();
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Sign failed", e);
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

    // ---------- 使用示例 ----------
    public static void main(String[] args) throws Exception {
        String apiKey = "ff8b77d3-4a99-4647-8174-00f691bac00b";
        String secret = "4W3KDdHPqPCusLgskw2KqNuP3aKlePDr3_P0tUvSdyc";

        ApiClientPostJson client = new ApiClientPostJson(apiKey, secret);
        String jsonBody = "{\"name\":\"John\",\"age\":30}";
        String result = client.postJson("http://localhost:8080/api/device/user/create", jsonBody);
        System.out.println(result);
    }
}