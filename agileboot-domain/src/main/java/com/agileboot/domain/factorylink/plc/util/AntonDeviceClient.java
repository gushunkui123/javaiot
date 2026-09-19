package com.agileboot.domain.factorylink.plc.util;

import com.agileboot.common.utils.jackson.JacksonUtil;
import com.agileboot.domain.factorylink.plc.dto.AntonDeviceBindingResultDTO;
import com.agileboot.domain.factorylink.plc.dto.AntonDeviceListDTO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 安冬硬件平台设备查询客户端，对应其两个接口：
 * <ul>
 *     <li>{@code GET /api/anton/device/list} —— 分页查询设备列表（{@link #listDevices}）；</li>
 *     <li>{@code GET /api/anton/device/{deviceId}/bindings} —— 查询该设备绑定的生产设备（{@link #listBindings}）；</li>
 * </ul>
 * 另提供 {@link #listDeviceBindings}：内部先取列表拿到 deviceId，再逐个查绑定，一步得到映射关系。
 * <p>
 * 签名复用 {@link ApiClientGetJson}：GET 无 bodyDigest，业务 Query 参数一并参与签名；
 * 而 {@code deviceId} 是路径参数，按第三方约定<b>不参与签名</b>。
 */
@Slf4j
@Component
public class AntonDeviceClient {

    /** 第三方业务成功码之一（接口文档示例值） */
    private static final int SUCCESS_CODE = 0;

    /** 第三方业务成功码之一（设备接口实际返回值） */
    private static final int HTTP_OK = 200;

    /** 设备不存在或已删除时第三方返回的 HTTP 状态码 */
    private static final int HTTP_NOT_FOUND = 404;

    /** 设备列表返回中不展示的硬件字段：在线状态、固件版本 */
    private static final List<String> EXCLUDED_DEVICE_FIELDS = List.of("onlineStatus", "firmwareVersion");

    @Value("${factory-link.anton.enabled:false}")
    private boolean enabled;

    @Value("${factory-link.anton.base-url:}")
    private String baseUrl;

    @Value("${factory-link.anton.device-list-path:/api/anton/device/list}")
    private String deviceListPath;

    @Value("${factory-link.anton.device-bindings-path:/api/anton/device/{deviceId}/bindings}")
    private String deviceBindingsPath;

    @Value("${factory-link.anton.api-key:}")
    private String apiKey;

    @Value("${factory-link.anton.api-secret:}")
    private String apiSecret;

    /** 是否启用安冬接口调用 */
    public boolean isEnabled() {
        return enabled;
    }

    /** 设备列表接口完整地址 */
    public String getListTargetUrl() {
        return baseUrl + deviceListPath;
    }

    /** 设备绑定查询接口完整地址（deviceId 为路径参数） */
    public String getBindingsTargetUrl(Long deviceId) {
        return baseUrl + deviceBindingsPath.replace("{deviceId}", String.valueOf(deviceId));
    }

    /**
     * 分页查询安冬设备列表（原样返回设备信息，含 deviceId）。
     *
     * @param pageNum  页码，null 表示使用第三方默认值（1）
     * @param pageSize 每页条数，null 表示使用第三方默认值（10）
     * @return 查询结果（含响应原文，便于排障）
     */
    public AntonDeviceListDTO listDevices(Integer pageNum, Integer pageSize) {
        AntonDeviceListDTO result = new AntonDeviceListDTO();
        result.setTargetUrl(getListTargetUrl());

        if (!enabled) {
            result.setError("安冬接口未启用（factory-link.anton.enabled=false）");
            return result;
        }

        long start = System.currentTimeMillis();
        try {
            ApiClientGetJson.ApiResponse response = new ApiClientGetJson(apiKey, apiSecret)
                    .getJsonWithStatus(getListTargetUrl(), buildPageParams(pageNum, pageSize));
            result.setHttpStatus(response.getStatusCode());
            result.setResponse(response.getBody());
            parseDeviceList(result, response.getBody());
        } catch (Exception e) {
            result.setError(e.getMessage());
            log.warn("[AntonDevice] 查询设备列表异常, url={}", getListTargetUrl(), e);
        } finally {
            result.setCostMs(System.currentTimeMillis() - start);
        }

        log.info("[AntonDevice] 查询设备列表: url={}, success={}, code={}, total={}, 耗时={}ms",
                getListTargetUrl(), result.isSuccess(), result.getCode(), result.getTotal(), result.getCostMs());
        return result;
    }

    /**
     * 查询安冬设备绑定的生产设备：先按分页取设备列表拿到 deviceId，再逐个查询绑定关系。
     *
     * @param pageNum  页码，null 表示使用第三方默认值（1）
     * @param pageSize 每页条数，null 表示使用第三方默认值（10）
     * @return 每台设备的绑定查询结果；设备列表查询失败时返回单条只带 error 的结果，便于定位
     */
    public List<AntonDeviceBindingResultDTO> listDeviceBindings(Integer pageNum, Integer pageSize) {
        if (!enabled) {
            return List.of(errorResult(getListTargetUrl(), "安冬接口未启用（factory-link.anton.enabled=false）"));
        }

        // 1. 取设备列表，解析出 deviceId
        List<Long> deviceIds;
        try {
            ApiClientGetJson.ApiResponse response = new ApiClientGetJson(apiKey, apiSecret)
                    .getJsonWithStatus(getListTargetUrl(), buildPageParams(pageNum, pageSize));
            deviceIds = parseDeviceIds(response.getBody());
        } catch (Exception e) {
            log.warn("[AntonDevice] 查询设备列表异常, url={}", getListTargetUrl(), e);
            return List.of(errorResult(getListTargetUrl(), "安冬设备列表查询失败：" + e.getMessage()));
        }
        log.info("[AntonDevice] 设备列表查询完成: url={}, 设备数={}", getListTargetUrl(), deviceIds.size());

        // 2. 逐个 deviceId 查绑定关系
        List<AntonDeviceBindingResultDTO> results = new ArrayList<>();
        for (Long deviceId : deviceIds) {
            results.add(listBindings(deviceId));
        }
        return results;
    }

    /**
     * 查询单台安冬设备绑定的生产设备。
     * <p>
     * 单台失败只记录在该条结果的 {@code error} 里，不影响其它设备。
     */
    public AntonDeviceBindingResultDTO listBindings(Long deviceId) {
        AntonDeviceBindingResultDTO result = new AntonDeviceBindingResultDTO();
        result.setDeviceId(deviceId);

        if (deviceId == null) {
            return errorResult(getListTargetUrl(), "deviceId 不能为空（请先确认设备列表返回中有 deviceId）");
        }
        String targetUrl = getBindingsTargetUrl(deviceId);
        result.setTargetUrl(targetUrl);

        long start = System.currentTimeMillis();
        try {
            // deviceId 为路径参数，按第三方约定不参与签名，因此不传任何业务 Query 参数
            ApiClientGetJson.ApiResponse response =
                    new ApiClientGetJson(apiKey, apiSecret).getJsonWithStatus(targetUrl, null);
            result.setHttpStatus(response.getStatusCode());
            result.setResponse(response.getBody());

            if (response.getStatusCode() == HTTP_NOT_FOUND) {
                // 第三方语义：设备不存在或已删除，不按"解析失败"处理
                result.setError("安冬设备不存在或已删除（HTTP 404）");
            } else {
                parseBindings(result, response.getBody());
            }
        } catch (Exception e) {
            result.setError(e.getMessage());
            log.warn("[AntonDevice] 查询设备绑定异常, url={}", targetUrl, e);
        } finally {
            result.setCostMs(System.currentTimeMillis() - start);
        }

        log.info("[AntonDevice] 查询设备绑定: deviceId={}, success={}, httpStatus={}, code={}, 绑定数={}, 耗时={}ms",
                deviceId, result.isSuccess(), result.getHttpStatus(), result.getCode(),
                result.getBindingCount(), result.getCostMs());
        return result;
    }

    /** 解析设备列表，仅取 deviceId，供绑定查询串联使用 */
    private List<Long> parseDeviceIds(String response) {
        Map<String, Object> body = JacksonUtil.fromMap(response);
        Integer code = toInt(body.get("code"));
        if (!isBizSuccess(code)) {
            throw new IllegalStateException("code=" + code + ", msg=" + body.get("msg"));
        }

        List<Long> deviceIds = new ArrayList<>();
        if (body.get("rows") instanceof List<?> rows) {
            for (Object row : rows) {
                if (row instanceof Map<?, ?> rowMap) {
                    Long deviceId = toLong(rowMap.get("deviceId"));
                    if (deviceId == null) {
                        deviceId = toLong(rowMap.get("id"));
                    }
                    if (deviceId == null) {
                        log.warn("[AntonDevice] 设备记录中未找到 deviceId，跳过: {}", row);
                        continue;
                    }
                    deviceIds.add(deviceId);
                }
            }
        }
        return deviceIds;
    }

    /** 解析设备列表响应：{@code {"total":1,"code":200,"msg":"查询成功","rows":[...]}} */
    private void parseDeviceList(AntonDeviceListDTO result, String response) {
        try {
            Map<String, Object> body = JacksonUtil.fromMap(response);
            Integer code = toInt(body.get("code"));
            result.setCode(code);
            result.setMsg(body.get("msg") == null ? null : String.valueOf(body.get("msg")));
            result.setTotal(toLong(body.get("total")));
            List<Map<String, Object>> rows = extractItems(body.get("rows"));
            // 展示过滤：去掉不需要展示的硬件字段（第三方原始响应仍保留在 response 字段中，便于排障）
            for (Map<String, Object> row : rows) {
                for (String excluded : EXCLUDED_DEVICE_FIELDS) {
                    row.remove(excluded);
                }
            }
            result.setRows(rows);
            result.setRowCount(rows.size());
            result.setSuccess(isBizSuccess(code));
        } catch (Exception e) {
            result.setError("第三方响应解析失败：" + e.getMessage());
            log.warn("[AntonDevice] 响应解析失败, response={}", response);
        }
    }

    /** 解析绑定响应：{@code {"code":200,"msg":"查询成功","data":[...]}}，无绑定时 data 为空数组 */
    private void parseBindings(AntonDeviceBindingResultDTO result, String response) {
        try {
            Map<String, Object> body = JacksonUtil.fromMap(response);
            Integer code = toInt(body.get("code"));
            result.setCode(code);
            result.setMsg(body.get("msg") == null ? null : String.valueOf(body.get("msg")));
            result.setBindings(extractItems(body.get("data")));
            result.setBindingCount(result.getBindings().size());
            result.setSuccess(isBizSuccess(code));
        } catch (Exception e) {
            result.setError("第三方响应解析失败：" + e.getMessage());
            log.warn("[AntonDevice] 响应解析失败, response={}", response);
        }
    }

    /** 第三方业务码判定：文档示例为 0，设备接口实际返回 200 */
    private boolean isBizSuccess(Integer code) {
        return code != null && (code == HTTP_OK || code == SUCCESS_CODE);
    }

    /** 仅传显式指定的分页参数；参数集与签名集保持严格一致 */
    private Map<String, String> buildPageParams(Integer pageNum, Integer pageSize) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        if (pageNum != null) {
            queryParams.put("pageNum", String.valueOf(pageNum));
        }
        if (pageSize != null) {
            queryParams.put("pageSize", String.valueOf(pageSize));
        }
        return queryParams;
    }

    /** 把第三方返回的数组原样转成 {@code List<Map>}，字段不做建模 */
    private List<Map<String, Object>> extractItems(Object values) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (values instanceof List<?> valueList) {
            for (Object item : valueList) {
                if (item instanceof Map<?, ?> itemMap) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    itemMap.forEach((key, value) -> row.put(String.valueOf(key), value));
                    list.add(row);
                }
            }
        }
        return list;
    }

    /** 构造只带失败原因的结果，用于设备列表本身查不通时回显 */
    private AntonDeviceBindingResultDTO errorResult(String targetUrl, String error) {
        AntonDeviceBindingResultDTO result = new AntonDeviceBindingResultDTO();
        result.setTargetUrl(targetUrl);
        result.setError(error);
        return result;
    }

    private Integer toInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
