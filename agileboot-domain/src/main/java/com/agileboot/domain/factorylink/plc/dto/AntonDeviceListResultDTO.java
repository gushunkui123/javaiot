package com.agileboot.domain.factorylink.plc.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 安冬设备列表查询结果（{@code GET /api/anton/device/list}）。
 * <p>
 * 第三方响应为 {@code {"total":1,"code":200,"msg":"查询成功","rows":[...]}}，其中 {@code code} 为 200 表示成功。
 * 设备明细字段由第三方定义，这里不做过早建模，原样透传到 {@link #rows}。
 */
@Data
public class AntonDeviceListResultDTO {

    /** 目标接口地址 */
    private String targetUrl;

    /** 是否查询成功 */
    private boolean success;

    /** HTTP 状态码 */
    private Integer httpStatus;

    /** 第三方业务码（200 / 0 = 成功） */
    private Integer code;

    /** 第三方提示信息 */
    private String msg;

    /** 设备总数（第三方 total） */
    private Long total;

    /** 本页返回的设备条数 */
    private int rowCount;

    /** 设备明细（字段由第三方定义，原样透传） */
    private List<Map<String, Object>> rows;

    /** 第三方响应原文（便于排障） */
    private String response;

    /** 失败原因（成功时为空） */
    private String error;

    /** 耗时（毫秒） */
    private long costMs;
}
