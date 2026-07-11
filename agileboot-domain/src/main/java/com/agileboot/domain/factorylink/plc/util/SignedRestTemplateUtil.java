package com.agileboot.domain.factorylink.plc.util;


import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SignedRestTemplateUtil {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiSecret;

    public SignedRestTemplateUtil(RestTemplate restTemplate, String apiKey, String apiSecret) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public <T> ResponseEntity<T> get(String baseUrl, String path, MultiValueMap<String, String> businessParams, ParameterizedTypeReference<T> responseType) {
        MultiValueMap<String, String> allParams = buildParams(businessParams);
        String url = buildUrl(baseUrl + path, allParams);
        String signature = ClientSignUtil.sign(allParams, apiSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Signature", signature);

        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), responseType);
    }

    public <T> ResponseEntity<T> post(String baseUrl, String path, MultiValueMap<String, String> businessParams, Object body, ParameterizedTypeReference<T> responseType) {
        MultiValueMap<String, String> allParams = buildParams(businessParams);
        String url = buildUrl(baseUrl + path, allParams);
        String signature = ClientSignUtil.sign(allParams, apiSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Signature", signature);
        headers.set("Content-Type", "application/json");

        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), responseType);
    }

    private MultiValueMap<String, String> buildParams(MultiValueMap<String, String> businessParams) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("apiKey", apiKey);
        params.add("timestamp", String.valueOf(System.currentTimeMillis()));
        params.add("nonce", UUID.randomUUID().toString().replace("-", ""));
        if (businessParams != null) {
            params.addAll(businessParams);
        }
        return params;
    }

    private String buildUrl(String baseUrl, MultiValueMap<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        String separator = baseUrl.contains("?") ? "&" : "?";
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                urlBuilder.append(separator);
                urlBuilder.append(entry.getKey()).append("=").append(value);
                separator = "&";
            }
        }
        return urlBuilder.toString();
    }
}
