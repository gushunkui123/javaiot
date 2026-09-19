package com.agileboot.domain.factorylink.plc.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 安冬设备绑定关系查询结果（{@code GET /api/anton/device/{deviceId}/bindings}）。
 * <p>
 * 第三方响应为 {@code {"code":0,"msg":"...","data":[...]}}，其中 {@code code == 0} 表示成功；
 * 无绑定时 {@code data} 为空数组，设备不存在或已删除时返回 <b>HTTP 404</b>。
 * 绑定明细字段由第三方定义，这里不做过早建模，原样透传到 {@link #bindings}。
 */
@Data
public class AntonDeviceBindingResultDTO {

    /** 目标接口地址 */
    private String targetUrl;

    /** 查询的安冬设备ID（来源：设备列表返回的 deviceId） */
    private Long deviceId;

    /** 是否查询成功（第三方业务码 code == 0） */
    private boolean success;

    /** HTTP 状态码（404 表示设备不存在或已删除） */
    private Integer httpStatus;

    /** 第三方业务码（0 = 成功） */
    private Integer code;

    /** 第三方提示信息 */
    private String msg;

    /** 绑定的生产设备条数 */
    private int bindingCount;

    /** 绑定的生产设备明细（字段由第三方定义，原样透传） */
    private List<Map<String, Object>> bindings;

    /** 第三方响应原文（便于排障） */
    private String response;

    /** 失败原因（成功时为空） */
    private String error;

    /** 耗时（毫秒） */
    private long costMs;
}
