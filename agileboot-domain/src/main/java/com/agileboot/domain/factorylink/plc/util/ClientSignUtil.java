package com.agileboot.domain.factorylink.plc.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.util.MultiValueMap;

public class ClientSignUtil {

    public static String sign(Map<String, String> params, String secret) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 多值参数签名：按 key 字典序、同 key 的多个 value 再按字典序拼接，
     * 与 SignedRestTemplateUtil 生成的 URL 查询串保持一致，便于第三方验签。
     */
    public static String sign(MultiValueMap<String, String> params, String secret) {
        TreeMap<String, List<String>> sorted = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            // 保留值的出现顺序，与 URL 查询串及第三方按出现顺序取值保持一致，避免重复 dataCodes 排序导致签名不一致
            List<String> values = new ArrayList<>(entry.getValue());
            sorted.put(entry.getKey(), values);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : sorted.entrySet()) {
            for (String value : entry.getValue()) {
                if (sb.length() > 0) sb.append("&");
                sb.append(entry.getKey()).append("=").append(value);
            }
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}