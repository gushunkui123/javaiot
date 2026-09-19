package com.agileboot.domain.factorylink.plc.dto;

import java.util.List;
import lombok.Data;

/** 告警规则下发结果：用于确认推送是否成功，并在 Swagger 中直接查看报文与第三方响应 */
@Data
public class RulePushResultDTO {

    /** 目标接口地址 */
    private String targetUrl;

    /** 是否推送成功 */
    private boolean success;

    /** 是否因快照无变化而跳过推送（定时触发时可能为 true） */
    private boolean skipped;

    /** 设备+模具 分组数 */
    private int groupCount;

    /** 规则条数合计 */
    private int ruleCount;

    /** 耗时（毫秒） */
    private long costMs;

    /** 第三方响应原文 */
    private String response;

    /** 失败原因（成功时为空） */
    private String error;

    /** 实际下发的报文（便于对照排查） */
    private List<MoldRulePushDTO> payload;
}
