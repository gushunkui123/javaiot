package com.agileboot.domain.factorylink.plc.util;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class SignedRestTemplateUtil {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiSecret;

    public SignedRestTemplateUtil(RestTemplate restTemplate, String apiKey, String apiSecret) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public <T> ResponseEntity<T> get(String baseUrl, String path, Map<String, String> businessParams, ParameterizedTypeReference<T> responseType) {
        Map<String, String> allParams = buildParams(businessParams);
        String url = buildUrl(baseUrl + path, allParams);
        String signature = ClientSignUtil.sign(allParams, apiSecret);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Signature", signature);
        
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    public <T> ResponseEntity<T> post(String baseUrl, String path, Map<String, String> businessParams, Object body, ParameterizedTypeReference<T> responseType) {
        Map<String, String> allParams = buildParams(businessParams);
        String url = buildUrl(baseUrl + path, allParams);
        String signature = ClientSignUtil.sign(allParams, apiSecret);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Signature", signature);
        headers.set("Content-Type", "application/json");
        
        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), responseType);
    }

    private Map<String, String> buildParams(Map<String, String> businessParams) {
        Map<String, String> params = new HashMap<>();
        params.put("apiKey", apiKey);
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", java.util.UUID.randomUUID().toString().replace("-", ""));
        if (businessParams != null) {
            params.putAll(businessParams);
        }
        return params;
    }

    private String buildUrl(String baseUrl, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        String separator = baseUrl.contains("?") ? "&" : "?";
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.append(separator);
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue());
            separator = "&";
        }
        return urlBuilder.toString();
    }
}