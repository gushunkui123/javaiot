package com.agileboot.domain.factorylink.plc.util;



import okhttp3.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ApiClient {

    private final String apiKey;
    private final String secret;
    private final OkHttpClient client = new OkHttpClient();

    public ApiClient(String apiKey, String secret) {
        this.apiKey = apiKey;
        this.secret = secret;
    }

    public String callApi(String url, Map<String, String> businessParams) throws Exception {
        // 1. 组装公共参数
        Map<String, String> params = new HashMap<>(businessParams);
        params.put("apiKey", apiKey);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", generateNonce());

        // 2. 计算签名
        String signature = ClientSignUtil.sign(params, secret);

        // 3. 构建 HTTP 请求（GET 示例）
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader("X-Signature", signature)
                .get()
                .build();

        // 4. 发送请求
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    private String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 使用示例
    public static void main(String[] args) throws Exception {
        String apiKey = "ff8b77d3-4a99-4647-8174-00f691bac00b";
        String secret = "4W3KDdHPqPCusLgskw2KqNuP3aKlePDr3_P0tUvSdyc";

        ApiClient client = new ApiClient(apiKey, secret);
        Map<String, String> bizParams = new HashMap<>();
        bizParams.put("dataCode", "gjUFMB");

        String result = client.callApi("http://localhost:8088/api/device/queryPlcDataPointRealValue", bizParams);
        System.out.println(result);
    }
}